import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.ArgDef;
import com.yuier.yuni.event.detector.message.command.model.CommandNode;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import db.GroupMcServer;

import static utils.Constant.*;

/**
 * @Title: MCManage
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/8/11 3:26
 * @description: MC 服务器管理
 */

public class MCManage extends CommandPlugin {

    private static final CommandNode ROOT = CommandNode.builder(安慕希)
            .description("MC 服务器管理")
            .arg(ArgDef.optional(服务器名称, "MC 服务器名称"))
            .child(CommandNode.builder(添加)
                    .description("添加 MC 服务器")
                    .arg(ArgDef.required(服务器名称, "MC 服务器名称"))
                    .arg(ArgDef.required(服务器地址, "服务器地址"))
                    .build())
            .child(CommandNode.builder(删除)
                    .description("删除 MC 服务器")
                    .arg(ArgDef.required(服务器名称, "MC 服务器名称"))
                    .build())
            .child(CommandNode.builder(修改名称)
                    .description("修改 MC 服务器名称")
                    .arg(ArgDef.required(服务器名称, "MC 服务器旧名称"))
                    .arg(ArgDef.required(服务器新名称, "MC 服务器新名称"))
                    .build())
            .child(CommandNode.builder(修改地址)
                    .description("修改 MC 服务器地址")
                    .arg(ArgDef.required(服务器名称, "MC 服务器旧名称"))
                    .arg(ArgDef.required(服务器地址, "MC 服务器地址"))
                    .build())
            .build();

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(ROOT);
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandResult commandResult = eventContext.getCommandResult();
        if (commandResult.hasChild(添加)) {
            MCServer.addMCServer(eventContext);
        } else if (commandResult.hasChild(删除)) {
            MCServer.deleteMCServer(eventContext);
        } else if (commandResult.hasChild(修改名称)) {
            MCServer.verifyMCServerName(eventContext);
        } else if (commandResult.hasChild(修改地址)) {
            MCServer.verifyMCServerAddr(eventContext);
        } else {
            MCStatus.mcServerStatus(eventContext);
        }
    }

    @Override
    public void initialize() {
        // 建表 + 注册
        PluginUtils.registerEntity(GroupMcServer.class);
    }
}
