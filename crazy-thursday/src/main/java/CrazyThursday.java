import com.yuier.yuni.core.util.CronExpressionBuilder;
import com.yuier.yuni.core.util.RedisUtil;
import com.yuier.yuni.plugin.manage.enable.PluginEnableProcessor;
import com.yuier.yuni.plugin.manage.enable.event.PluginDisableEvent;
import com.yuier.yuni.plugin.manage.enable.event.PluginEnableEvent;
import com.yuier.yuni.plugin.model.active.Action;
import com.yuier.yuni.plugin.model.active.scheduled.ScheduledPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import entity.RedisCache;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Title: CrazyThursday
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/9 11:34
 * @description: 疯狂星期四文案
 */

public class CrazyThursday extends ScheduledPlugin {
    @Override
    public void enable(PluginEnableEvent event) {
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
        processor.enablePlugin(event, StartAtThursday.class);
    }

    @Override
    public void disable(PluginDisableEvent event) {
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
        processor.disablePlugin(event, StartAtThursday.class);
    }

    @Override
    public Action getAction() {
        return () -> {
            Long 今天凌晨零点毫秒数 = System.currentTimeMillis();
            // 获取预期发送文案的时间点
            List<Long> 发送文案的时间点 = chooseTimePoints(今天凌晨零点毫秒数);
            // 存到 redis 里
            RedisCache redisCache = new RedisCache(LocalDate.now().toString(), 发送文案的时间点);
            RedisUtil.set(RedisCache.REDIS_KEY, PluginUtils.serialize(redisCache));
            // 计算时间间隔
            List<Long> 发送文案的间隔时间 = CrazyThursdayUtil.计算等待间隔(发送文案的时间点, 今天凌晨零点毫秒数);
            for (Long 间隔 : 发送文案的间隔时间) {
                try {
                    // 等待间隔
                    Thread.sleep(间隔);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                CrazyThursdayUtil.发送疯狂星期四文案(this);
            }
        };
    }



    /**
     * 生成今天将要发送疯四文案的时间点
     * @param 今天凌晨零点毫秒数 今天凌晨零点毫秒数
     * @return 时间点
     */
    public static List<Long> chooseTimePoints(Long 今天凌晨零点毫秒数) {

        // 定义时间段边界（毫秒偏移）
        final Long 一小时毫秒数 = 3_600_000L; // 1小时 = 3600000毫秒
        Long 上午十点 = 今天凌晨零点毫秒数 + 10 * 一小时毫秒数; // 10:00:00.000
        Long 下午一点   = 今天凌晨零点毫秒数 + 13 * 一小时毫秒数; // 13:00:00.000（开区间终点）
        Long 下午三点 = 今天凌晨零点毫秒数 + 15 * 一小时毫秒数; // 15:00:00.000
        Long 傍晚六点   = 今天凌晨零点毫秒数 + 18 * 一小时毫秒数; // 18:00:00.000（开区间终点）

        List<Long> timePoints = new ArrayList<>();

        // 生成并排序第一个时间段的两个时间点
        timePoints.addAll(生成区间内随机数(上午十点, 下午一点, 2));

        // 生成并排序第二个时间段的两个时间点
        timePoints.addAll(生成区间内随机数(下午三点, 傍晚六点, 2));

        return timePoints;
    }

    /**
     * 生成区间内指定数量的随机数
     * @param 开始含 开始区间
     * @param 结束不含 结束区间
     * @param 需求数量 数量
     * @return 随机数
     */
    private static List<Long> 生成区间内随机数(Long 开始含, Long 结束不含, Integer 需求数量) {
        if (结束不含 <= 开始含) {
            throw new IllegalArgumentException("结束区间必须大于开始区间");
        }
        if (结束不含 - 开始含 < 需求数量) {
            throw new IllegalArgumentException("区间长度必须大于等于数量");
        }
        Set<Long> tmpResult = new HashSet<>();
        while (tmpResult.size() < 需求数量) {
            tmpResult.add(ThreadLocalRandom.current().nextLong(开始含, 结束不含));
        }
        ArrayList<Long> result = new ArrayList<>(tmpResult);
        result.sort(Long::compareTo);
        return result;
    }

    @Override
    public String cronExpression() {
        return CronExpressionBuilder.create().weeklyOn(4).build();
    }
}
