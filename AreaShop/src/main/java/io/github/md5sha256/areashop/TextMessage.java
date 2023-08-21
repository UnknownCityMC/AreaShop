package io.github.md5sha256.areashop;

import me.wiefferink.interactivemessenger.processing.Message;

public class TextMessage implements MessageWrapper {

    private final String text;

    public TextMessage(String text) {
        this.text = text;
    }

    @Override
    public Message toLegacyMessage() {
        return Message.empty().append(text);
    }

    @Override
    public String toMiniMessage(AdventureMessageRenderer messageRenderer) {
        return messageRenderer.performReplacements(text);
    }
}
