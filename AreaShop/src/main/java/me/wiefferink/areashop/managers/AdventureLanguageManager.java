package me.wiefferink.areashop.managers;

import jakarta.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class AdventureLanguageManager {

    private final Map<String, LanguageRegistry> languageRegistryMap = new HashMap<>();

    private static class LanguageRegistry {
        private final Map<String, Component> messages = new HashMap<>();

        public LanguageRegistry(Map<String, Component> messages) {
            this.messages.putAll(messages);
        }

        public void messageFor(String key, Component message) {
            this.messages.put(key, message);
        }

        public Component messageFor(String key) {
            return this.messages.getOrDefault(key, Component.empty());
        }
    }

    private String currentLanguage;
    private String fallbackLanguage;

    public AdventureLanguageManager(String currentLanguage, String fallbackLanguage) {
        this.currentLanguage = currentLanguage;
        this.fallbackLanguage = fallbackLanguage;
    }

    public AdventureLanguageManager currentLanguage(@NotNull String language) {
        this.currentLanguage = language;
        return this;
    }

    public AdventureLanguageManager fallbackLanguage(@NotNull String language) {
        this.fallbackLanguage = language;
        return this;
    }

    public AdventureLanguageManager loadValues(@NotNull String language, @NotNull Map<String, Component> values) {
        this.languageRegistryMap.put(language, new LanguageRegistry(values));
        return this;
    }

    public Component messageFor(String key) {
        LanguageRegistry current = this.languageRegistryMap.get(this.currentLanguage);
        Component message = current.messageFor(key);
        if (message.equals(Component.empty()) && !this.currentLanguage.equals(fallbackLanguage)) {
            return this.languageRegistryMap.get(this.fallbackLanguage).messageFor(key);
        }
        return message;
    }

}
