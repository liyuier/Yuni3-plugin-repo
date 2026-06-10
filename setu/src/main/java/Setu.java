import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.core.model.message.segment.ImageSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import entity.ApiResponseSetu;
import entity.LoliconApiResponse;

import java.util.HashMap;

/**
 * @Title: Setu
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/5 13:46
 * @description: 色图模块
 */

public class Setu extends CommandPlugin {

    private static final String LOLICON_API_V2 = "https://api.lolicon.app/setu/v2";

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create("色图").build());
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("r18", 0);  // 0 为非 R18，QQ 群还是要稳一手
        params.put("size", "regular");  // 图片大小为 regular ，比最大的 original 小一号
        LoliconApiResponse loliconApiResponse = PluginUtils.simplePost(LOLICON_API_V2, params, LoliconApiResponse.class);
        if (loliconApiResponse.getError() != null && !loliconApiResponse.getError().isEmpty()) {
            eventContext.getChatSession().response("Lolicon Api 调用失败，报错信息: " + loliconApiResponse.getError());
            return;
        }
        if (loliconApiResponse.getData() == null || loliconApiResponse.getData().isEmpty()) {
            eventContext.getChatSession().response("Lolicon Api 返回了空数据。");
            return;
        }
        ApiResponseSetu oneSetu = loliconApiResponse.getData().get(0);
        long pid = oneSetu.getPid();
        String title = oneSetu.getTitle();
        String author = oneSetu.getAuthor();
        String imgUrl = oneSetu.getUrls().getRegular();
        if (imgUrl == null || imgUrl.isEmpty()) {
            eventContext.getChatSession().response("Lolicon Api 获取图片地址失败。");
            return;
        }
        eventContext.getChatSession().response(new MessageChain(
                "pid: " + pid + "\n" +
                "title: " + title + "\n" +
                "author: " + author + "\n"
        ).addSegment(new ImageSegment().setFile(imgUrl)));
    }
}
