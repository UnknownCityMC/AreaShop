package me.wiefferink.areashop.tools;

import com.google.inject.Singleton;
import io.github.md5sha256.areashop.KeyedMessage;
import io.github.md5sha256.areashop.MessageWrapper;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.interactivemessenger.processing.Message;

@Singleton
public class LegacyMessageBridge implements MessageBridge {

    /**
     * Send a message to a target without a prefix.
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    @Override
    public void messageNoPrefix(Object target, String key, Object... replacements) {
        unwrapMessages(replacements);
        Message.fromKey(key).replacements(replacements).send(target);
    }

    /**
     * Send a message to a target, prefixed by the default chat prefix.
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    @Override
    public void message(Object target, String key, Object... replacements) {
        unwrapMessages(replacements);
        Message.fromKey(key).prefix().replacements(replacements).send(target);
    }

    @Override
    public void message(Object target, MessageWrapper message) {
        message.toLegacyMessage().send(target);
    }

    private void unwrapMessages(Object... replacements) {
        for (int i = 0; i < replacements.length; i++) {
            Object repl = replacements[i];
            if (repl instanceof KeyedMessage wrapper) {
                replacements[i] = Message.fromKey(wrapper.messageKey())
                        .replacements(wrapper.replacements())
                        .prefix(wrapper.prefix());
            }
        }
    }

}
