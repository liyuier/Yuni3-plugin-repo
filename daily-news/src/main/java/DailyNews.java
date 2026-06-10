import com.yuier.yuni.core.bot.BotGroupInfo;
import com.yuier.yuni.core.bot.YuniBot;
import com.yuier.yuni.core.model.message.MessageChain;
import com.yuier.yuni.core.model.message.segment.ImageSegment;
import com.yuier.yuni.core.util.CronExpressionBuilder;
import com.yuier.yuni.plugin.manage.enable.event.PluginDisableEvent;
import com.yuier.yuni.plugin.manage.enable.event.PluginEnableEvent;
import com.yuier.yuni.plugin.model.active.Action;
import com.yuier.yuni.plugin.model.active.scheduled.ScheduledPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import db.DBHelper;
import db.DailyNewsGroupEnable;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Title: DailyNews
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/2 0:41
 * @description: 每日早报
 */

@Slf4j
public class DailyNews extends ScheduledPlugin {

    @Override
    public Action getAction() {
        return () -> {
            // 先获取每日早报内容
            DailyNewsResponse dailyNewsResponse = PluginUtils.simplePost("https://api.2xb.cn/zaob", null, DailyNewsResponse.class);
            // 判断是否当天的日报
            String todayFormatStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!todayFormatStr.equals(dailyNewsResponse.getDatatime())) {
                log.info("接口响应日期为 {}, 非本日日报，不发送", dailyNewsResponse.getDatatime());
                return;
            }
            MessageChain dailyNewsMessageChain = new MessageChain(new ImageSegment().setFile(dailyNewsResponse.getImageUrl()));
            // 先获取群列表
            YuniBot bot  = PluginUtils.getYuniBot();
            List<BotGroupInfo> groupList = bot.getGroupList().orElse(List.of());
            List<Long> groupIdList = groupList.stream()
                    .map(BotGroupInfo::getGroupId)
                    .toList();
            // 再根据策略发送
            DailyNewsStrategyConfig strategyConfig = PluginUtils.loadJsonConfigFromPlugin("daily_news_strategy.json", DailyNewsStrategyConfig.class, this.getClass());
            if ("blacklist".equals(strategyConfig.getStrategy())) {
                List<DailyNewsGroupEnable> blackLists = DBHelper.findBlackLists();
                groupIdList.forEach(groupId -> {
                    if (blackLists.stream().noneMatch(blackGroup -> blackGroup.getGroupId().equals(groupId))) {
                        // 发送群消息
                        PluginUtils.sendGroupMessage(groupId, dailyNewsMessageChain);
                    }
                });
            } else if ("whitelist".equals(strategyConfig.getStrategy())) {
                List<DailyNewsGroupEnable> whiteLists = DBHelper.findWhiteLists();
                groupIdList.forEach(groupId -> {
                    if (whiteLists.stream().anyMatch(whiteGroup -> whiteGroup.getGroupId().equals(groupId))) {
                        // 发送群消息
                        PluginUtils.sendGroupMessage(groupId, dailyNewsMessageChain);
                    }
                });
            }
        };
    }

    @Override
    public String cronExpression() {
        return CronExpressionBuilder.create()
                .dailyAt(8, 30).build();
    }

    @Override
    public void enable(PluginEnableEvent event) {

    }

    @Override
    public void disable(PluginDisableEvent event) {

    }

    @Override
    public void initialize() {
        // 建表
        DBHelper.createJdbi();
    }
}
