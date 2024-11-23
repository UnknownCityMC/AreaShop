package me.wiefferink.areashop.commands.util;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ArgumentParseExceptionHandler<C extends CommandSource<?>> implements ExceptionHandler<C, AreaShopCommandException> {

    private final PaperMessenger messenger;

    public ArgumentParseExceptionHandler(@Nonnull PaperMessenger messenger) {
        this.messenger = messenger;
    }


    @Override
    public void handle(@Nonnull ExceptionContext<C, AreaShopCommandException> context) {
        var exception = context.exception();
        var sender = context.context().sender().sender();
        NodePath key = exception.messageKey();
        Object[] replacements = exception.replacements();
        if (replacements.length == 0) {
            messenger.sendMessage(sender, key);
            return;
        }
        // Pass the values as a var-args and not as a string[]
        messenger.sendMessage(sender, key);
    }
}
