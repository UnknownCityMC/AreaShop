package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.MessageBridge;

public class ForwardingMessageBridge implements MessageBridge {

    private MessageBridge delegate;

    public ForwardingMessageBridge() {
    }

    public void delegate(MessageBridge messageBridge) {
        this.delegate = messageBridge;
    }

    @Override
    public void messageNoPrefix(Object target, String key, Object... replacements) {
        if (this.delegate == null) {
            return;
        }
        this.delegate.messageNoPrefix(target, key, replacements);
    }

    @Override
    public void message(Object target, String key, Object... replacements) {
        if (this.delegate == null) {
            return;
        }
        this.delegate.message(target, key, replacements);
    }
}
