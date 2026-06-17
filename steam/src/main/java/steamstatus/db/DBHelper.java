package steamstatus.db;

import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.List;

/**
 * @Title: DBHelper
 * @Author yuier
 * @Package steamstatus.db
 * @Date 2026/6/17 19:33
 * @description: 数据库操作工具类
 */

@Slf4j
public class DBHelper {

    /**
     * 创建数据库连接
     */
    public static void createTableIfNotExists() {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            userSteamIdDao.createTableIfNotExists();
        });
        log.info("表 user_steam_id 创建成功");
    }

    /**
     * 添加用户 steamid
     */
    public static void insert(UserSteamId userSteamId) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            userSteamIdDao.insert(userSteamId);
        });
    }

    /**
     * 更新用户 steamid
     */
    public static void updateByUserId(UserSteamId userSteamId) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            userSteamIdDao.updateByUserId(userSteamId);
        });
    }

    /**
     * 根据用户 id 查询用户 steamid
     */
    public static List<UserSteamId> findByUserId(Long userId) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            return userSteamIdDao.findByUserId(userId);
        });
    }

    /**
     * 查询所有用户 steamid
     */
    public static List<UserSteamId> findAll() {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            return userSteamIdDao.findAll();
        });
    }

    /**
     * 根据用户 id 删除用户 steamid
     */
    public static void deleteByUserId(Long userId) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            userSteamIdDao.deleteByUserId(userId);
        });
    }

    /**
     * 判断用户 id 是否存在
     */
    public static boolean userIdExists(Long userId) {
        List<UserSteamId> byUserId = findByUserId(userId);
        return byUserId != null && !byUserId.isEmpty();
    }

    /**
     * 分页查询，pageNum 从 0 开始
     */
    public static List<UserSteamId> findByPage(int pageSize, int pageNum) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            return userSteamIdDao.findAllByPage(pageSize, pageNum);
        });
    }

    /**
     * 获取数据库中数据的行数
     */
    public static int getIdDataCount() {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            UserSteamIdDao userSteamIdDao = handle.attach(UserSteamIdDao.class);
            return userSteamIdDao.count();
        });
    }
}
