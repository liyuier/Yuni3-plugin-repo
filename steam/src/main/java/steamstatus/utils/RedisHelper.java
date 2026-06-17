package steamstatus.utils;

import com.yuier.yuni.core.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: RedisHelper
 * @Author yuier
 * @Package steamstatus.redis
 * @Date 2026/6/17 20:26
 * @description: Redis 操作工具类
 */

@Slf4j
public class RedisHelper {

    public static final String STEAM_ID_TO_USER_ID_MAPPING = "plugin:steam:steamid:to:userid:key";
    public static final String STEAM_ID_TO_PLAYER_STATUS_MAPPING = "plugin:steam:steamid:to:playerstatus:key";

    /**
     * 更新 steamid 与 userid 的映射关系
     */
    public static void updateIdMap(HashMap<String, Long> steamIdToUserIdMap) {
        RedisUtil.delete(STEAM_ID_TO_USER_ID_MAPPING);
        RedisUtil.hSetAll(STEAM_ID_TO_USER_ID_MAPPING, steamIdToUserIdMap);
    }

    // 获取 steamid 与 userid 的映射关系
    public static Map<String, Object> getIdMap() {
        return RedisUtil.hGetAll(STEAM_ID_TO_USER_ID_MAPPING);
    }

    // 增加一条 steamid-userid 的记录
    public static void addId(String steamId, Long userId) {
        RedisUtil.hSet(STEAM_ID_TO_USER_ID_MAPPING, steamId, userId);
    }

    // 删除一条 steamid-userid 的记录
    public static void deleteId(String steamId) {
        RedisUtil.hDelete(STEAM_ID_TO_USER_ID_MAPPING, steamId);
    }

    // 更新 steamid 与 playerstatus 的映射关系
    public static void updatePlayerStatusMap(HashMap<String, String> steamIdToPlayerStatusMap) {
        RedisUtil.delete(STEAM_ID_TO_PLAYER_STATUS_MAPPING);
        RedisUtil.hSetAll(STEAM_ID_TO_PLAYER_STATUS_MAPPING, steamIdToPlayerStatusMap);
    }

    // 获取 steamid 与 playerstatus 的映射关系
    public static Map<String, Object> getPlayerStatusMap() {
        return RedisUtil.hGetAll(STEAM_ID_TO_PLAYER_STATUS_MAPPING);
    }

    // 增加一条 steamid-状态 的记录
    public static void addPlayerStatus(String steamId, String playerStatus) {
        RedisUtil.hSet(STEAM_ID_TO_PLAYER_STATUS_MAPPING, steamId, playerStatus);
    }

    // 删除一条 steamid-状态 的记录
    public static void deletePlayerStatus(String steamId) {
        RedisUtil.hDelete(STEAM_ID_TO_PLAYER_STATUS_MAPPING, steamId);
    }

    // 刷新 steamid-状态 的记录
    public static void refreshIdStatus(String steamid, String serialize) {
        RedisUtil.hSet(STEAM_ID_TO_PLAYER_STATUS_MAPPING, steamid, serialize);
    }
}
