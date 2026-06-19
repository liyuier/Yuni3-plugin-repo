package steamstatus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: AppDetailData
 * @Author yuier
 * @Package steamstatus.model
 * @Date 2026/6/19 20:15
 * @description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppDetailData {

    private String type;
    private String name;
    private String steamAppid;
}
