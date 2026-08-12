import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.event.detector.message.command.CommandNodeDetector;
import com.yuier.yuni.event.detector.message.command.model.ArgDef;
import com.yuier.yuni.event.detector.message.command.model.CommandNode;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import util.TransMap;
import util.TransferUtil;

import java.util.List;

/**
 * @Title: ChieruTrans
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 15:21
 * @description: 切噜语转换
 */
public class ChieruTrans extends CommandPlugin {

    public static final String CHIERU_TRANS = "切噜";
    public static final String CHIERU_TRANS_ENCODED = "转换";
    public static final String CHIERU_TRANS_ENCODED_ARG = "raw_words_of_chieru";
    public static final String CHIERU_TRANS_DECODED = "还原";
    public static final String CHIERU_TRANS_DECODED_ARG = "chieru_words";

    private static final CommandNode ROOT = CommandNode.builder(CHIERU_TRANS)
            .description("切噜语编码/解码")
            .child(CommandNode.builder(CHIERU_TRANS_ENCODED)
                    .description("切噜语编码")
                    .arg(ArgDef.required(CHIERU_TRANS_ENCODED_ARG, "切噜语编码", com.yuier.yuni.core.enums.CommandArgRequireType.PLAIN))
                    .build())
            .child(CommandNode.builder(CHIERU_TRANS_DECODED)
                    .description("切噜语解码")
                    .arg(ArgDef.required(CHIERU_TRANS_DECODED_ARG, "切噜语解码", com.yuier.yuni.core.enums.CommandArgRequireType.PLAIN))
                    .build())
            .build();

    @Override
    public CommandNodeDetector getDetector() {
        return new CommandNodeDetector(ROOT);
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandResult result = eventContext.getCommandResult();

        if (result.hasChild(CHIERU_TRANS_ENCODED)) {
            transferToChieru(eventContext, result.getChild(CHIERU_TRANS_ENCODED));
        }
        if (result.hasChild(CHIERU_TRANS_DECODED)) {
            transferToPlain(eventContext, result.getChild(CHIERU_TRANS_DECODED));
        }
    }

    /** 将明文转换为切噜语 */
    private void transferToChieru(YuniMessageEvent eventContext, CommandResult encodeCmd) {
        String rawWords = encodeCmd.getArg(CHIERU_TRANS_ENCODED_ARG).asText();
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> chieruMap = transMap.getChieru();
        String chieruCode = TransferUtil.encode(rawWords, chieruMap);
        eventContext.getChatSession().response(chieruMap.get(0) + chieruCode);
    }

    /** 将切噜语转换为明文 */
    private void transferToPlain(YuniMessageEvent eventContext, CommandResult decodeCmd) {
        String chieruWords = decodeCmd.getArg(CHIERU_TRANS_DECODED_ARG).asText();
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> chieruMap = transMap.getChieru();
        if (!chieruWords.startsWith(chieruMap.get(0))) {
            eventContext.getChatSession().response("非法切噜语文本，开头缺少标识符 " + chieruMap.get(0));
            return;
        }
        try {
            String rawWords = TransferUtil.decode(chieruWords.substring(chieruMap.get(0).length()), chieruMap);
            eventContext.getChatSession().response(rawWords);
        } catch (IllegalArgumentException e) {
            eventContext.getChatSession().response("非法切噜语文本，含有未定义的字符");
        }
    }
}
