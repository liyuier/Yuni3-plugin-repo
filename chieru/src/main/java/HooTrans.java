import com.yuier.yuni.core.model.message.segment.TextSegment;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.core.event.matched.CommandMatched;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import util.TransMap;
import util.TransferUtil;

import java.util.List;

import static com.yuier.yuni.core.enums.CommandArgRequireType.PLAIN;

/**
 * @Title: HooTrans
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 17:56
 * @description: 齁哦哦转换
 */

public class HooTrans extends CommandPlugin {

    public static final String HOO_TRANS = "齁哦";
    public static final String HOO_TRANS_ENCODED_OPTION = "转换";

    public static final String HOO_TRANS_DECODED_OPTION = "还原";

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create(HOO_TRANS)
                .addOptionWithRequiredArg(HOO_TRANS_ENCODED_OPTION, "raw_words_of_hoo", "齁哦语编码", PLAIN)
                .addOptionWithRequiredArg(HOO_TRANS_DECODED_OPTION, "hoo_words", "齁哦语解码", PLAIN)
                .build());
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandMatched commandMatched = eventContext.getCommandMatched();
        if (commandMatched.hasOption(HOO_TRANS_ENCODED_OPTION)) {  // 齁哦语编码指令
            transferToHoo(eventContext, commandMatched);
        }
        if (commandMatched.hasOption(HOO_TRANS_DECODED_OPTION)) {  // 齁哦语解码指令
            transferToPlain(eventContext, commandMatched);
        }
    }

    private void transferToHoo(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 获取明文
        TextSegment hooRawWordSegment = (TextSegment) commandMatched.getOptionRequiredArgValue(HOO_TRANS_ENCODED_OPTION);
        String hooRawWords = hooRawWordSegment.getText();
        // 获取映射表，翻译为齁哦语言
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> hooMap = transMap.getHoo();
        String hooCode = TransferUtil.encode(hooRawWords, hooMap);
        // 开头加一个 "齁"
        eventContext.getChatSession().response(hooMap.get(0) + hooCode);
    }

    private void transferToPlain(YuniMessageEvent eventContext, CommandMatched commandMatched) {
        // 获取明文
        TextSegment hooWordSegment = (TextSegment) commandMatched.getOptionRequiredArgValue(HOO_TRANS_DECODED_OPTION);
        String hooWords = hooWordSegment.getText();
        // 获取映射表
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> hooMap = transMap.getHoo();
        // 检查开头是否有齁哦语标识
        if (!hooWords.startsWith(hooMap.get(0))) {
            eventContext.getChatSession().response("非法齁哦语文本，开头缺少标识符 " + hooMap.get(0));
            return;
        }
        // 去掉开头标识符，进行解码
        try {
            String rawWords = TransferUtil.decode(hooWords.substring(hooMap.get(0).length()), hooMap);
            eventContext.getChatSession().response(rawWords);
        } catch (IllegalArgumentException e) {
            eventContext.getChatSession().response("非法齁哦语文本，含有未定义的字符");
        }
    }
}
