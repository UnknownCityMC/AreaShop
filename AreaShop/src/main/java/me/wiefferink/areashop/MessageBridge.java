package me.wiefferink.areashop;

import io.github.md5sha256.areashop.MessageWrapper;

public interface MessageBridge {
    /**
     * Send a message to a target without a prefix.
     *
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    void messageNoPrefix(Object target, String key, Object... replacements);

    /**
     * Send a message to a target, prefixed by the default chat prefix.
     *
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    void message(Object target, String key, Object... replacements);

    void message(Object target, MessageWrapper message);
}
