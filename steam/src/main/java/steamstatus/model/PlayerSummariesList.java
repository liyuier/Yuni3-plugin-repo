package steamstatus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: PlayerSummariesList
 * @Author yuier
 * @Package steamstatus.model
 * @Date 2026/6/17 19:57
 * @description: 玩家情况列表
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSummariesList {

    private List<SinglePlayerSummary> players;
}
