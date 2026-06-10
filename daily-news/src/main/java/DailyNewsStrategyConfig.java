import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: DailyNewsStrategyConfig
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 6:11
 * @description: 每日新闻发送策略
 */

@Data
@NoArgsConstructor
public class DailyNewsStrategyConfig {

    /**
     * 策略
     * - blacklist: 黑名单
     * - whitelist: 白名单
     */
    private String strategy;
}
