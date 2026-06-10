package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: entity.PigImageListElement
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/3 13:13
 * @description: 猪猪详情数据
 */

@Data
@NoArgsConstructor
public class PigImageListElement {
    // id
    private String id;
    // 描述
    private String title;
    // 目前已知有 图片 、GIF
    private String duration;
    // 图片类型目前已知有 static 、 animated
    private String imageType;
    // 查看次数
    private Long viewCount;
    // 下载次数
    private Long downloadCount;
    // URL 路径，从 data 开始
    private String thumbnail;
    // 图片名
    private String filename;
    // 大概是上传时间
    private Long mtime;
}
