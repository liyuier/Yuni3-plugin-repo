package steamstatus.utils;

import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import steamstatus.MemberSteamStatus;
import steamstatus.config.SteamApiKey;
import steamstatus.db.DBHelper;
import steamstatus.db.UserSteamId;
import steamstatus.model.PlayerSummariesResponse;
import steamstatus.model.SinglePlayerSummary;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Title: SteamStatusUtil
 * @Author yuier
 * @Package steamstatus.utils
 * @Date 2026/6/17 20:54
 * @description: 工具类
 */

@Slf4j
public class SteamStatusUtil {

    /**
     * 同步数据库中的 steamid 到 Redis 中
     */
    public static void syncDbToRedis() {
        HashMap<String, Long> steamIdToUserIdMap = new HashMap<>();
        DBHelper.findAll().forEach(userSteamId -> {
            steamIdToUserIdMap.put(userSteamId.getSteamId(), userSteamId.getUserId());
        });
        RedisHelper.updateIdMap(steamIdToUserIdMap);
    }

    // 拼接基础请求 url
    public static String getBaseUrl() {
        String baseUrl = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";
        SteamApiKey steamApiKey = PluginUtils.loadJsonConfigFromPlugin("steam_api_key.json", SteamApiKey.class, MemberSteamStatus.class);
        baseUrl += "?key=" + steamApiKey.getApikey() + "&steamids=";
        return baseUrl;
    }

    // 根据 steamid 获取用户 id
    public static Long getUserIdBySteamId(String steamId) {
        return (Long) RedisHelper.getIdMap().get(steamId);
    }

    // 初始化用户状态
    public static void initPlayerStatus() {
        HashMap<String, String> steamIdToPlayerStatusMap = new HashMap<>();
        int idDataCount = DBHelper.getIdDataCount();
        if (idDataCount == 0) {
            RedisHelper.updatePlayerStatusMap(steamIdToPlayerStatusMap);
            return;
        }
        String baseUrl = getBaseUrl();
        for (int i = 0; i <= idDataCount / 100; i += 20) {
            List<UserSteamId> userSteamIds = DBHelper.findByPage(100, i);
            // 将 steamid 用 ',' 连接起来
            String steamIdsParamStr = userSteamIds.stream()
                    .map(UserSteamId::getSteamId)  // 提取 steamId 字段
                    .filter(steamId -> steamId != null && !steamId.isEmpty()) // 过滤空值
                    .collect(Collectors.joining(","));
            String url = baseUrl + steamIdsParamStr;
            // 请求 steam 接口
            PlayerSummariesResponse response = PluginUtils.simpleGet(url, PlayerSummariesResponse.class);
            // 维护用户状态
            response.getResponse().getPlayers().forEach(playerSummer -> {
                steamIdToPlayerStatusMap.put(playerSummer.getSteamid(), PluginUtils.serialize(playerSummer));
            });
        }
        RedisHelper.updatePlayerStatusMap(steamIdToPlayerStatusMap);
    }

    // 根据 steamid 获取当前用户状态
    public static SinglePlayerSummary getPlayerStatusBySteamId(String steamId) {
        Object cached = RedisHelper.getPlayerStatusMap().get(steamId);
        if (cached == null) return null;
        return PluginUtils.deserialize((String) cached, SinglePlayerSummary.class);
    }

    // 添加 steamid 到 Redis 中
    public static void addSteamIdToRedis(String steamId, Long userId) {
        RedisHelper.addId(steamId, userId);
        String url = getBaseUrl() + steamId;
        // 请求 steam 接口
        PlayerSummariesResponse response = PluginUtils.simpleGet(url, PlayerSummariesResponse.class);
        // 维护用户状态
        response.getResponse().getPlayers().forEach(playerSummer -> {
            RedisHelper.addPlayerStatus(playerSummer.getSteamid(), PluginUtils.serialize(playerSummer));
        });
    }

    // 删除 steamid 从 Redis 中
    public static void deleteSteamIdFromRedis(String steamId) {
        RedisHelper.deleteId(steamId);
        RedisHelper.deletePlayerStatus(steamId);
    }

    // 刷新 id-status
    public static void refreshIdStatus(SinglePlayerSummary 玩家状态) {
        RedisHelper.refreshIdStatus(玩家状态.getSteamid(), PluginUtils.serialize(玩家状态));
    }
}
