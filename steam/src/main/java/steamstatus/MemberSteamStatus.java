package steamstatus;

import com.yuier.yuni.core.bot.MessageTarget;
import com.yuier.yuni.core.bot.YuniBot;
import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.core.util.CronExpressionBuilder;
import com.yuier.yuni.event.util.EventLogUtil;
import com.yuier.yuni.plugin.manage.enable.PluginEnableProcessor;
import com.yuier.yuni.plugin.manage.enable.event.PluginDisableEvent;
import com.yuier.yuni.plugin.manage.enable.event.PluginEnableEvent;
import com.yuier.yuni.plugin.model.active.Action;
import com.yuier.yuni.plugin.model.active.scheduled.ScheduledPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import steamstatus.db.DBHelper;
import steamstatus.db.UserSteamId;
import steamstatus.model.PlayerSummariesResponse;
import steamstatus.model.SinglePlayerSummary;
import steamstatus.utils.SteamStatusUtil;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Title: member.MemberSteamStatus
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/6/17 15:20
 * @description: 群友 steam 游戏状态监控
 */

public class MemberSteamStatus extends ScheduledPlugin {
    @Override
    public void enable(PluginEnableEvent event) {

    }

    @Override
    public void disable(PluginDisableEvent event) {

    }

    @Override
    public Action getAction() {
        return () -> {
            int idDataCount = DBHelper.getIdDataCount();
            if (idDataCount == 0) {
                return;
            }
            String baseUrl = SteamStatusUtil.getBaseUrl();

            for (int i = 0; i <= idDataCount / 100; i += 20) {
                List<UserSteamId> userSteamIds = DBHelper.findByPage(100, i);
                CompletableFuture.runAsync(() -> {
                    // 将 steamid 用 ',' 连接起来
                    String steamIdsParamStr = userSteamIds.stream()
                            .map(UserSteamId::getSteamId)  // 提取 steamId 字段
                            .filter(steamId -> steamId != null && !steamId.isEmpty()) // 过滤空值
                            .collect(Collectors.joining(","));
                    String url = baseUrl + steamIdsParamStr;
                    // 请求 steam 接口
                    PlayerSummariesResponse response = PluginUtils.simpleGet(url, PlayerSummariesResponse.class);
                    response.getResponse().getPlayers().forEach(playerSummer -> {
                        String steamid = playerSummer.getSteamid();
                        SinglePlayerSummary 之前的状态 = SteamStatusUtil.getPlayerStatusBySteamId(steamid);
                        checkAndReportStatusChange(之前的状态, playerSummer);
                        // 刷新 redis
                        SteamStatusUtil.refreshIdStatus(playerSummer);
                    });
                });
            }
        };
    }

    // 针对不同状态变化进行差分
    private void checkAndReportStatusChange(SinglePlayerSummary 之前的状态, SinglePlayerSummary 新的状态) {
        if (之前的状态 == null) {
            // 新记录且状态为在游戏
            if (新的状态.getGameid() != null) {
                reportPlaying(新的状态);
            }
            return;
        }
        if (之前的状态.getGameid() == null) {
            // 旧记录且状态变化为：不在游戏 -> 在游戏
            if (新的状态.getGameid() != null) {
                reportPlaying(新的状态);
            }
            return;
        } else {
            // 旧记录且状态变化为：在游戏 -> 不在游戏
            if (新的状态.getGameid() == null) {
                reportStopPlaying(之前的状态);
                return;
            }
            // 旧记录且状态变化为：在游戏 -> 在另一个游戏
            if (!之前的状态.getGameid().equals(新的状态.getGameid())) {
                reportChangePlaying(之前的状态, 新的状态);
            }
        }
    }

    // 变更游玩的游戏
    private void reportChangePlaying(SinglePlayerSummary 之前的状态, SinglePlayerSummary 新的状态) {
        // 查找用户 ID
        Long userId = SteamStatusUtil.getUserIdBySteamId(之前的状态.getSteamid());
        // 获取用户所在的群组
        HashSet<Long> userGroups = PluginUtils.findUserGroups(userId);
        userGroups.forEach(groupId -> {
            PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
            // 判断该群组是否启用当前功能
            if (processor.isPluginEnabled(groupId, this.getClass())) {
                YuniBot bot = PluginUtils.getYuniBot();
                String groupMemberName = EventLogUtil.getGroupMemberName(groupId, userId);
                bot.sendMessage(MessageTarget.group(groupId), new MessageChain(
                        "群友 " + groupMemberName +
                                "(Steam id: " + 之前的状态.getPersonaname() + ") 不再游玩: " + 之前的状态.getGameextrainfo() +
                                " ; 正在游玩: " + 新的状态.getGameextrainfo()
                ));
            }
        });

    }

    // 停止游戏
    private void reportStopPlaying(SinglePlayerSummary 之前的状态) {
        // 查找用户 ID
        Long userId = SteamStatusUtil.getUserIdBySteamId(之前的状态.getSteamid());
        // 获取用户所在的群组
        HashSet<Long> userGroups = PluginUtils.findUserGroups(userId);
        userGroups.forEach(groupId -> {
            PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
            // 判断该群组是否启用当前功能
            if (processor.isPluginEnabled(groupId, this.getClass())) {
                YuniBot bot = PluginUtils.getYuniBot();
                String groupMemberName = EventLogUtil.getGroupMemberName(groupId, userId);
                bot.sendMessage(MessageTarget.group(groupId), new MessageChain(
                        "群友 " + groupMemberName + "(Steam id: " + 之前的状态.getPersonaname() + ") 不再游玩: " + 之前的状态.getGameextrainfo()
                ));
            }
        });
    }

    // 启动游戏
    private void reportPlaying(SinglePlayerSummary 新的状态) {
        // 查找用户 ID
        Long userId = SteamStatusUtil.getUserIdBySteamId(新的状态.getSteamid());
        // 获取用户所在的群组
        HashSet<Long> userGroups = PluginUtils.findUserGroups(userId);
        userGroups.forEach(groupId -> {
            PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
            // 判断该群组是否启用当前功能
            if (processor.isPluginEnabled(groupId, this.getClass())) {
                YuniBot bot = PluginUtils.getYuniBot();
                String groupMemberName = EventLogUtil.getGroupMemberName(groupId, userId);
                bot.sendMessage(MessageTarget.group(groupId), new MessageChain(
                        "群友 " + groupMemberName + "(Steam id: " + 新的状态.getPersonaname() + ") 正在游玩: " + 新的状态.getGameextrainfo()
                ));
            }
        });
    }

    @Override
    public String cronExpression() {
        return CronExpressionBuilder.create().everyMinutes(1).build();
    }

    @Override
    public void initialize() {
        DBHelper.createTableIfNotExists();
        SteamStatusUtil.syncDbToRedis();
    }
}
