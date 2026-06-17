package steamstatus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: SinglePlayerSummary
 * @Author yuier
 * @Package steamstatus.model
 * @Date 2026/6/17 19:57
 * @description: 单个玩家概况
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SinglePlayerSummary {

    private String steamid;
    private String personaname;
    private String gameextrainfo;
    private String gameid;
}
