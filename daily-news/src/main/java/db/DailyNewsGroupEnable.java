package db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: db.DailyNewsGroupEnable
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 5:57
 * @description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyNewsGroupEnable {
    // 自增主键
    Long id;
    // 群号
    Long groupId;
    // 如果为 1 则在黑名单内
    Integer black;
    // 如果为 1 则在白名单内
    Integer white;
}