import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.plugin.data.CrudRepository;
import com.yuier.yuni.plugin.util.PluginUtils;
import db.GroupMcServers;
import utils.AddrCheckResult;
import utils.MCManageUtils;
import utils.McServerPinger;

import java.util.List;

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

        CrudRepository<GroupMcServers> repo = PluginUtils.repo(GroupMcServers.class);

        // 先查看数据库中是否存在该名称的 MC 服务器
        List<GroupMcServers> servers = repo.findByField("serverName", serverName);
        if (!servers.isEmpty()) {
            context.getChatSession().reply("当前群组已添加同名服务器，请重新选择服务器名称");
            return;
        }
        // 检查 MC 服务器地址是否合法
        AddrCheckResult addrCheck = MCManageUtils.mcServerAddrValid(serverAddr);
        if (!addrCheck.isSuccess()) {
            context.getChatSession().reply(addrCheck.getMessage());
            return;
        }

        // 保存到数据库中
        GroupMcServers groupMcServer = new GroupMcServers();
        groupMcServer.setGroupId(groupId);
        groupMcServer.setServerName(serverName);
        groupMcServer.setServerAddr(serverAddr);
        repo.save(groupMcServer);

        // 查询一下目标服务器状态，并将各种信息响应给群里
        context.getChatSession().reply("成功添加服务器！当前服务器状态: \n" + McServerPinger.buildMcServerStatusStr(serverAddr));
    }
}
