import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;

/**
 * @Title: MCManage
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/8/11 3:26
 * @description: MC 服务器管理
 */

public class MCManage extends CommandPlugin {

    private static final String 安慕希 = "mc";
    private static final String 添加 = "添加";

    private static final String 服务器名称 = "";

    @Override
    public CommandDetector getDetector() {
        return null;
    }

    @Override
    public void execute(YuniMessageEvent eventContext) {

    }
}
