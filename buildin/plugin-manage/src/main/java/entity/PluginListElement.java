package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: PluginListElement
 * @Author yuier
 * @Package entity
 * @Date 2025/12/28 3:32
 * @description:
 */

@Data
@NoArgsConstructor
public class PluginListElement {
    private Boolean hasModule;
    private PluginListSinglePlugin singlePlugin;
    private PluginListModule modulePlugin;

    public PluginListElement(PluginListSinglePlugin singlePlugin) {
        this.hasModule = false;
        this.singlePlugin = singlePlugin;
    }

    public PluginListElement(PluginListModule modulePlugin) {
        this.hasModule = true;
        this.modulePlugin = modulePlugin;
    }
}
