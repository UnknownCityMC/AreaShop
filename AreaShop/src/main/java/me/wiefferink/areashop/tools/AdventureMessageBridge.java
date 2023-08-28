package me.wiefferink.areashop.tools;

import jakarta.inject.Inject;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.managers.AdventureLanguageManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.regex.Pattern;

public class AdventureMessageBridge implements MessageBridge {

    private final Pattern REPLACEMENT_PATTERN = Pattern.compile("(%)(\\d)(%)");

    private final AdventureLanguageManager adventureLanguageManager;

    public AdventureMessageBridge(AdventureLanguageManager languageManager) {
        this.adventureLanguageManager = languageManager;
    }

    @Override
    public void messageNoPrefix(Object target, String key, Object... replacements) {
        if (!(target instanceof Audience audience)) {
            return;
        }
        Component component = this.adventureLanguageManager.messageFor(key);
        if (component.equals(Component.empty())) {
            return;
        }
        // FIXME: do replacements
        audience.sendMessage(component);
    }

    @Override
    public void message(Object target, String key, Object... replacements) {
        if (!(target instanceof Audience audience)) {
            return;
        }
        Component component = this.adventureLanguageManager.messageFor(key);
        if (component.equals(Component.empty())) {
            return;
        }
        // FIXME: do replacements
        audience.sendMessage(component);
    }
}
