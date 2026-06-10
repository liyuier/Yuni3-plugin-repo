package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Title: ApiResponseSetu
 * @Author yuier
 * @Package entity
 * @Date 2026/2/5 14:00
 * @description: 响应的单个色图
 */

@Data
@NoArgsConstructor
public class ApiResponseSetu {

    // pid
    // 依据文档描述，所有图片均来自 pixiv
    private long pid;
    // 作品所在页
    private int p;
    // 画师的 pixiv id
    private int uid;
    // 标题
    private String title;
    // 作者
    private String author;
    // 是否为 r18 . 接口作者的划分，不等同于作品本身的 R18 分类
    private boolean r18;
    // 原图宽度 px
    private int width;
    // 原图高度 px
    private int height;
    // 作品标签
    private List<String> tags;
    // 图片文件扩展名
    private String ext;
    // 是否 AI 作品
    // 0 未知（旧画作或字段未更新），1 不是，2 是
    private boolean aiType;
    // 上传日期时间戳
    private long uploadDate;
    // 图片链接
    private SetuUrl urls;
}
