import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.plugin.data.CrudRepository;
import com.yuier.yuni.plugin.util.PluginUtils;
import db.GroupMcServer;
import utils.AddrCheckResult;
import utils.MCManageUtils;
import utils.McServerPinger;

import java.util.List;

import static com.yuier.yuni.core.constants.SystemConstants.FIRST_INDEX;
import static utils.Constant.*;

/**
 * @Title: MCServer
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/8/12 19:42
 * @description: MC 服务器的添加 / 删除
 */

public class MCServer {

    public static void addMCServer(YuniMessageEvent context) {
        CommandResult commandResult = context.getCommandResult();
        Long groupId = context.getGroupId();
        CommandResult addServer = commandResult.getChild(添加);
        String serverName = addServer.getArg(服务器名称).asText();
        String serverAddr = addServer.getArg(服务器地址).asText();

        CrudRepository<GroupMcServer> repo = PluginUtils.repo(GroupMcServer.class);

        // 先查看数据库中是否存在该名称的 MC 服务器
        List<GroupMcServer> servers = repo.findByField("serverName", serverName);
        if (!servers.isEmpty()) {
            context.getChatSession().reply("当前群组已添加同名服务器，请重新选择服务器名称。");
            return;
        }
        // 检查 MC 服务器地址是否合法
        AddrCheckResult addrCheck = MCManageUtils.mcServerAddrValid(serverAddr);
        if (!addrCheck.isSuccess()) {
            context.getChatSession().reply(addrCheck.getMessage());
            return;
        }

        // 保存到数据库中
        GroupMcServer groupMcServer = new GroupMcServer();
        groupMcServer.setGroupId(groupId);
        groupMcServer.setServerName(serverName);
        groupMcServer.setServerAddr(serverAddr);
        repo.save(groupMcServer);

        // 查询一下目标服务器状态，并将各种信息响应给群里
        context.getChatSession().reply("成功添加服务器！当前服务器状态: \n" + McServerPinger.buildMcServerStatusStr(serverAddr));
    }

    public static void deleteMCServer(YuniMessageEvent context) {
        CommandResult commandResult = context.getCommandResult();
        Long groupId = context.getGroupId();
        CommandResult deleteServer = commandResult.getChild(删除);
        String serverName = deleteServer.getArg(服务器名称).asText();

        // 检查数据库中当前群组是否绑定了该服务器名称
        List<GroupMcServer> servers = PluginUtils.query(GroupMcServer.class)
                .where("groupId", groupId)
                .where("serverName", serverName)
                .list();
        if (servers.isEmpty()) {
            context.getChatSession().reply("当前群组下未添加名称为【" + serverName + "】的服务器，请确认服务器名称。");
            return;
        }

        PluginUtils.delete(GroupMcServer.class)
                .where("groupId", groupId)
                .where("serverName", serverName)
                .execute();
        context.getChatSession().reply("已删除名称为【" + serverName + "】的服务器。");
    }

    public static void verifyMCServerName(YuniMessageEvent context) {
        CommandResult commandResult = context.getCommandResult();
        Long groupId = context.getGroupId();
        CommandResult verifyServerName = commandResult.getChild(修改名称);
        String serverName = verifyServerName.getArg(服务器名称).asText();
        String newServerName = verifyServerName.getArg(服务器新名称).asText();

        // 检查数据库中当前群组是否绑定了该服务器名称
        List<GroupMcServer> servers = PluginUtils.query(GroupMcServer.class)
                .where("groupId", groupId)
                .where("serverName", serverName)
                .list();
        if (servers.isEmpty()) {
            context.getChatSession().reply("当前群组下未添加名称为【" + serverName + "】的服务器，请确认服务器名称。");
            return;
        }

        // 保存新名称
        CrudRepository<GroupMcServer> repo = PluginUtils.repo(GroupMcServer.class);
        GroupMcServer groupMcServer = servers.get(FIRST_INDEX);
        groupMcServer.setServerName(newServerName);
        repo.save(groupMcServer);

        context.getChatSession().reply("已修改名称为【" + serverName + "】的服务器至新名称:【" + newServerName + "】。");
    }

    public static void verifyMCServerAddr(YuniMessageEvent context) {
        CommandResult commandResult = context.getCommandResult();
        Long groupId = context.getGroupId();
        CommandResult verifyServerName = commandResult.getChild(修改地址);
        String serverName = verifyServerName.getArg(服务器名称).asText();
        String newServerAddr = verifyServerName.getArg(服务器地址).asText();

        // 检查数据库中当前群组是否绑定了该服务器名称
        List<GroupMcServer> servers = PluginUtils.query(GroupMcServer.class)
                .where("groupId", groupId)
                .where("serverName", serverName)
                .list();
        if (servers.isEmpty()) {
            context.getChatSession().reply("当前群组下未添加名称为【" + serverName + "】的服务器，请确认服务器名称。");
            return;
        }

        // 保存新地址
        CrudRepository<GroupMcServer> repo = PluginUtils.repo(GroupMcServer.class);
        GroupMcServer groupMcServer = servers.get(FIRST_INDEX);
        groupMcServer.setServerAddr(newServerAddr);
        repo.save(groupMcServer);

        context.getChatSession().reply("已修改名称为【" + serverName + "】的服务器至新地址: " + newServerAddr + ".");
    }
}
