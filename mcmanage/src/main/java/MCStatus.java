import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.plugin.data.CrudRepository;
import com.yuier.yuni.plugin.util.PluginUtils;
import db.GroupMcServer;
import utils.McServerPinger;

import java.util.List;

import static com.yuier.yuni.core.constants.SystemConstants.*;
import static utils.Constant.*;

/**
 * @Title: MCStatus
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/8/13 0:14
 * @description: MC 服务器状态查询
 */

public class MCStatus {

    public static void mcServerStatus(YuniMessageEvent context) {
        Long groupId = context.getGroupId();
        CommandResult commandResult = context.getCommandResult();

        // 首先检查有没有携带服务器名称参数
        if (!commandResult.hasArg(服务器名称)) {
            // 检查当前群组下有多少服务器
            CrudRepository<GroupMcServer> repo = PluginUtils.repo(GroupMcServer.class);
            List<GroupMcServer> groupMcServerList = repo.findByField("groupId", groupId);
            if (groupMcServerList.size() > 1) {
                context.getChatSession().reply(
                        "当前群组下绑定多个 MC 服务器，请明确要查询的服务器名称: \n" +
                                buildServerNameList(groupMcServerList).strip());
            } else if (groupMcServerList.isEmpty()) {
                context.getChatSession().reply("当前群组下未添加服务器，请先添加服务器。");
            } else {
                // 群组下有且仅有一个服务器
                GroupMcServer groupMcServer = groupMcServerList.get(FIRST_INDEX);
                context.getChatSession().reply(McServerPinger.buildMcServerStatusStr(groupMcServer.getServerAddr()));
            }
        } else {
            String serverName = commandResult.getArg(服务器名称).asText();

            // 检查数据库中当前群组是否绑定了该服务器名称
            List<GroupMcServer> servers = PluginUtils.query(GroupMcServer.class)
                    .where("groupId", groupId)
                    .where("serverName", serverName)
                    .list();
            if (servers.isEmpty()) {
                context.getChatSession().reply("当前群组下未添加名称为【" + serverName + "】的服务器，请确认服务器名称。");
                return;
            }

            GroupMcServer groupMcServer = servers.get(FIRST_INDEX);
            context.getChatSession().reply(McServerPinger.buildMcServerStatusStr(groupMcServer.getServerAddr()));
        }
    }

    private static String buildServerNameList(List<GroupMcServer> GroupMcServerList) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < GroupMcServerList.size(); i++) {
            GroupMcServer groupMcServer = GroupMcServerList.get(i);
            result.append(i + 1).append(". ")
                    .append(groupMcServer.getServerName()).append(" ")
                    .append(groupMcServer.getServerAddr()).append("\n");
        }
        return result.toString();
    }
}
