import com.yuier.yuni.core.model.message.segment.TextSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.plugin.manage.PluginContainer;
import com.yuier.yuni.plugin.model.PluginInstance;
import com.yuier.yuni.plugin.model.PluginModuleInstance;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import util.PluginManageUtil;
import util.PluginReloadProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static util.PluginManagerConstants.PLUGIN_MANAGE_RELOAD;

/**
 * @Title: PluginReload
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/6 0:03
 * @description: 重载插件
 */

@Slf4j
@NoArgsConstructor
public class PluginReload {

    public void reloadSpecifiedPlugin(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 先判断插件序号是否越界
        TextSegment pluginSeqSegment = (TextSegment) commandMatched.getOptionOptionalArgValue(PLUGIN_MANAGE_RELOAD);
        int pluginSeq = Integer.parseInt(pluginSeqSegment.getText());
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        if (pluginSeq <= 0 || pluginSeq > container.getPluginCount()) {
            eventContext.getChatSession().response("没有序号为" + pluginSeq + "的插件。");
            return;
        }
        // 寻找插件模块以及其对应的 jar 包
        String pluginFullId = container.getPluginFullIdByIndex(pluginSeq);
        PluginInstance pluginInstance = container.getPluginInstanceByFullId(pluginFullId);
        List<File> possibleJarFiles = findSpecifiedPlugin(pluginFullId);
        if (possibleJarFiles.isEmpty()) {
            eventContext.getChatSession().response(
                    "没有找到插件 " + pluginInstance.getPluginName() + " 对应的 jar 包，已退出重载流程。\n" +
                    "请检查插件 jar 包是否正确放置到插件目录下。");
            return;
        } else if (possibleJarFiles.size() > 1) {
            eventContext.getChatSession().response("找到多个插件 " + pluginInstance.getPluginName() + " 可能对应的 jar 包，已退出重载流程。\n" +
                    "请确保插件目录下 jar 包名称不冲突。");
            return;
        }
        // 重载插件
        File jarFile = possibleJarFiles.get(0);
        PluginReloadProcessor pluginReloadProcessor = new PluginReloadProcessor();
        pluginReloadProcessor.reloadPlugin(jarFile);
        eventContext.getChatSession().response("已重载插件 " + pluginInstance.getPluginName());
    }

    /**
     * 寻找指定插件可能存在的 jar 包
     * @param pluginFullId 插件全 ID
     * @return 插件 jar 包
     */
    public List<File> findSpecifiedPlugin(String pluginFullId) {
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        PluginModuleInstance pluginModuleInstance = container.getPluginModuleByPluginFullId(pluginFullId);
        String moduleId = pluginModuleInstance.getModuleId();
        String jarFileName = pluginModuleInstance.getJarFileName();
        String appPluginDirectoryPath = PluginManageUtil.getAppPluginDirectoryPath();

        File pluginDir = new File(appPluginDirectoryPath);
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            log.warn("插件目录不存在: {}", appPluginDirectoryPath);
            return new ArrayList<>();
        }

        Path rootPath = Paths.get(appPluginDirectoryPath);

        try (Stream<Path> walkStream = Files.walk(rootPath)) {  // 使用 try-with-resources 确保 Stream 资源被正确关闭
            return walkStream.filter(Files::isRegularFile) // 确保是文件，不是目录
                    .filter(p -> p.toString().toLowerCase().endsWith(".jar"))  // 只保留 .jar 文件
                    .map(Path::toFile)  // 转换为 File 对象
                    .filter(file -> file.getName().equals(jarFileName)
                            || file.getName().startsWith(moduleId))  // 筛选插件缓存的 jar 包名称，或者以模块 ID 开头的 jar 包名称
                    .toList();
        } catch (IOException e) {
            log.error("遍历目录时发生错误: {}", appPluginDirectoryPath, e);
            return List.of();
        }
    }

    public void reloadAllPlugins(YuniMessageEvent eventContext, PluginManage pluginManage) {
        PluginReloadProcessor pluginReloadProcessor = new PluginReloadProcessor();
        pluginReloadProcessor.reloadAllPlugins();
        eventContext.getChatSession().response("已重载所有插件");
    }
}
