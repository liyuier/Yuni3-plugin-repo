package meta;

import com.yuier.yuni.core.event.meta.YuniHeartbeatEvent;
import com.yuier.yuni.core.event.meta.YuniMetaEvent;
import com.yuier.yuni.event.detector.meta.DefaultYuniMetaDetector;
import com.yuier.yuni.plugin.model.passive.meta.MetaPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Title: HeartBeatEvent
 * @Author yuier
 * @Package meta
 * @Date 2026/1/5 21:25
 * @description: 心跳事件
 */

@Slf4j
public class HeartBeatEvent extends MetaPlugin {
    @Override
    public DefaultYuniMetaDetector getDetector() {
        return new DefaultYuniMetaDetector(event -> {
            if ("heartbeat".equals(event.getMetaEventType())) {
                return PluginUtils.deserialize(event.getRawJson(), YuniHeartbeatEvent.class);
            }
            return null;
        });
    }

    @Override
    public void execute(YuniMetaEvent eventContext) {
        YuniHeartbeatEvent heartbeatEvent = (YuniHeartbeatEvent) eventContext;
        log.debug(heartbeatEvent.toLogString());
    }
}
