package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: entity.PigTodayCache
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/3 14:04
 * @description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PigTodayCache {

    private String imageTitle;
    private String imageName;
    private String date;
}
