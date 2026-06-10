package util;

import com.microsoft.playwright.*;
import com.yuier.yuni.core.util.RedisUtil;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.plugin.manage.PluginContainer;
import com.yuier.yuni.plugin.manage.PluginManager;
import com.yuier.yuni.plugin.manage.enable.PluginEnableProcessor;
import com.yuier.yuni.plugin.model.PluginModuleInstance;
import com.yuier.yuni.plugin.model.PluginInstance;
import com.yuier.yuni.plugin.model.PluginMetadata;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.microsoft.playwright.options.WaitForSelectorState.VISIBLE;

/**
 * @Title: PluginManageUtil
 * @Author yuier
 * @Package util
 * @Date 2025/12/28 19:49
 * @description:
 */

@Slf4j
public class PluginManageUtil {

    public static final String PLUGIN_LIST_HASHCODE_CACHE_KEY = "plugin:list:hashcode:key";
    public static final String PLUGIN_LIST_IMAGE_CACHE_KEY = "plugin:list:image:key";

    public static final String PLUGIN_DETAIL_HASHCODE_CACHE_KEY = "plugin:detail:hashcode:key";
    public static final String PLUGIN_DETAIL_IMAGE_CACHE_KEY = "plugin:detail:image:key";

    public static Integer getPluginModulesHashCodeCache(YuniMessageEvent eventContext) {
        Map<String, Object> stringObjectMap = RedisUtil.hGetAll(PLUGIN_LIST_HASHCODE_CACHE_KEY);
        if (stringObjectMap != null) {
            return (Integer) stringObjectMap.get(eventContext.getPosition());
        }
        return null;
    }

    public static void savePluginListHashCodeCache(YuniMessageEvent eventContext, int hashCode) {
        RedisUtil.hSet(PLUGIN_LIST_HASHCODE_CACHE_KEY, eventContext.getPosition(), hashCode);
    }

    public static String getPluginListImageCache(YuniMessageEvent eventContext) {
        Map<String, Object> stringObjectMap = RedisUtil.hGetAll(PLUGIN_LIST_IMAGE_CACHE_KEY);
        if (stringObjectMap != null) {
            return (String) stringObjectMap.get(eventContext.getPosition());
        }
        return null;
    }

    public static void savePluginListImageCache(YuniMessageEvent eventContext, String image) {
        RedisUtil.hSet(PLUGIN_LIST_IMAGE_CACHE_KEY, eventContext.getPosition(), image);
    }

    public static Integer getPluginDetailHashCodeCache(YuniMessageEvent eventContext, String pluginId) {
        Map<String, Object> stringObjectMap = RedisUtil.hGetAll(PLUGIN_DETAIL_HASHCODE_CACHE_KEY);
        if (stringObjectMap != null) {
            return (Integer) stringObjectMap.get(buildPluginDetailCacheKey(eventContext, pluginId));
        }
        return null;
    }

    public static void savePluginDetailHashCodeCache(YuniMessageEvent eventContext, String pluginId, int hashCode) {
        RedisUtil.hSet(PLUGIN_DETAIL_HASHCODE_CACHE_KEY, buildPluginDetailCacheKey(eventContext, pluginId), hashCode);
    }

    public static String getPluginDetailImageCache(YuniMessageEvent eventContext, String pluginId) {
        Map<String, Object> stringObjectMap = RedisUtil.hGetAll(PLUGIN_DETAIL_IMAGE_CACHE_KEY);
        if (stringObjectMap != null) {
            return (String) stringObjectMap.get(buildPluginDetailCacheKey(eventContext, pluginId));
        }
        return null;
    }

    public static void savePluginDetailImageCache(YuniMessageEvent eventContext, String pluginId, String image) {
        RedisUtil.hSet(PLUGIN_DETAIL_IMAGE_CACHE_KEY, buildPluginDetailCacheKey(eventContext, pluginId), image);
    }
    
    private static String buildPluginDetailCacheKey(YuniMessageEvent eventContext, String pluginId) {
        return eventContext.getPosition() + ":" + pluginId;
    }

    public static Integer calculateHashCodeForShowingPluginDetail(YuniMessageEvent eventContext, String pluginId) {
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
        PluginInstance pluginInstance = container.getPluginInstanceByFullId(pluginId);
        return Objects.hash(
                pluginInstance.getIndex(),  // 插件序号
                pluginInstance.getPluginMetadata().getName(),  // 插件名称
                pluginInstance.getPluginMetadata().getDescription(),  // 插件描述
                pluginInstance.getPluginMetadata().getTips(),  // 插件提示
                pluginInstance.getPluginMetadata().getAuthor(),  // 插件作者
                pluginInstance.getPluginMetadata().getVersion(),  // 插件版本
                processor.isPluginEnabled(eventContext, pluginInstance)  // 插件使能情况
        );
    }


    /**
     * 获取消息发送位置下的插件列表的 hashCode
     * @param event 消息事件
     * @return 插件列表的 hashCode
     */
    public static int calculateHashCodeForShowingPluginList(YuniMessageEvent event) {
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        Map<String, PluginModuleInstance> pluginModules = container.getPluginModuleInstanceMap();
        int[] singleHashes = new int[pluginModules.size()];
        int i = 0;
        for (PluginModuleInstance moduleInstance : pluginModules.values()) {
            singleHashes[i] = getModuleInstanceHashCodeForShowingPluginList(moduleInstance, event);
            i++;
        }
        Arrays.sort(singleHashes);
        return Arrays.hashCode(singleHashes);
    }

    /**
     * 获取消息发送位置下，单个插件模块下的插件列表的 hashCode
     * @param moduleInstance 插件模块实例
     * @param event 消息事件
     * @return 插件列表的 hashCode
     */
    private static int getModuleInstanceHashCodeForShowingPluginList(PluginModuleInstance moduleInstance, YuniMessageEvent event) {
        PluginContainer container = PluginUtils.getBean(PluginContainer.class);
        List<PluginInstance> pluginInstances = container.getPluginInstanceListByModuleId(moduleInstance.getModuleId());
        int[] singleHashes = new int[pluginInstances.size()];
        for (int i = 0; i < pluginInstances.size(); i++) {
            PluginInstance pluginInstance = pluginInstances.get(i);
            singleHashes[i] = getPluginHashCodeForShowingPluginList(pluginInstance, event);
        };
        // 排序，消除顺序影响
        Arrays.sort(singleHashes);
        return Arrays.hashCode(singleHashes);
    }

    /**
     * 获取消息发送位置下，单个插件的 hashCode
     * @param pluginInstance 插件实例
     * @param event 消息事件
     * @return 插件的 hashCode
     */
    private static int getPluginHashCodeForShowingPluginList(PluginInstance pluginInstance, YuniMessageEvent event) {
        PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
        PluginMetadata pluginMetadata = pluginInstance.getPluginMetadata();
        return Objects.hash(
                pluginMetadata.getName(),  // 插件名称
                pluginMetadata.getDescription(),  // 插件描述
                processor.isPluginEnabled(event, pluginInstance)  // 插件使能情况
        );
    }

    /**
     * 渲染模板字符串为 HTML
     * @param templateString 模板字符串
     * @param context 模板上下文
     * @return 渲染后的 HTML 字符串
     */
    public static String renderToHtml(String templateString, Context context) {
        // 初始化模板引擎
        TemplateEngine templateEngine = new TemplateEngine();
        // 配置 StringTemplateResolver
        templateEngine.setTemplateResolver(new StringTemplateResolver());
        return templateEngine.process(templateString, context);
    }

    /**
     * 截屏 HTML 字符串并输出 Base64
     * @param htmlStr HTML 字符串
     * @param elementId 要截图的元素 id
     */
    public static String screenForHtmlStrToBase64(String htmlStr, String elementId) {
        String base64 = null;
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // 设置为true可在无头模式下运行
            Page page = browser.newPage();
            // 加载HTML字符串
            page.setContent(htmlStr);

            base64 = captureElementAsBase64(page, elementId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }

    /**
     * 截取指定元素并返回Base64编码的图片
     * @param page 页面对象
     * @param elementId 要截取的元素 ID
     * @return Base64编码的图片字符串，如果元素不存在则返回null
     */
    public static String captureElementAsBase64(Page page, String elementId) {
        try {
            Locator element = page.locator("#" + elementId);

            // 等待元素可见
            element.waitFor(new Locator.WaitForOptions().setState(VISIBLE));

            // 检查元素是否存在
            if (element.count() == 0) {
                log.info("找不到ID为 " + elementId + " 的元素");
                return null;
            }

            // 截取元素并获取字节数组
            byte[] screenshotBytes = element.screenshot();

            // 将字节数组转换为Base64
            String base64Image = Base64.getEncoder().encodeToString(screenshotBytes);

            log.info("成功截取元素 " + elementId);

            return base64Image;
        } catch (Exception e) {
            log.info("截取元素 " + elementId + " 时发生错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 截屏 HTML 字符串
     * @param htmlStr HTML 字符串
     * @param elementId 要截图的容器 id
     */
    public static void screenForHtmlStrAndSave(String htmlStr, String elementId, String outputFilePath) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // 设置为true可在无头模式下运行
            Page page = browser.newPage();
            // 加载HTML字符串
            page.setContent(htmlStr);

            screenInPage(page, elementId, outputFilePath);
            log.info("HTML字符串截图已保存为 {}", outputFilePath);
            // 关闭浏览器
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 截屏 HTML 文件
     * @param htmlFilePath HTML 文件路径
     * @param elementId 要截图的容器 id
     */
    public static void screenForHtmlFileAndSave(String htmlFilePath, String elementId, String outputFilePath) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)); // 设置为true可在无头模式下运行
            Page page = browser.newPage();
            page.navigate("file://" + Paths.get(htmlFilePath).toAbsolutePath());

            // 等待页面加载完成
            page.waitForLoadState();
            screenInPage(page, elementId, outputFilePath);
            log.info("HTML字符串截图已保存为 {}", outputFilePath);
            // 关闭浏览器
            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void screenInPage(Page page, String elementId, String outputFilePath) {
        Locator element = page.locator("#" + elementId);

        // 检查元素是否存在
        if (element.count() > 0) {
            // 等待元素可见且稳定（无动画、布局完成）
            element.waitFor(new Locator.WaitForOptions()
                    .setState(VISIBLE)
                    .setTimeout(1000)); // 超时 1 秒
            Path outputPath = Paths.get(outputFilePath).toAbsolutePath();
            // 创建所有必要的父目录
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 截取指定元素
            element.screenshot(new Locator.ScreenshotOptions()
                    .setPath(outputPath));
        } else {
            log.error("找不到ID为 " + elementId + " 的元素");
        }
    }

    public static String getAppPluginDirectoryPath() {
        PluginManager manager = PluginUtils.getBean(PluginManager.class);
        return manager.getPluginDirectoryPath();
    }
}
