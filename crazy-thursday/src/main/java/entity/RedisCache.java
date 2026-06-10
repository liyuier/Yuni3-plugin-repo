package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: RedisCache
 * @Author yuier
 * @Package entity
 * @Date 2026/2/9 11:45
 * @description: 疯四发送文案时间点在 redis 中存储的对象类型定义
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisCache {

    public static final String REDIS_KEY = "plugin:crazy:thursday:key";
    // 存储的日期
    private String date;
    // 存储的 4 个时间点
    private List<Long> timePoints;
}
