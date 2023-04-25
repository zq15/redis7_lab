package com.example.lock.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自研分布式锁，实现 Lock 接口
 */
public class RedisDistributedLock implements Lock {

    private StringRedisTemplate stringRedisTemplate;

    private String lockName;

    private String uuidValue;

    private long expireTime;

    /*
    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate, String lockName) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockName = lockName;
        this.uuidValue = IdUtil.simpleUUID() + Thread.currentThread().getId();
        this.expireTime = 50L;
    }
     */
    public RedisDistributedLock(StringRedisTemplate stringRedisTemplate, String lockName, String uuid) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockName = lockName;
        this.uuidValue = uuid + ":" + Thread.currentThread().getId();
        this.expireTime = 50L;
    }


    @Override
    public void lock() {
        tryLock();
    }

    @Override
    public boolean tryLock() {
        try {
            tryLock(-1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (time == -1L) {
            String luaScript = "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1 then " +
                    "    redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                    "    redis.call('expire', KEYS[1], ARGV[2]) " +
                    "    return 1 " +
                    "else" +
                    "    return 0 " +
                    "end";
            while (!(Boolean) stringRedisTemplate.execute(new DefaultRedisScript(luaScript, Boolean.class),
                    Arrays.asList(lockName), uuidValue, String.valueOf(expireTime))){
                try {
                    Thread.sleep(60);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 新建一个后台扫描程序，来监视key目前的ttl，是否到我们规定的 1/2 1/3 来实现续期
            resetExpire();
            return true;
        }
        return false;
    }

    public void resetExpire() {
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 1 then " +
                "return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                "return 0 " +
                "end";
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                if ((boolean) stringRedisTemplate.execute(new DefaultRedisScript(script, Boolean.class),
                        Arrays.asList(lockName), uuidValue, String.valueOf(expireTime))) {
                    resetExpire();
                }
            }
        }, (this.expireTime * 1000) / 3);
    }

    @Override
    public void unlock() {
        String luaScript = "if redis.call('hexists', KEYS[1], ARGV[1]) == 0 then " +
                "    return nil " +
                "elseif redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0 then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
        // nil = false 1 = true 0 = false
        Long flag = (Long) stringRedisTemplate.execute(new DefaultRedisScript(luaScript, Long.class),
                Arrays.asList(lockName), uuidValue);
        if (null == flag) {
            throw new RuntimeException("锁不存在");
        }
    }

    // 下面两个暂时用不到，condition 中断
    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }
}
