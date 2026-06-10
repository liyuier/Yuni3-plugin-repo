import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.core.model.message.segment.ImageSegment;
import com.yuier.yuni.core.model.message.segment.TextSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.plugin.manage.PluginContainer;
import com.yuier.yuni.plugin.manage.enable.PluginEnableProcessor;
import com.yuier.yuni.plugin.model.PluginInstance;
import com.yuier.yuni.plugin.model.PluginModuleInstance;
import com.yuier.yuni.plugin.util.PluginUtils;
import entity.PluginDetail;
import entity.PluginListElement;
import entity.PluginListModule;
import entity.PluginListSinglePlugin;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import util.PluginManageUtil;

import java.util.ArrayList;
import java.util.List;

import static util.PluginManagerConstants.PLUGIN_MANAGE_VIEW;

/**
 * @Title: PluginShow
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 0:55
 * @description: 插件信息展示
 */

@Slf4j
@NoArgsConstructor
public class PluginShow {

    /**
     * 显示插件列表
     * @param eventContext  消息事件上下文
     * @param pluginManage  插件管理插件，用于一些需求插件本身的操作
     */
    public void showPluginList(YuniMessageEvent eventContext, PluginManage pluginManage) {
        // 取出缓存中，当前消息发送位置下的插件列表哈希，以及插件列表图片缓存
        Integer pluginModulesHashCode = PluginManageUtil.getPluginModulesHashCodeCache(eventContext);
        String pluginListImageCache = PluginManageUtil.getPluginListImageCache(eventContext);
        // 先计算当前消息发送位置下的插件列表的 hashCode 是否有变动
        int currPluginListHashCode = PluginManageUtil.calculateHashCodeForShowingPluginList(eventContext);
        if (pluginModulesHashCode != null && pluginModulesHashCode.equals(currPluginListHashCode)) {
            // 哈希没变，直接返回缓存图片
            if (pluginListImageCache != null) {
                eventContext.getChatSession().response(new MessageChain(
                        new ImageSegment().setFile(pluginListImageCache)
                ));
                return;
            }
        }
        /* 缓存图片不存在，或者插件列表的 hashCode 发生了变化，进入重新生成图片流程 */

        // 先保存一下最新哈希
        PluginManageUtil.savePluginListHashCodeCache(eventContext, currPluginListHashCode);
        // 获取模板字符串
        String templateFilePath = "templates/plugin-list.html";
        String templateStr = PluginUtils.loadTextFromPluginFolder(templateFilePath, pluginManage.getClass());
        Context context = new Context();
        // 组装数据
        List<PluginListElement> pluginListElements = assemblerPluginListData(eventContext);
        context.setVariable("pluginList", pluginListElements);
        // 渲染 HTML
        String htmlStr = PluginManageUtil.renderToHtml(templateStr, context);
        // TODO 这里应该做成可选本地图片还是 bas64。由于我的 OneBot 客户端与 Yuni 部署在不同机器上，所以返回 Base64
        String imgBase64 = PluginManageUtil.screenForHtmlStrToBase64(htmlStr, "result-container");
        eventContext.getChatSession().response(new MessageChain(
                new ImageSegment().setFile(buildImageBase64DataFileNameForCache(imgBase64))
        ));
        // 保存图片缓存
        PluginManageUtil.savePluginListImageCache(eventContext, buildImageBase64DataFileNameForCache(imgBase64));
    }

    private static String buildImageBase64DataFileNameForCache(String fileName) {
        return "base64://" + fileName;
    }

    /**
     * 组装插件列表数据
     * @param eventContext  消息事件
     * @return  组装后的插件列表数据
     */
    public List<PluginListElement> assemblerPluginListData(YuniMessageEvent eventContext) {
        // 准备
        ArrayList<PluginListElement> pluginListElements = new ArrayList<>();
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);

        // 以插件模块为单位组装数据
        for (String moduleId : container.getPluginModuleIds()) {
            // 取出插件模块数据，与模块下各个单独插件的数据
            PluginModuleInstance pluginModule = container.getPluginModuleInstanceByModuleId(moduleId);
            List<PluginInstance> pluginInstances = container.getPluginInstanceListByModuleId(moduleId);
            if (pluginInstances == null || pluginInstances.isEmpty()) {
                continue;
            }
            if (pluginInstances.size() == 1) {
                // 组装单个插件数据
                PluginInstance pluginInstance = pluginInstances.get(0);
                pluginListElements.add(new PluginListElement(new PluginListSinglePlugin(
                        pluginInstance.getIndex(),
                        pluginInstance.getPluginName(),
                        pluginInstance.getPluginDescription(),
                        processor.isPluginEnabled(eventContext, pluginInstance)

                )));
            } else {
                // 组装插件模块
                PluginListModule pluginListModule = new PluginListModule();
                pluginListModule.setModuleName(pluginModule.getModuleName());
                for (PluginInstance pluginInstance : pluginInstances) {
                    pluginListModule.addSubPlugin(new PluginListSinglePlugin(
                            pluginInstance.getIndex(),
                            pluginInstance.getPluginName(),
                            pluginInstance.getPluginDescription(),
                            processor.isPluginEnabled(eventContext, pluginInstance)
                    ));
                }
                pluginListElements.add(new PluginListElement(pluginListModule));
            }
        }
        return pluginListElements;
    }

    /**
     * 显示指定插件详情
     * @param eventContext  消息事件上下文
     * @param commandMatched  命令匹配结果
     * @param pluginManage  插件管理插件，用于一些需求插件本身的操作
     */
    public void showPluginDetail(YuniMessageEvent eventContext, CommandMatched commandMatched, PluginManage pluginManage) {
        // 先解析出要查看的插件序号
        TextSegment pluginSeqSegment = (TextSegment) commandMatched.getOptionOptionalArgValue(PLUGIN_MANAGE_VIEW);
        int pluginSeq = Integer.parseInt(pluginSeqSegment.getText());
        // 判断序号是否越界
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        if (pluginSeq <= 0 || pluginSeq > container.getPluginCount()) {
            eventContext.getChatSession().response("没有序号为" + pluginSeq + "的插件。");
            return;
        }
        // 取出缓存中消息发送位置下插件详情哈希与详情图片的缓存
        String pluginFullId = container.getPluginFullIdByIndex(pluginSeq);
        Integer pluginDetailHashCodeCache = PluginManageUtil.getPluginDetailHashCodeCache(eventContext, pluginFullId);
        String pluginDetailImageCache = PluginManageUtil.getPluginDetailImageCache(eventContext, pluginFullId);
        // 计算消息发送位置的插件实时哈希
        Integer currentPluginHash = PluginManageUtil.calculateHashCodeForShowingPluginDetail(eventContext,  pluginFullId);
        if (pluginDetailHashCodeCache != null && pluginDetailHashCodeCache.equals(currentPluginHash)) {
            // 哈希没变，直接返回缓存图片
            if (pluginDetailImageCache != null) {
                eventContext.getChatSession().response(new MessageChain(
                        new ImageSegment().setFile(pluginDetailImageCache)
                ));
                return;
            }
        }
        /* 缓存图片不存在，或者插件列表的 hashCode 发生了变化，进入重新生成图片流程 */

        // 先保存一下最新哈希
        PluginManageUtil.savePluginDetailHashCodeCache(eventContext, pluginFullId, currentPluginHash);
        // 获取模板字符串
        String templateFilePath = "templates/plugin-detail.html";
        String templateStr = PluginUtils.loadTextFromPluginFolder(templateFilePath, pluginManage.getClass());
        // 组装数据
        Context context = new Context();
        PluginEnableProcessor enableProcessor = PluginUtils.getBean(PluginEnableProcessor.class);
        PluginInstance pluginInstance = container.getPluginInstanceByFullId(pluginFullId);
        PluginDetail pluginDetail = new PluginDetail(
                pluginInstance.getIndex(),
                pluginInstance.getPluginName(),
                pluginInstance.getPluginDescription(),
                pluginInstance.getPluginMetadata().getTips(),
                pluginInstance.getPluginMetadata().getAuthor(),
                pluginInstance.getPluginMetadata().getVersion(),
                enableProcessor.isPluginEnabled(eventContext, pluginInstance)
        );
        context.setVariable("plugin", pluginDetail);
        // 渲染 HTML
        String htmlStr = PluginManageUtil.renderToHtml(templateStr, context);
        // TODO 同上
        String imgBase64 = PluginManageUtil.screenForHtmlStrToBase64(htmlStr, "result-container");
        eventContext.getChatSession().response(new MessageChain(
                new ImageSegment().setFile(buildImageBase64DataFileNameForCache(imgBase64))
        ));
        // 保存图片缓存
        PluginManageUtil.savePluginDetailImageCache(eventContext, pluginFullId, buildImageBase64DataFileNameForCache(imgBase64));
    }
}
