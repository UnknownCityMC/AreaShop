package io.github.md5sha256.areashop;

import me.wiefferink.interactivemessenger.processing.Message;

import java.util.ArrayList;
import java.util.List;

public class CompositeMessage implements MessageWrapper {

    private final List<MessageWrapper> children = new ArrayList<>();

    @Override
    public Message toLegacyMessage() {
        Message message = Message.empty();
        for (MessageWrapper wrapper : children) {
            message.append(wrapper.toLegacyMessage());
        }
        return message;
    }

    @Override
    public String toMiniMessage(AdventureMessageRenderer messageRenderer) {
        StringBuilder message = new StringBuilder();
        for (MessageWrapper wrapper : children) {
            message.append(wrapper.toMiniMessage(messageRenderer));
        }
        return message.toString();
    }

    public CompositeMessage appendMessage(MessageWrapper wrapper) {
        children.add(wrapper);
        return this;
    }

    public CompositeMessage appendText(String text) {
        this.children.add(new TextMessage(text));
        return this;
    }

    public CompositeMessage appendMessage(String key, boolean prefix, Object... replacements) {
        this.children.add(new KeyedMessage(key, prefix, replacements));
        return this;
    }

    public CompositeMessage appendMessage(String key, Object... replacements) {
        this.children.add(new KeyedMessage(key, replacements));
        return this;
    }


}
