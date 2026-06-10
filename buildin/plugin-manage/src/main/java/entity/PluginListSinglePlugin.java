package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: PluginListSinglePlugin
 * @Author yuier
 * @Package entity
 * @Date 2025/12/30 5:11
 * @description: 列表展示时的单个插件
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginListSinglePlugin {
    private int index;
    private String name;
    private String desc;
    private boolean enabled;
}
