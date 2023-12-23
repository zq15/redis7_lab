package com.example.lock.service;

import com.example.lock.lock.DistributedLockFactory;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String port;

    @Autowired
    private DistributedLockFactory distributedLockFactory;

    @Autowired
    private Redisson redisson;

//    private Lock lock = new ReentrantLock();

    // v 9.0
    public String saleByRedisson() {
        String retMessage = "";
        String key = "zzyRedisLock";

        RLock lock = redisson.getLock(key);
        lock.lock();

        try {
            //1 查询库存信息
            String result = stringRedisTemplate.opsForValue().get("inventory001");
            //2 判断库存是否足够
            Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
            //3 扣除库存，每次减少一个
            if (inventoryNumber > 0) {
                inventoryNumber--;
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(inventoryNumber));
                retMessage = "成功卖出一个商品，剩余库存" + inventoryNumber;
                System.out.println(retMessage + "，端口号：" + port);
//                testReEntryByRedisson();
            } else {
                retMessage = "商品卖完了";
            }
        } finally {
            // TODO 高并发下会存在 v 5.0 删除别人的锁的问题
            // v 9.1 改进
            // ps 不需要 lua 脚本保证原子性，框架底层已帮我们实现
            if (lock.isLocked() &&!lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return retMessage + "，端口号：" + port;
    }

    // v 7.0 lua 脚本
    public String sale() {
        String retMessage = "";
        String key = "zzyRedisLock";

        Lock redisLock = distributedLockFactory.getDistributedLock("redis");
        redisLock.lock();

        try {
            //1 查询库存信息
            String result = stringRedisTemplate.opsForValue().get("inventory001");
            //2 判断库存是否足够
            Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
            //3 扣除库存，每次减少一个
            if (inventoryNumber > 0) {
                inventoryNumber--;
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(inventoryNumber));
                retMessage = "成功卖出一个商品，剩余库存" + inventoryNumber;
                System.out.println(retMessage + "，端口号：" + port);
                testReEntry();
            } else {
                retMessage = "商品卖完了";
            }
        } finally {
            redisLock.unlock();
        }

        return retMessage + "，端口号：" + port;
    }

    private void testReEntryByRedisson() {
        Lock redisLock = redisson.getLock("zzyRedisLock");
        redisLock.lock();

        try {
            System.out.println("========测试可重入锁========");
        } finally {
            redisLock.unlock();
        }
    }

    private void testReEntry() {
        Lock redisLock = distributedLockFactory.getDistributedLock("redis");
        redisLock.lock();

        try {
            System.out.println("========测试可重入锁========");
        } finally {
            redisLock.unlock();
        }
    }


    /**
     * v6.0
     * @return
     */
    /*
    public String sale() {
        String retMessage = "";
        String key = "zzyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + Thread.currentThread().getId();


        // 不用递归了，高并发下使用自旋替代递归重试，使用while
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue)) {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 抢锁成功的请求线程，进行正常的业务逻辑操作，扣减库存

        try {
            //1 查询库存信息
            String result = stringRedisTemplate.opsForValue().get("inventory001");
            //2 判断库存是否足够
            Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
            //3 扣除库存，每次减少一个
            if (inventoryNumber > 0) {
                inventoryNumber--;
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(inventoryNumber));
                retMessage = "成功卖出一个商品，剩余库存" + inventoryNumber;
                System.out.println(retMessage + "，端口号：" + port);
            } else {
                retMessage = "商品卖完了";
            }
        } finally {
//            stringRedisTemplate.delete(key);
            // 改进点，修改为 Lua 脚本的 redis 分布式锁调用，必须保证原子性，参考官网脚本案例
            String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                                "return redis.call('del',KEYS[1]) " +
                            "else " +
                                "return 0 " +
                            "end";
            stringRedisTemplate.execute(new DefaultRedisScript(luaScript, Boolean.class), Arrays.asList(key), uuidValue);
        }

        return retMessage + "，端口号：" + port;
    }
    *
     */

    // 3.1 递归 重试 容易栈溢出 不推荐，高并发唤醒后 while 判断而不是if
    /*
    public String sale() {
        String retMessage = "";
        String key = "zzyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + Thread.currentThread().getId();

        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);

        if (!flag) {
            // 暂停20s，进行递归重试
            try {
                TimeUnit.MICROSECONDS.sleep(20);} catch (InterruptedException e) {e.printStackTrace();}
            sale();
        } else {
            // 抢锁成功的请求线程，进行正常的业务逻辑操作，扣减库存

            try {
                //1 查询库存信息
                String result = stringRedisTemplate.opsForValue().get("inventory001");
                //2 判断库存是否足够
                Integer inventoryNumber = result == null? 0 : Integer.parseInt(result);
                //3 扣除库存，每次减少一个
                if (inventoryNumber > 0) {
                    inventoryNumber--;
                    stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(inventoryNumber));
                    retMessage = "成功卖出一个商品，剩余库存" + inventoryNumber;
                    System.out.println(retMessage + "，端口号：" + port);
                }else {
                    retMessage = "商品卖完了";
                }
            } finally {
                stringRedisTemplate.delete(key);
            }
        }

        return retMessage + "，端口号：" + port;
    }
     */

    /**
     * v2.0 单机版加锁配合nginx和jmeter压测后，不满足分布式锁的性能要求，出现超卖
     * @return
     */
    /*
    public String sale() {
        String retMessage = "";

        lock.lock();
        try {
            //1 查询库存信息
            String result = stringRedisTemplate.opsForValue().get("inventory001");
            //2 判断库存是否足够
            Integer inventoryNumber = result == null? 0 : Integer.parseInt(result);
            //3 扣除库存，每次减少一个
            if (inventoryNumber > 0) {
                inventoryNumber--;
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(inventoryNumber));
                retMessage = "成功卖出一个商品，剩余库存" + inventoryNumber;
                System.out.println(retMessage + "，端口号：" + port);
            }else {
                retMessage = "商品卖完了";
            }
        } finally {
            lock.unlock();
        }

        return retMessage + "，端口号：" + port;
    }
    */
}
