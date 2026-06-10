import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.pattern.PatternDetector;
import com.yuier.yuni.plugin.model.passive.message.PatternPlugin;

/**
 * @Title: AntiGbf
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/6 3:02
 * @description: 反GBF
 */

public class AntiGbf extends PatternPlugin {
    @Override
    public PatternDetector getDetector() {
        return new PatternDetector(chain -> chain.containsString("granbluefantasy.jp"));
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        eventContext.getChatSession().reply("大家不要信，这是骑空士的陷阱！");
    }
}
