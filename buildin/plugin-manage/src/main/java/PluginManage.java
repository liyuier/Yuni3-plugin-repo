import com.yuier.yuni.core.enums.CommandArgRequireType;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;

import static util.PluginManagerConstants.*;

/**
 * @Title: PluginManage
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2025/12/28 0:20
 * @description: 插件管理入口
 */

public class PluginManage extends CommandPlugin {

    private PluginShow pluginShow = new PluginShow();
    private PluginEnable pluginEnable = new PluginEnable();
    private PluginReload pluginReload = new PluginReload();

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandMatched commandMatched = eventContext.getCommandMatched();
        if (commandMatched.hasOption(PLUGIN_MANAGE_VIEW)) {
            if (commandMatched.optionHasOptionalArg(PLUGIN_MANAGE_VIEW)) {
                pluginShow.showPluginDetail(eventContext, commandMatched, this);
            } else {
                pluginShow.showPluginList(eventContext, this);
            }
        }
        if (commandMatched.hasOption(PLUGIN_MANAGE_ENABLE)) {
            if (commandMatched.optionHasRequiredArg(PLUGIN_MANAGE_ENABLE)) {
                pluginEnable.enablePlugin(eventContext, commandMatched);
            }
        }
        if (commandMatched.hasOption(PLUGIN_MANAGE_DISABLE)) {
            if (commandMatched.optionHasRequiredArg(PLUGIN_MANAGE_DISABLE)) {
                pluginEnable.disablePlugin(eventContext, commandMatched);
            }
        }
        /* 待开发 */
        if (commandMatched.hasOption(PLUGIN_MANAGE_RELOAD)) {
            if (commandMatched.optionHasOptionalArg(PLUGIN_MANAGE_RELOAD)) {
                pluginReload.reloadSpecifiedPlugin(eventContext, commandMatched);
            } else {
                pluginReload.reloadAllPlugins(eventContext, this);
            }
        }
    }

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create(PLUGIN_MANAGE_HEAD)
                // 命令选项 -查看，携带可选参数 pluginSeq ，含义是插件序号
                .addOptionWithOptionalArg(PLUGIN_MANAGE_VIEW, PLUGIN_MANAGE_VIEW_SEQ, "查看指定插件详情", CommandArgRequireType.NUMBER)
                .addOptionWithRequiredArg(PLUGIN_MANAGE_ENABLE, PLUGIN_MANAGE_ENABLE_SEQ, "开启指定插件", CommandArgRequireType.NUMBER)
                .addOptionWithRequiredArg(PLUGIN_MANAGE_DISABLE, PLUGIN_MANAGE_DISABLE_SEQ, "关闭指定插件", CommandArgRequireType.NUMBER)
                .addOptionWithOptionalArg(PLUGIN_MANAGE_RELOAD, PLUGIN_MANAGE_RELOAD_SEQ, "重载插件", CommandArgRequireType.NUMBER)
                .build()) ;
    }
}
