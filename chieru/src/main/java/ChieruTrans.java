import com.yuier.yuni.core.model.message.segment.TextSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import util.TransMap;
import util.TransferUtil;

import java.util.List;

import static com.yuier.yuni.core.enums.CommandArgRequireType.PLAIN;

/**
 * @Title: ChieruTrans
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 15:21
 * @description: 切噜语转换
 */

@Slf4j
public class ChieruTrans extends CommandPlugin {



    public static final String CHIERU_TRANS = "切噜";
    public static final String CHIERU_TRANS_ENCODED_OPTION = "转换";

    public static final String CHIERU_TRANS_DECODED_OPTION = "还原";

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create(CHIERU_TRANS)
                .addOptionWithRequiredArg(CHIERU_TRANS_ENCODED_OPTION, "raw_words_of_chieru", "切噜语编码", PLAIN)
                .addOptionWithRequiredArg(CHIERU_TRANS_DECODED_OPTION, "chieru_words", "切噜语解码", PLAIN)
                .build());
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandMatched commandMatched = eventContext.getCommandMatched();
        if (commandMatched.hasOption(CHIERU_TRANS_ENCODED_OPTION)) {  // 切噜编码指令
            transferToChieru(eventContext, commandMatched);
        }
        if (commandMatched.hasOption(CHIERU_TRANS_DECODED_OPTION)) {  // 切噜解码指令
            transferToPlain(eventContext, commandMatched);
        }

    }

    /**
     * 将明文转换为切噜语
     * @param eventContext  消息上下文
     * @param commandMatched  指令匹配器
     */
    private void transferToChieru(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 获取明文
        TextSegment chieruRawWordSegment = (TextSegment) commandMatched.getOptionRequiredArgValue(CHIERU_TRANS_ENCODED_OPTION);
        String chieruRawWords = chieruRawWordSegment.getText();
        // 获取映射表，翻译为切噜语言
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> chieruMap = transMap.getChieru();
        String chieruCode = TransferUtil.encode(chieruRawWords, chieruMap);
        // 开头加一个 "切"
        eventContext.getChatSession().response(chieruMap.get(0) + chieruCode);
    }

    /**
     * 将切噜语转换为明文
     * @param eventContext  消息上下文
     * @param commandMatched  指令匹配器
     */
    private void transferToPlain(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 获取明文
        TextSegment chieruWordSegment = (TextSegment) commandMatched.getOptionRequiredArgValue(CHIERU_TRANS_DECODED_OPTION);
        String chieruWords = chieruWordSegment.getText();
        // 获取映射表
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> chieruMap = transMap.getChieru();
        // 检查开头是否有切噜语标识
        if (!chieruWords.startsWith(chieruMap.get(0))) {
            eventContext.getChatSession().response("非法切噜语文本，开头缺少标识符 " + chieruMap.get(0));
            return;
        }
        // 去掉开头标识符，进行解码
        try {
            String rawWords = TransferUtil.decode(chieruWords.substring(chieruMap.get(0).length()), chieruMap);
            eventContext.getChatSession().response(rawWords);
        } catch (IllegalArgumentException e) {
            eventContext.getChatSession().response("非法切噜语文本，含有未定义的字符");
        }
    }
}