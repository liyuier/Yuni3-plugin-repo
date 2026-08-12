package steamstatus;

import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.core.event.matched.CommandResult;
import com.yuier.yuni.event.detector.message.command.CommandNodeDetector;
import com.yuier.yuni.event.detector.message.command.model.ArgDef;
import com.yuier.yuni.event.detector.message.command.model.CommandNode;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;
import steamstatus.db.DBHelper;
import steamstatus.db.UserSteamId;
import steamstatus.utils.RedisHelper;
import steamstatus.utils.SteamStatusUtil;

import java.util.List;

/**
 * @Title: UserRegisterSteamId
 * @Author yuier
 * @Package steamstatus
 * @Date 2026/6/17 23:20
 * @description: 用户绑定 steam id
 */

public class UserRegisterSteamId extends CommandPlugin {

    private static final String STEAM = "steam";
    private static final String 绑定ID = "绑定id";
    private static final String 解绑ID = "解绑id";
    private static final String ID参数 = "ID参数";

    private static final CommandNode ROOT = CommandNode.builder(STEAM)
            .description("Steam ID 绑定/解绑")
            .child(CommandNode.builder(绑定ID)
                    .description("绑定 steam id")
                    .arg(ArgDef.required(ID参数, "steam id"))
                    .build())
            .child(CommandNode.builder(解绑ID)
                    .description("解绑 steam id")
                    .build())
            .build();

    @Override
    public CommandNodeDetector getDetector() {
        return new CommandNodeDetector(ROOT);
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {
        CommandResult result = eventContext.getCommandResult();
        if (result.hasChild(解绑ID)) {
            unbindSteamId(eventContext);
        }
        if (result.hasChild(绑定ID)) {
            bindSteamId(eventContext, result.getChild(绑定ID));
        }
    }

    private void unbindSteamId(YuniMessageEvent eventContext) {
        Long userId = eventContext.getUserId();
        List<UserSteamId> byUserId = DBHelper.findByUserId(userId);
        if (byUserId == null || byUserId.isEmpty()) {
            eventContext.getChatSession().reply("你未绑定任何 steam id.");
        } else {
            DBHelper.deleteByUserId(userId);
            RedisHelper.deleteId(byUserId.getFirst().getSteamId());
            eventContext.getChatSession().reply("你已解除绑定 steam id: " + byUserId.getFirst().getSteamId());
        }
    }

    private void bindSteamId(YuniMessageEvent eventContext, CommandResult bindCmd) {
        // 获取 steam id
        String steamId = bindCmd.getArg(ID参数).asText();
        // 获取用户 id
        Long userId = eventContext.getUserId();
        List<UserSteamId> byUserId = DBHelper.findByUserId(userId);
        // 检查用户是否已有记录
        if (byUserId != null && !byUserId.isEmpty()) {
            eventContext.getChatSession().reply("你已绑定如下 steam id: " + byUserId.getFirst().getSteamId() +
                    "; 若要绑定新 id, 请使用 `/steam 解绑id` 命令解除绑定当前 ID");
            return;
        }
        DBHelper.insert(new UserSteamId(userId, steamId));
        // 刷新到 redis 中
        SteamStatusUtil.addSteamIdToRedis(steamId, userId);
        eventContext.getChatSession().reply("你已绑定 steam id: " + steamId);
    }
}
