package steamstatus.db;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: UserSteamId
 * @Author yuier
 * @Package steamstatus.db
 * @Date 2026/6/17 15:59
 * @description: 记录用户 steamid 关系
 */

@Data
@NoArgsConstructor
public class UserSteamId {

    private Long id;

    private Long userId;

    private String steamId;

    public UserSteamId(Long userId, String steamId) {
        this.userId = userId;
        this.steamId = steamId;
    }
}
