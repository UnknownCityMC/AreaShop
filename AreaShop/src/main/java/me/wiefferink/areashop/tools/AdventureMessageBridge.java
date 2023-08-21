package me.wiefferink.areashop.tools;

import io.github.md5sha256.areashop.AdventureMessageRenderer;
import io.github.md5sha256.areashop.MessageWrapper;
import me.wiefferink.areashop.MessageBridge;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AdventureMessageBridge implements MessageBridge {
    private final AdventureMessageRenderer messageRenderer;


    public AdventureMessageBridge(AdventureMessageRenderer messageRenderer) {
        this.messageRenderer = messageRenderer;
    }

    @Override
    public void message(Object target, MessageWrapper message) {
        if (!(target instanceof Audience audience)) {
            return;
        }
        String miniMessage = message.toMiniMessage(this.messageRenderer);
        audience.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    @Override
    public void messageNoPrefix(Object target, String key, Object... replacements) {
        if (!(target instanceof Audience audience)) {
            return;
        }
        audience.sendMessage(this.messageRenderer.renderMessageFromKey(key, replacements));
    }

    @Override
    public void message(Object target, String key, Object... replacements) {
        if (!(target instanceof Audience audience)) {
            return;
        }
        Component message = this.messageRenderer.renderMessageFromKey(key, replacements);
        Component prefix = this.messageRenderer.renderedChatPrefix();
        Component prefixedMessage = prefix.append(message);
        audience.sendMessage(prefixedMessage);
    }
}
