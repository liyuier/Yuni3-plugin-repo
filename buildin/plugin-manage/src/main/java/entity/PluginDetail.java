package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: PluginDetail
 * @Author yuier
 * @Package entity
 * @Date 2025/12/30 19:16
 * @description: 插件详情展示页
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginDetail {

    private Integer index;
    private String name;
    private String description;
    private List<String> tips;
    private String author;
    private String version;
    private Boolean enabled;
}
