package me.wiefferink.areashop.managers;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AdventureLanguageManager {

    private final Map<String, LanguageRegistry> languageRegistryMap = new HashMap<>();
    private String currentLanguage;
    private String fallbackLanguage;

    private Component chatPrefix = Component.empty();

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

    public AdventureLanguageManager loadValues(@NotNull String language,
                                               @NotNull Map<String, String> values) {
        this.languageRegistryMap.put(language, new LanguageRegistry(values));
        return this;
    }

    public String messageFor(String key, String lang) {
        LanguageRegistry registry = this.languageRegistryMap.get(lang);
        if (registry == null) {
            return "";
        }
        return registry.messageFor(key);
    }

    public String messageFor(String key) {
        String  message = messageFor(key, this.currentLanguage);
        if (message.isEmpty() && !this.currentLanguage.equals(fallbackLanguage)) {
            message = messageFor(key, this.fallbackLanguage);
        }
        return message;
    }

    public void chatPrefix(Component prefix) {
        this.chatPrefix = prefix;
    }

    public Component chatPrefix() {
        return this.chatPrefix;
    }

    private static class LanguageRegistry {
        private final Map<String, String> messages = new HashMap<>();

        public LanguageRegistry(Map<String, String> messages) {
            this.messages.putAll(messages);
        }

        public void messageFor(String key, String message) {
            this.messages.put(key, message);
        }

        public String messageFor(String key) {
            return this.messages.getOrDefault(key, "");
        }

        public boolean isEmpty() {
            return this.messages.isEmpty();
        }
    }

}
