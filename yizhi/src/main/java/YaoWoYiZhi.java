
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import com.yuier.yuni.core.enums.CommandArgRequireType;
import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.core.model.message.segment.ImageSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.plugin.model.YuniPlugin;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

/**
 * @Title: YaoWoYiZhi
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/6 6:29
 * @description: 要我一直 x 吗
 */

@Slf4j
public class YaoWoYiZhi extends CommandPlugin {

    private static final String YI_ZHI = "一直";
    private static final String TARGET_IMAGE = "图片";

    private static final String 要我一直 = "要我一直";
    private static final String 吗 = "吗";
    private static final String[] supportImageEndName = new String[] {"jpg", "png", "jpeg"};

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create(YI_ZHI)
                .addRequiredArg(TARGET_IMAGE, "要我一直", CommandArgRequireType.IMAGE)
                .build());
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        ImageSegment targetImage = (ImageSegment) eventContext.getCommandMatched().getArgValue(TARGET_IMAGE);
        String imageFileName = targetImage.getFile();
        String imageFileUrl = targetImage.getUrl();
        String imageBase64 = null;

        // 根据图片后缀判断图片类型
        String imageEndName = getImageEndName(imageFileName.toLowerCase());
        if ("gif".equals(imageEndName)) {
            imageBase64 = processGifFromUrl(imageFileUrl);
        } else if (Arrays.asList(supportImageEndName).contains(imageEndName)) {
            imageBase64 = processStaticImageFromUrl(imageFileUrl);
        } else {
            eventContext.getChatSession().response("不支持的图片格式: " + imageEndName);
            return;
        }
        eventContext.getChatSession().response(new MessageChain(new ImageSegment().setFile("base64://" + imageBase64)));
    }

    private String getImageEndName(String lowerName) {
        String[] split = lowerName.split("\\.");
        return split[split.length - 1];
    }

    /**
     * 处理gif图片
     * @param imageFileUrl GIF 解码器
     * @return 图片Base64
     */
    private String processGifFromUrl(String imageFileUrl) {
        GifDecoder decoder = new GifDecoder();
        try (InputStream stream = new URL(imageFileUrl).openStream(); BufferedInputStream buffered = new BufferedInputStream(stream)) {
            // 标记流以便重置
            buffered.mark(Integer.MAX_VALUE);
            int status = decoder.read(buffered);
            if (status != GifDecoder.STATUS_OK || decoder.getFrameCount() <= 0) {
                throw new RuntimeException("GIF 解码失败");
            }

            int frameCount = decoder.getFrameCount();
            // 处理第一帧以获取输出格式
            BufferedImage firstFrame = decoder.getFrame(0);
            BufferedImage sampleFrame = processSingleFrame(firstFrame);
            // 创建输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(outputStream);
            encoder.setRepeat(decoder.getLoopCount());

            // 设置输出质量
            encoder.setQuality(10); // 1-10，10为最高质量

            // 批量处理所有帧
            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = decoder.getFrame(i);
                int delay = decoder.getDelay(i);

                // 优化内存使用：处理完成后立即释放原帧
                BufferedImage processedFrame = processSingleFrame(frame);

                encoder.setDelay(Math.max(delay, 10)); // 最小延迟10ms避免播放过快
                encoder.addFrame(processedFrame);

                // 帮助GC
                frame.flush();
                processedFrame.flush();
            }

            encoder.finish();

            byte[] gifBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(gifBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理静态图片
     * @param imageFileUrl 图片 URL
     * @return 图片 Base64
     */
    private String processStaticImageFromUrl(String imageFileUrl) {
        // 下载图片
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(new URL(imageFileUrl));
        } catch (IOException e) {
            log.error("下载 QQ 图片失败: ");
            e.printStackTrace();
            return null;
        }

        // 处理单帧图片
        BufferedImage resultImage = processSingleFrame(originalImage);

        // 转换为Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(resultImage, "jpg", baos);
        } catch (IOException e) {
            log.error("图片转换失败");
            e.printStackTrace();
        }
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private BufferedImage processSingleFrame(BufferedImage originalImage) {
        // 统一新图片尺寸
        int newMainImgWidth = 600;  // 新主图宽度统一为 600px
        int newMainImgHeight = (int) (originalImage.getHeight() * 600.0 / originalImage.getWidth());  // 等比例计算新主图高度

        // 画布高度计算方法 1: 等比例扩大原始高度，系数 700.0 ，并添加 30px 预留高度
        int canvasHeightByProportion = (int) (originalImage.getHeight() * 700.0 / originalImage.getWidth()) + 30;
        // 画布高度计算方法 2: 直接将新主图高度 + 150px
        int canvasHeightByAbsolute = newMainImgHeight + 150;
        // 画布高度取两种计算方法的大值
        int canvasHeight = Math.max(canvasHeightByProportion, canvasHeightByAbsolute);

        // 制作画布，宽度为新主图宽度，高度为画布高度，背景为白色
        BufferedImage resultImage = new BufferedImage(newMainImgWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        // 使用 Graphics2D 绘画
        Graphics2D g2d = resultImage.createGraphics();
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 绘制白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newMainImgWidth, canvasHeight);
        // 调整原始图片大小为新主图
        BufferedImage resizedImage = resizeImage(originalImage, newMainImgWidth, newMainImgHeight);

        // 在画布左上角绘制调整后的原始图片
        g2d.drawImage(resizedImage, 0, 0, null);

        // 创建小图（右下角的小图）：宽度 100，高度为大图高度的 1/6
        int smallImageWidth = 100;
        int smallImageHeight = (int) (newMainImgHeight / 6.0);  // 小图高度是大图高度的 1/6
        BufferedImage smallImage = resizeImage(originalImage, smallImageWidth, smallImageHeight);

        // 计算小图位置
        int smallImageX = 400;  // X坐标固定在400
        int smallImageY = newMainImgHeight + (int)((canvasHeight - newMainImgHeight - smallImageHeight) / 2.0);  // 小图在底部空白处垂直居中

        // 绘制小图
        g2d.drawImage(smallImage, smallImageX, smallImageY, null);

        // 设置字体
        Font font = PluginUtils.loadFontFromPlugin("static/font/HarmonyOS_SansSC_Medium.ttf", 100, YaoWoYiZhi.class);

        g2d.setFont(font);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setColor(Color.BLACK);

        // 绘制文字 - 使用精确的位置计算
        int height = g2d.getFontMetrics().getAscent() + g2d.getFontMetrics().getDescent();
        int textY = newMainImgHeight + height;

        // 写左半边字，x 为 0
        g2d.drawString(要我一直, 0, textY);
        // 计算吗的宽度
        int word2Width = g2d.getFontMetrics().stringWidth(吗);
        // 写右半边字，x 为 600 - 吗的宽度
        g2d.drawString(吗, 600 - word2Width, textY);

        // 释放资源
        g2d.dispose();
        return resultImage;
    }

    /**
     * resizeImage方法思路：
     * 1. 创建目标尺寸的新BufferedImage对象
     * 2. 获取Graphics2D绘图上下文
     * 3. 设置高质量渲染提示（插值算法、渲染质量、抗锯齿）
     * 4. 使用drawImage方法将原图绘制到新图上，同时完成尺寸调整
     * 5. 释放绘图资源
     *
     * 渲染提示的作用：
     * - KEY_INTERPOLATION: 使用双线性插值算法，使缩放后的图片更平滑
     * - KEY_RENDERING: 优先考虑渲染质量而非速度
     * - KEY_ANTIALIASING: 启用抗锯齿，使边缘更平滑
     * @param originalImage 原始图片
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的图片
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // 创建目标尺寸的新图片对象
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

        // 获取绘图上下文
        Graphics2D g2d = resizedImage.createGraphics();

        // 设置高质量渲染提示，提升缩放后图片质量
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 将原图绘制到新图上，同时完成尺寸调整
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);

        // 释放绘图资源
        g2d.dispose();

        return resizedImage;
    }
}
