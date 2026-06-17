package steamstatus.db;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * @Title: UserSteamIdDao
 * @Author yuier
 * @Package steamstatus.db
 * @Date 2026/6/17 19:24
 * @description: 用户-steamid 表操作接口
 */

@RegisterBeanMapper(UserSteamId.class)
public interface UserSteamIdDao {

    @SqlUpdate("CREATE TABLE IF NOT EXISTS user_steam_id (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "steam_id TEXT" +
            ")")
    void createTableIfNotExists();

    @SqlUpdate("INSERT INTO user_steam_id (user_id, steam_id) VALUES (:userId, :steamId)")
    void insert(@BindBean UserSteamId userSteamId);

    @SqlUpdate("DELETE FROM user_steam_id WHERE user_id = :userId")
    void deleteByUserId(@Bind("userId") Long userId);

    @SqlUpdate("UPDATE user_steam_id SET steam_id = :steamId WHERE user_id = :userId")
    void updateByUserId(@BindBean UserSteamId userSteamId);

    @SqlQuery("SELECT * FROM user_steam_id WHERE user_id = :userId")
    List<UserSteamId> findByUserId(@Bind("userId") Long userId);

    @SqlQuery("SELECT * FROM user_steam_id")
    List<UserSteamId> findAll();

    // 分页查询
    @SqlQuery("SELECT * FROM user_steam_id LIMIT :pageSize OFFSET :offset")
    List<UserSteamId> findAllByPage(@Bind("pageSize") int pageSize, @Bind("offset") int offset);

    // 查询数据库中数据的行数
    @SqlQuery("SELECT COUNT(*) FROM user_steam_id")
    int count();
}
