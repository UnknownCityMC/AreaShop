package me.wiefferink.areashop.commands.util;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
        TagResolver[] tagResolvers = exception.tagResolvers();
        messenger.sendMessage(sender, key, tagResolvers);
    }
}
