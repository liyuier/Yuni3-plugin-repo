package notice;

import com.yuier.yuni.core.event.notice.PokeEvent;
import com.yuier.yuni.core.event.notice.YuniNoticeEvent;
import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.event.detector.notice.DefaultYuniNoticeDetector;
import com.yuier.yuni.plugin.model.passive.notice.NoticePlugin;
import com.yuier.yuni.plugin.util.PluginUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: PokeNotice
 * @Author yuier
 * @Package notice
 * @Date 2026/1/1 16:37
 * @description: 群内戳一戳
 */

public class PokeNotice extends NoticePlugin {

    Map<Long, Map<Long, Integer>> pokeCountMap = new HashMap<>();
    String[] pokeMessages = new String[]{
            "戳我干嘛？",
            "别戳我！",
            "说了别戳我！",
            "再戳我要生气了！"
    };

    @Override
    public DefaultYuniNoticeDetector getDetector() {
        return new DefaultYuniNoticeDetector(event -> {
            if ("notify".equals(event.getNoticeType())) {
                PokeEvent noticeEvent = PluginUtils.deserialize(event.getRawJson(), PokeEvent.class);
                assert noticeEvent != null;
                if ("poke".equals(noticeEvent.getSubType())) {
                    return PluginUtils.deserialize(event.getRawJson(), PokeEvent.class);
                }
            }
            return null;
        });
    }

    @Override
    public void execute(YuniNoticeEvent eventContext) {
        PokeEvent event = (PokeEvent) eventContext;
        if (!event.getTargetId().equals(event.getSelfId())) {
            return;
        }
        int pokeCount = getPokeCount(event.getGroupId(), event.getUserId());
        PluginUtils.sendGroupMessage(event.getGroupId(), new MessageChain()
                .addAt(event.getUserId())
                .addTextSegment(pokeMessages[pokeCount]));
        pokeCount++;
        if (pokeCount >= pokeMessages.length) {
            pokeCount = 0;
        }
        setPokeCount(event.getGroupId(), event.getUserId(), pokeCount);
    }

    private int getPokeCount(long groupId, long userId) {
        Map<Long, Integer> groupPokeCount = pokeCountMap.getOrDefault(groupId, new HashMap<>());
        return groupPokeCount.getOrDefault(userId, 0);
    }

    private void setPokeCount(long groupId, long userId, int count) {
        Map<Long, Integer> groupPokeCount = pokeCountMap.getOrDefault(groupId, new HashMap<>());
        groupPokeCount.put(userId, count);
        pokeCountMap.put(groupId, groupPokeCount);
    }
}
