import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.ArgDef;
import com.yuier.yuni.event.detector.message.command.model.CommandNode;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import com.yuier.yuni.plugin.util.PluginUtils;
import util.TransMap;
import util.TransferUtil;

import java.util.List;

/**
 * @Title: HooTrans
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 17:56
 * @description: 齁哦哦转换
 */
public class HooTrans extends CommandPlugin {

    public static final String HOO_TRANS = "齁哦";
    public static final String HOO_TRANS_ENCODED = "转换";
    public static final String HOO_TRANS_ENCODED_ARG = "raw_words_of_hoo";
    public static final String HOO_TRANS_DECODED = "还原";
    public static final String HOO_TRANS_DECODED_ARG = "hoo_words";

    private static final CommandNode ROOT = CommandNode.builder(HOO_TRANS)
            .description("齁哦语编码/解码")
            .child(CommandNode.builder(HOO_TRANS_ENCODED)
                    .description("齁哦语编码")
                    .arg(ArgDef.required(HOO_TRANS_ENCODED_ARG, "齁哦语编码", com.yuier.yuni.core.enums.CommandArgRequireType.PLAIN))
                    .build())
            .child(CommandNode.builder(HOO_TRANS_DECODED)
                    .description("齁哦语解码")
                    .arg(ArgDef.required(HOO_TRANS_DECODED_ARG, "齁哦语解码", com.yuier.yuni.core.enums.CommandArgRequireType.PLAIN))
                    .build())
            .build();

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(ROOT);
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandResult result = eventContext.getCommandResult();

        if (result.hasChild(HOO_TRANS_ENCODED)) {
            transferToHoo(eventContext, result.getChild(HOO_TRANS_ENCODED));
        }
        if (result.hasChild(HOO_TRANS_DECODED)) {
            transferToPlain(eventContext, result.getChild(HOO_TRANS_DECODED));
        }
    }

    private void transferToHoo(YuniMessageEvent eventContext, CommandResult encodeCmd) {
        String rawWords = encodeCmd.getArg(HOO_TRANS_ENCODED_ARG).asText();
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> hooMap = transMap.getHoo();
        String hooCode = TransferUtil.encode(rawWords, hooMap);
        eventContext.getChatSession().response(hooMap.get(0) + hooCode);
    }

    private void transferToPlain(YuniMessageEvent eventContext, CommandResult decodeCmd) {
        String hooWords = decodeCmd.getArg(HOO_TRANS_DECODED_ARG).asText();
        TransMap transMap = PluginUtils.loadJsonConfigFromPlugin("trans-map.json", TransMap.class, this.getClass());
        List<String> hooMap = transMap.getHoo();
        if (!hooWords.startsWith(hooMap.get(0))) {
            eventContext.getChatSession().response("非法齁哦语文本，开头缺少标识符 " + hooMap.get(0));
            return;
        }
        try {
            String rawWords = TransferUtil.decode(hooWords.substring(hooMap.get(0).length()), hooMap);
            eventContext.getChatSession().response(rawWords);
        } catch (IllegalArgumentException e) {
            eventContext.getChatSession().response("非法齁哦语文本，含有未定义的字符");
        }
    }
}
