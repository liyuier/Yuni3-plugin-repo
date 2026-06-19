package steamstatus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: AppDetailResponse
 * @Author yuier
 * @Package steamstatus.model
 * @Date 2026/6/19 20:13
 * @description: 游戏详情响应
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppDetailResponse {
    private Boolean success;
    private AppDetailData data;
}
