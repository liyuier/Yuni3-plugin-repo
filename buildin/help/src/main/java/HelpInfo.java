import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandNodeDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandNode;
import com.yuier.yuni.plugin.model.passive.message.CommandNodePlugin;
import com.yuier.yuni.plugin.util.PluginUtils;

/**
 * @Title: HelpInfo
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 12:43
 * @description: 帮助信息输出
 */

public class HelpInfo extends CommandNodePlugin {

    private static final CommandNode ROOT = CommandNode.builder("帮助").build();

    @Override
    public CommandNodeDetector getDetector() {
        return new CommandNodeDetector(ROOT);
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        String helpInfoFilePath = "help-info.txt";
        String helpInfoStr = PluginUtils.loadTextFromPluginFolder(helpInfoFilePath, this.getClass());
        eventContext.getChatSession().response(helpInfoStr);
    }
}
