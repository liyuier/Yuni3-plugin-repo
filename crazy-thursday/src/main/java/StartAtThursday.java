import com.yuier.yuni.core.util.RedisUtil;
import com.yuier.yuni.plugin.manage.enable.PluginEnableProcessor;
import com.yuier.yuni.plugin.manage.enable.event.PluginDisableEvent;
import com.yuier.yuni.plugin.manage.enable.event.PluginEnableEvent;
import com.yuier.yuni.plugin.model.active.Action;
import com.yuier.yuni.plugin.model.active.immediate.ImmediatePlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import entity.RedisCache;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * @Title: StartAtThursday
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/9 14:37
 * @description: 如果在周四重启了怎么办
 */

@Slf4j
public class StartAtThursday extends ImmediatePlugin {
    @Override
    public void enable(PluginEnableEvent event) {
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
        processor.enablePlugin(event, CrazyThursday.class);
    }

    @Override
    public void disable(PluginDisableEvent event) {
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
        processor.disablePlugin(event, CrazyThursday.class);
    }

    @Override
    public Action getAction() {
        return () -> {
            if (!LocalDate.now().getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
                return;
            }
            if (!RedisUtil.exists(RedisCache.REDIS_KEY)) {
                return;
            }
            Long 当前时间 = System.currentTimeMillis();
            RedisCache redisCache = PluginUtils.deserialize((String) RedisUtil.get(RedisCache.REDIS_KEY), RedisCache.class);
            assert redisCache != null;
            List<Long> 发送文案的间隔时间 = CrazyThursdayUtil.计算等待间隔(redisCache.getTimePoints(), 当前时间);
            for (Long 间隔 : 发送文案的间隔时间) {
                try {
                    // 等待间隔
                    log.info("[StartAtThursday] 开始等待间隔：{} 毫秒", 间隔);
                    Thread.sleep(间隔);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                CrazyThursdayUtil.发送疯狂星期四文案(this);
            }
        };
    }
}
