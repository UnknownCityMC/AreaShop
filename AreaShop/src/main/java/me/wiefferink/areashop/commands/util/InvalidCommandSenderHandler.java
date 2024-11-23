package me.wiefferink.areashop.commands.util;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.commands.util.commandsource.PlayerCommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

public class InvalidCommandSenderHandler implements ExceptionHandler<CommandSource<?>, InvalidCommandSenderException> {

    private final PaperMessenger messenger;

    public InvalidCommandSenderHandler(@Nonnull PaperMessenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public void handle(@NonNull ExceptionContext<CommandSource<?>, InvalidCommandSenderException> context) throws Throwable {
        InvalidCommandSenderException exception = context.exception();
        if (exception.requiredSenderTypes().contains(PlayerCommandSource.class)) {
            this.messenger.sendMessage(context.context().sender().sender(), NodePath.path("exception", "only-player"));
            return;
        }
        throw exception;
    }
}
