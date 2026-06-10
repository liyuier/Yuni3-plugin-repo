import com.yuier.yuni.core.enums.CommandArgRequireType;
import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.core.model.message.segment.AtSegment;
import com.yuier.yuni.core.model.message.segment.ImageSegment;
import com.yuier.yuni.core.util.RedisUtil;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import entity.PigImageList;
import entity.PigImageListElement;
import entity.PigTodayCache;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Title: PigToday
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/3 12:35
 * @description: 今日猪猪
 */

public class PigToday extends CommandPlugin {

    private static final String PIG_TODAY = "今日猪猪";
    private static final String TARGET_MEMBER = "目标成员";

    private static final String PIGHUB_API_ALL_IMAGES = "https://pighub.top/api/all-images";
    private static final String PIGHUB_IMAGE_BASE_URL = "https://pighub.top/data/";

    private static final String PIGTODAY_CACHE_KEY = "plugin:pigtoday:key";

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create(PIG_TODAY)
                .addOptionalArg(TARGET_MEMBER, "猪猪名称", CommandArgRequireType.AT)
                .build());
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandMatched commandMatched = eventContext.getCommandMatched();
        if (commandMatched.hasArg(TARGET_MEMBER)) {
            getMemberPigToday(eventContext, commandMatched);
        } else {
            getSenderPigToday(eventContext);
        }
    }

    private void getMemberPigToday(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        AtSegment atSegment = (AtSegment) commandMatched.getArgValue(TARGET_MEMBER);
        String targetQqStr = atSegment.getQq();
        if (targetQqStr.equals("all")) {
            eventContext.getChatSession().reply("请指定具体群友");
        }
        Long targetQq = Long.parseLong(targetQqStr);
        PigTodayCache pigToday = getPigToday(targetQq);
        String targetMemberName = PluginUtils.getGroupMemberName(eventContext.getGroupId(), targetQq);
        eventContext.getChatSession().reply(new MessageChain(targetMemberName + " 今天是: \n" + pigToday.getImageTitle() + "\n")
                .addSegment(new ImageSegment().setFile(PIGHUB_IMAGE_BASE_URL + pigToday.getImageName())));
    }

    private void getSenderPigToday(YuniMessageEvent eventContext) {
        PigTodayCache pigToday = getPigToday(eventContext.getUserId());
        eventContext.getChatSession().reply(new MessageChain("你今天是: \n" + pigToday.getImageTitle() + "\n")
                .addSegment(new ImageSegment().setFile(PIGHUB_IMAGE_BASE_URL + pigToday.getImageName())));
    }

    // 获取猪猪
    private PigTodayCache getPigToday(Long targetQq) {
        String targetQqStr = String.valueOf(targetQq);
        // 先去缓存里看一看有没有
        if (RedisUtil.exists(PIGTODAY_CACHE_KEY)) {
            Map<String, String> pigTodayMap = (Map<String, String>) RedisUtil.get(PIGTODAY_CACHE_KEY);
            if (pigTodayMap != null && pigTodayMap.containsKey(targetQqStr)) {
                PigTodayCache pigTodayCache = PluginUtils.deserialize(pigTodayMap.get(targetQqStr), PigTodayCache.class);
                assert pigTodayCache != null;
                if (LocalDate.now().toString().equals(pigTodayCache.getDate())) {
                    return pigTodayCache;
                }
            }
        } else {
            // 如果缓存里没有 Map 的键，先建立一下
            RedisUtil.set(PIGTODAY_CACHE_KEY, new HashMap<String, String>());
        }
        // 缓存里没有，去接口里请求
        PigImageList pigImageList = PluginUtils.simpleGet(PIGHUB_API_ALL_IMAGES, PigImageList.class);
        Random random = new Random();
        PigImageListElement pigImageData = pigImageList.getImages().get(random.nextInt(pigImageList.getTotal()));
        // 缓存起来
        Map<String, String> pigTodayMap = (Map<String, String>) RedisUtil.get(PIGTODAY_CACHE_KEY);
        PigTodayCache pigTodayCache = new PigTodayCache(
                pigImageData.getTitle(),
                pigImageData.getFilename(),
                LocalDate.now().toString()
        );
        pigTodayMap.put(targetQqStr, PluginUtils.serialize(pigTodayCache));
        RedisUtil.set(PIGTODAY_CACHE_KEY, pigTodayMap);
        return pigTodayCache;
    }
}
