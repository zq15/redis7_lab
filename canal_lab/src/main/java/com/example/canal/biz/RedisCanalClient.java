package com.example.canal.biz;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisCanalClient {

    @Resource
    RedisTemplate redisTemplate;

    /**
     * 读取 canal 数据写入redis
     * @param columns 行
     */
    public void redisInsert(List<CanalEntry.Column> columns) {
        JSONObject jsonObject = new JSONObject();
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "  update=" + column.getUpdated());
            jsonObject.put(column.getName(), column.getValue());
        }
        if (columns.size()>0) {
            redisTemplate.opsForValue().set(columns.get(0).getValue(), jsonObject.toJSONString());
        }
    }

    /**
     * 读取 canal 数据删除 redis 行
     * @param columns 行
     */
    public void redisDelete(List<CanalEntry.Column> columns) {
        JSONObject jsonObject = new JSONObject();
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "  update=" + column.getUpdated());
            jsonObject.put(column.getName(), column.getValue());
        }
        if (columns.size()>0) {
            redisTemplate.delete(columns.get(0).getValue());
        }
    }

    /**
     * 读取 canal 数据修改 redis 行
     * @param columns 行
     */
    public void redisUpdate(List<CanalEntry.Column> columns) {
        JSONObject jsonObject = new JSONObject();
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "  update=" + column.getUpdated());
            jsonObject.put(column.getName(), column.getValue());
        }
        if (columns.size()>0) {

            redisTemplate.opsForValue().set(columns.get(0).getValue(), jsonObject.toJSONString());
            System.out.println("----------------update after: " + redisTemplate.opsForValue().get(columns.get(0).getValue()));

        }
    }

    /**
     * 读取 canal 数据并操作
     * @param entrys 行
     */
    public void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    redisDelete(rowData.getBeforeColumnsList());
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    redisInsert(rowData.getAfterColumnsList());
                } else {
                    redisUpdate(rowData.getAfterColumnsList());
                }
            }
        }
    }

}
