package db;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * @Title: db.DailyNewsGroupEnableDao
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 6:00
 * @description:
 */

@RegisterBeanMapper(DailyNewsGroupEnable.class)
public interface DailyNewsGroupEnableDao {

    @SqlUpdate("CREATE TABLE IF NOT EXISTS daily_news_group_enable (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "groupId INTEGER," +
            "black INTEGER," +
            "white INTEGER" +
            ")")
    void createTable();

    @SqlUpdate("INSERT INTO daily_news_group_enable (groupId, black, white) VALUES (:groupId, :black, :white)")
    void insert(DailyNewsGroupEnable groupEnable);

    @SqlQuery("SELECT * FROM daily_news_group_enable WHERE black = 1")
    List<DailyNewsGroupEnable> findBlackLists();

    @SqlQuery("SELECT * FROM daily_news_group_enable WHERE white = 1")
    List<DailyNewsGroupEnable> findWhiteLists();

    @SqlQuery("SELECT * FROM daily_news_group_enable")
    List<DailyNewsGroupEnable> findAll();

    @SqlUpdate("DELETE FROM daily_news_group_enable WHERE groupId = :groupId")
    void deleteGroup(DailyNewsGroupEnable groupEnable);
}
