package io.github.md5sha256.areashop;

import me.wiefferink.interactivemessenger.processing.Message;

public record KeyedMessage(String messageKey, boolean prefix, Object... replacements) implements MessageWrapper{
    public KeyedMessage(String messageKey, Object... replacements) {
        this(messageKey, false, replacements);
    }

    @Override
    public Message toLegacyMessage() {
        return Message.fromKey(messageKey).prefix(prefix).replacements(replacements);
    }

    @Override
    public String toMiniMessage(AdventureMessageRenderer messageRenderer) {
        String chatPrefix = messageRenderer.chatPrefix();
        return chatPrefix + messageRenderer.renderMessageFromKey(messageKey);
    }
}
