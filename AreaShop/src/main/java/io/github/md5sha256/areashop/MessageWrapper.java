package io.github.md5sha256.areashop;

import me.wiefferink.interactivemessenger.processing.Message;

public interface MessageWrapper {

    Message toLegacyMessage();

    String toMiniMessage(AdventureMessageRenderer messageRenderer);

}
