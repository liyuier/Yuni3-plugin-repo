package pattern;

import com.yuier.yuni.core.bot.MessageSentResult;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.pattern.PatternDetector;
import com.yuier.yuni.plugin.model.passive.message.PatternPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;

/**
 * @Title: pattern.HelloPattern
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2025/12/23 20:06
 * @description:
 */

public class HelloPattern extends PatternPlugin {

    @Override
    public void execute(YuniMessageEvent eventContext) {
        MessageSentResult response = eventContext.getChatSession().response("你好，我是 " + PluginUtils.getBotNickName() + " !");
    }

    @Override
    public PatternDetector getDetector() {
        return new PatternDetector(chain -> {
            return chain.containsString("你好");
        });
    }
}
