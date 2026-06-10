import com.yuier.yuni.core.enums.UserPermission;
import com.yuier.yuni.core.model.message.segment.TextSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.permission.manage.UserPermissionManager;
import com.yuier.yuni.plugin.manage.PluginContainer;
import com.yuier.yuni.plugin.manage.PluginManager;
import com.yuier.yuni.plugin.manage.enable.event.PluginDisableEvent;
import com.yuier.yuni.plugin.manage.enable.event.PluginEnableEvent;
import com.yuier.yuni.plugin.model.PluginInstance;
import com.yuier.yuni.plugin.model.YuniPlugin;
import com.yuier.yuni.plugin.model.active.ActivePluginInstance;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static util.PluginManagerConstants.PLUGIN_MANAGE_DISABLE;
import static util.PluginManagerConstants.PLUGIN_MANAGE_ENABLE;

/**
 * @Title: PluginEnable
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 1:17
 * @description: 插件使能
 */

@Slf4j
@NoArgsConstructor
public class PluginEnable {


    public void enablePlugin(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 先判断插件序号是否越界
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        TextSegment pluginSeqSegment = (TextSegment) commandMatched.getOptionRequiredArgValue(PLUGIN_MANAGE_ENABLE);
        int pluginSeq = Integer.parseInt(pluginSeqSegment.getText());
        if (pluginSeq <= 0 || pluginSeq > container.getPluginCount()) {
            eventContext.getChatSession().response("没有序号为" + pluginSeq + "的插件。");
            return;
        }
        // 获取目标插件 ID 与插件 实例
        String pluginFullId = container.getPluginFullIdByIndex(pluginSeq);
        PluginInstance pluginInstance = container.getPluginInstanceByFullId(pluginFullId);
        // 检查是否内置插件
        UserPermissionManager permissionManager = PluginUtils.getBean(UserPermissionManager.class);
        UserPermission userPermission = permissionManager.getUserPermission(eventContext, pluginFullId);
        if (pluginInstance.isBuiltIn() &&  userPermission.getPriority() < UserPermission.MASTER.getPriority()) {
            eventContext.getChatSession().response(pluginSeq + " 号插件为内置插件，只有 bot 拥有者有权开启或关闭");
            return;
        }
        // TODO 提醒一下
        if (pluginInstance instanceof ActivePluginInstance) {
            eventContext.getChatSession().response("主动类插件暂不完全支持配置开启 / 关闭，正在加紧适配中。。。");
        }

        // 调用一下插件本身的 enable 方法
        YuniPlugin plugin = pluginInstance.getPlugin();
        plugin.enable(new PluginEnableEvent(eventContext.getGroupId(), eventContext.getUserId()));

        // 调用插件管理器的 enable 方法
        PluginManager pluginManager = PluginUtils.getBean(PluginManager.class);
        pluginManager.enablePlugin(eventContext, pluginFullId);

        eventContext.getChatSession().response("已开启 " + pluginSeq + " 号插件 " + pluginInstance.getPluginName());
    }

    public void disablePlugin(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 先判断插件序号是否越界
        TextSegment pluginSeqSegment = (TextSegment) commandMatched.getOptionRequiredArgValue(PLUGIN_MANAGE_DISABLE);
        int pluginSeq = Integer.parseInt(pluginSeqSegment.getText());
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        if (pluginSeq <= 0 || pluginSeq > container.getPluginCount()) {
            eventContext.getChatSession().response("没有序号为" + pluginSeq + "的插件。");
            return;
        }
        // 获取目标插件 ID 与实例
        String pluginFullId = container.getPluginFullIdByIndex(pluginSeq);
        PluginInstance pluginInstance = container.getPluginInstanceByFullId(pluginFullId);
        // 插件管理插件无法被禁用
        String pluginManageFullId = container.getPluginFullIdByPluginClass(PluginManage.class);
        if (pluginFullId.equals(pluginManageFullId)) {
            eventContext.getChatSession().response("插件管理插件无法被禁用。");
            return;
        }
        // 检查是否内置插件
        UserPermissionManager permissionManager = PluginUtils.getBean(UserPermissionManager.class);
        UserPermission userPermission = permissionManager.getUserPermission(eventContext, pluginFullId);
        if (pluginInstance.isBuiltIn() &&  userPermission.getPriority() < UserPermission.MASTER.getPriority()) {
            eventContext.getChatSession().response(pluginSeq + " 号插件为内置插件，只有 bot 拥有者有权开启或关闭");
            return;
        }
        // TODO 提醒一下
        if (pluginInstance instanceof ActivePluginInstance) {
            eventContext.getChatSession().response("主动类插件暂不完全支持配置开启 / 关闭，正在加紧适配中。。。");
        }

        // 调用插件本身的 disable 方法
        YuniPlugin plugin = pluginInstance.getPlugin();
        plugin.disable(new PluginDisableEvent(eventContext.getGroupId(), eventContext.getUserId()));

        // 调用插件管理器的 disable 方法
        PluginManager pluginManager = PluginUtils.getBean(PluginManager.class);
        pluginManager.disablePlugin(eventContext, pluginFullId);

        eventContext.getChatSession().response("已关闭 " + pluginSeq + " 号插件 " + pluginInstance.getPluginName());
    }
}
