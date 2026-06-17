package steamstatus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: PlayerSummariesResponse
 * @Author yuier
 * @Package steamstatus.model
 * @Date 2026/6/17 19:56
 * @description: steam 玩家概况接口响应模型
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSummariesResponse {

    private PlayerSummariesList response;
}
