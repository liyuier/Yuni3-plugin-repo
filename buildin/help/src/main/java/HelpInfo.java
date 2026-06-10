import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;

/**
 * @Title: HelpInfo
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 12:43
 * @description: 帮助信息输出
 */

public class HelpInfo extends CommandPlugin {

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create("帮助").build());
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        String helpInfoFilePath = "help-info.txt";
        String helpInfoStr = PluginUtils.loadTextFromPluginFolder(helpInfoFilePath, this.getClass());
        eventContext.getChatSession().response(helpInfoStr);
    }
}
