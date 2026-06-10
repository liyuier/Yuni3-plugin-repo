package immediate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: TargetGroupConfig
 * @Author yuier
 * @Package immediate
 * @Date 2026/2/12 13:52
 * @description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetGroupConfig {

    private List<Long> targetGroups;
}
