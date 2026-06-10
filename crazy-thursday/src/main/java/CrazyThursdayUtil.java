import com.yuier.yuni.core.bot.BotGroupInfo;
import com.yuier.yuni.core.bot.MessageTarget;
import com.yuier.yuni.core.bot.YuniBot;
import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.plugin.manage.enable.PluginEnableProcessor;
import com.yuier.yuni.plugin.model.YuniPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Title: CrazyThursdayUtil
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/2/9 14:30
 * @description: CrazyThursdayUtil
 */

@Slf4j
public class CrazyThursdayUtil {

    public static void 发送疯狂星期四文案(YuniPlugin plugin) {
        // 每个时间点按照 0.85 的概率判断是否发送
        if (!PluginUtils.checkHitProbability(0.85f)) {
            log.info("[CrazyThursdayUtil] 未命中概率，本次不发送消息");
            return;
        }
        // 挑选文案
        String[] 候选文案 = PluginUtils.loadJsonConfigFromPlugin("sentences.json", String[].class, plugin.getClass());
        String 本次要发送的文案 = PluginUtils.getRandomElement(Arrays.asList(候选文案));
        // 获取群列表
        YuniBot bot  = PluginUtils.getYuniBot();
        List<BotGroupInfo> groupList = bot.getGroupList().orElse(List.of());
        // 获取群列表中的群组
        for (BotGroupInfo groupListElement : groupList) {
            Long groupId = groupListElement.getGroupId();
            PluginEnableProcessor processor = PluginUtils.getBean(PluginEnableProcessor.class);
            // 判断该群组是否启用当前功能
            if (!processor.isPluginEnabled(groupId, CrazyThursday.class)) {
                continue;
            }
            // 发送文案
            bot.sendMessage(MessageTarget.group(groupId), new MessageChain(本次要发送的文案));
        }
    }

    public static List<Long> 计算等待间隔(List<Long> 发送文案的时间点, Long 开始时间) {
        List<Long> timeIntervals = new ArrayList<>();
        if (发送文案的时间点.isEmpty()) {
            return timeIntervals;
        }
        Long realStartTime = -1L;
        int startIndex = -1;
        for (int i = 0; i < 发送文案的时间点.size(); i ++) {
            if (发送文案的时间点.get(i) < 开始时间) {
                continue;
            }
            realStartTime = 发送文案的时间点.get(i);
            startIndex = i;
            break;
        }
        timeIntervals.add(realStartTime - 开始时间);
        for (int i = startIndex + 1; i < 发送文案的时间点.size(); i ++) {
            timeIntervals.add(发送文案的时间点.get(i) - 发送文案的时间点.get(i - 1));
        }
        return timeIntervals;
    }
}
