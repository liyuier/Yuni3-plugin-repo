package db;

import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.List;

/**
 * @Title: db.DBHelper
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 6:03
 * @description:
 */

@Slf4j
public class DBHelper {

    /**
     * 创建数据库连接
     */
    public static void createJdbi () {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle( handle -> {
            DailyNewsGroupEnableDao groupEnableDao = handle.attach(DailyNewsGroupEnableDao.class);
            groupEnableDao.createTable();
        });
        log.info("表 daily_news_group_enable 创建成功");
    }

    /**
     * 查询黑名单
     */
    public static List<DailyNewsGroupEnable> findBlackLists() {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            DailyNewsGroupEnableDao groupEnableDao = handle.attach(DailyNewsGroupEnableDao.class);
            return groupEnableDao.findBlackLists();
        });
    }

    /**
     * 查询白名单
     */
    public static List<DailyNewsGroupEnable> findWhiteLists() {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            DailyNewsGroupEnableDao groupEnableDao = handle.attach(DailyNewsGroupEnableDao.class);
            return groupEnableDao.findWhiteLists();
        });
    }

    /**
     * 查询所有
     */
    public static List<DailyNewsGroupEnable> findAll() {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi.withHandle(handle -> {
            DailyNewsGroupEnableDao groupEnableDao = handle.attach(DailyNewsGroupEnableDao.class);
            return groupEnableDao.findAll();
        });
    }

    /**
     * 添加群记录
     */
    public static void insert(DailyNewsGroupEnable groupEnable) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            DailyNewsGroupEnableDao groupEnableDao = handle.attach(DailyNewsGroupEnableDao.class);
            groupEnableDao.insert(groupEnable);
        });
        log.info("群 {} 添加成功", groupEnable.getGroupId());
    }

    /**
     * 添加群到黑名单
     */
    public static void addGroupToBlackList(Long groupId) {
        removeGroup(groupId);
        insert(new DailyNewsGroupEnable(null, groupId, 1, null));
    }

    /**
     * 添加群到白名单
     */
    public static void addGroupToWhiteList(Long groupId) {
        removeGroup(groupId);
        insert(new DailyNewsGroupEnable(null, groupId, null, 1));
    }

     /**
     * 移除群
     */
     public static void removeGroup(Long groupId) {
        Jdbi jdbi = Jdbi.create(PluginUtils.getAppDatabaseUrl());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            DailyNewsGroupEnableDao groupEnableDao = handle.attach(DailyNewsGroupEnableDao.class);
            groupEnableDao.deleteGroup(new DailyNewsGroupEnable(null, groupId, null, null));
        });
      }
}
