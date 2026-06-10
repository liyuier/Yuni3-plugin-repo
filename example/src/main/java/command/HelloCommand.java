package command;

import com.yuier.yuni.core.bot.MessageSentResult;
import com.yuier.yuni.core.event.YuniMessageEvent;
import com.yuier.yuni.event.detector.message.command.CommandDetector;
import com.yuier.yuni.event.detector.message.command.model.CommandBuilder;
import com.yuier.yuni.plugin.model.passive.message.CommandPlugin;

/**
 * @Title: command.HelloCommand
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2025/12/23 21:03
 * @description:
 */

public class HelloCommand extends CommandPlugin {

    @Override
    public void execute(YuniMessageEvent eventContext) {
        MessageSentResult response = eventContext.getChatSession().response("Hello Command!");
    }

    @Override
    public CommandDetector getDetector() {
        return new CommandDetector(CommandBuilder.create("test").build());
    }

}
