import com.yuier.yuni.core.enums.CommandArgRequireType;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.event.detector.message.command.CommandNodeDetector;
import com.yuier.yuni.event.detector.message.command.model.ArgDef;
import com.yuier.yuni.event.detector.message.command.model.CommandNode;
import com.yuier.yuni.plugin.model.passive.message.CommandNodePlugin;

import static util.PluginManagerConstants.*;

/**
 * @Title: PluginManage
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2025/12/28 0:20
 * @description: 插件管理入口
 */

public class PluginManage extends CommandNodePlugin {

    private PluginShow pluginShow = new PluginShow();
    private PluginEnable pluginEnable = new PluginEnable();
    private PluginReload pluginReload = new PluginReload();

    private static final CommandNode ROOT = CommandNode.builder(PLUGIN_MANAGE_HEAD)
            .description("插件管理")
            .requiresChild()
            // 命令选项 -查看，携带可选参数 pluginSeq ，含义是插件序号
            .child(CommandNode.builder(PLUGIN_MANAGE_VIEW)
                    .description("查看指定插件详情")
                    .arg(ArgDef.optional(PLUGIN_MANAGE_VIEW_SEQ, "查看指定插件详情", CommandArgRequireType.NUMBER))
                    .build())
            .child(CommandNode.builder(PLUGIN_MANAGE_ENABLE)
                    .description("开启指定插件")
                    .arg(ArgDef.required(PLUGIN_MANAGE_ENABLE_SEQ, "开启指定插件", CommandArgRequireType.NUMBER))
                    .build())
            .child(CommandNode.builder(PLUGIN_MANAGE_DISABLE)
                    .description("关闭指定插件")
                    .arg(ArgDef.required(PLUGIN_MANAGE_DISABLE_SEQ, "关闭指定插件", CommandArgRequireType.NUMBER))
                    .build())
            .child(CommandNode.builder(PLUGIN_MANAGE_RELOAD)
                    .description("重载插件")
                    .arg(ArgDef.optional(PLUGIN_MANAGE_RELOAD_SEQ, "重载插件", CommandArgRequireType.NUMBER))
                    .build())
            .build();

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandResult result = eventContext.getCommandResult();
        if (result.hasChild(PLUGIN_MANAGE_VIEW)) {
            if (result.getChild(PLUGIN_MANAGE_VIEW).hasArg(PLUGIN_MANAGE_VIEW_SEQ)) {
                pluginShow.showPluginDetail(eventContext, result, this);
            } else {
                pluginShow.showPluginList(eventContext, this);
            }
        }
        if (result.hasChild(PLUGIN_MANAGE_ENABLE)) {
            if (result.getChild(PLUGIN_MANAGE_ENABLE).hasArg(PLUGIN_MANAGE_ENABLE_SEQ)) {
                pluginEnable.enablePlugin(eventContext, result);
            }
        }
        if (result.hasChild(PLUGIN_MANAGE_DISABLE)) {
            if (result.getChild(PLUGIN_MANAGE_DISABLE).hasArg(PLUGIN_MANAGE_DISABLE_SEQ)) {
                pluginEnable.disablePlugin(eventContext, result);
            }
        }
        if (result.hasChild(PLUGIN_MANAGE_RELOAD)) {
            if (result.getChild(PLUGIN_MANAGE_RELOAD).hasArg(PLUGIN_MANAGE_RELOAD_SEQ)) {
                pluginReload.reloadSpecifiedPlugin(eventContext, result);
            } else {
                pluginReload.reloadAllPlugins(eventContext, this);
            }
        }
    }

    @Override
    public CommandNodeDetector getDetector() {
        return new CommandNodeDetector(ROOT);
    }
}
