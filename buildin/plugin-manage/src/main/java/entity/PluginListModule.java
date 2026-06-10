package entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: PluginListModule
 * @Author yuier
 * @Package entity
 * @Date 2025/12/30 4:57
 * @description: 展示插件列表中的插件模块元素
 */

@Data
public class PluginListModule {
     private String moduleName;
    List<PluginListSinglePlugin> subPlugins;

    public PluginListModule() {
        subPlugins =  new ArrayList<>();
    }

    public void addSubPlugin(PluginListSinglePlugin plugin) {
        subPlugins.add(plugin);
    }
}
