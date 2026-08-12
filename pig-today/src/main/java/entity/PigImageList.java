package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: entity.PigImageList
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/3 13:40
 * @description: 猪猪图片列表
 */

@Data
@NoArgsConstructor
public class PigImageList {
    private Integer code;
    private String message;
    private List<PigImageListElement> data;
}
