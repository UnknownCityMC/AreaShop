package io.github.md5sha256.areashop;

import me.wiefferink.areashop.managers.AdventureLanguageManager;
import me.wiefferink.interactivemessenger.processing.ReplacementProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdventureMessageRenderer {


    /**
     * Lang pattern used by the old mechanism
     * Equivalent REGEX: \Q%\E\Qlang:\E([a-zA-Z-]+?)\|(.+?)\|\Q%\E
     * Group 1 is the message key for substitution
     * Group 2 is the replacement arguments
     * NOTE: A properly formatted key should have a trailing '|' char.
     */
    private static final Pattern LANG_PATTERN = Pattern.compile(
            Pattern.quote("%") + "lang:([a-zA-Z-]+?)\\|(.+)\\|+" + Pattern.quote("%")
    );
    private static final Pattern POSITIONAL_ARGUMENT_PATTERN = Pattern.compile(
            Pattern.quote("%") + "(\\d+)" + Pattern.quote("%")
    );
    private static final Pattern NAMED_ARGUMENT_PATTERN = Pattern.compile(Pattern.quote(
            "%") + "(?<name>[a-zA-Z]+)" + Pattern.quote("%"));
    private static final Pattern VARIABLE_DELIMITER = Pattern.compile("\\|");
    private final AdventureLanguageManager adventureLanguageManager;

    public AdventureMessageRenderer(AdventureLanguageManager languageManager) {
        this.adventureLanguageManager = languageManager;
    }

    public String chatPrefix() {
        return this.adventureLanguageManager.chatPrefix();
    }

    public Component renderedChatPrefix() {
        return renderMessage(this.adventureLanguageManager.chatPrefix());
    }

    public Component renderMessageFromKey(String key, Object... replacements) {
        String message = this.adventureLanguageManager.messageFor(key);
        if (message.isEmpty()) {
            return Component.empty();
        }
        return renderMessage(message, replacements);
    }

    public String performReplacementsFromKey(String key, Object... replacements) {
        String message = this.adventureLanguageManager.messageFor(key);
        if (message.isEmpty()) {
            return message;
        }
        return performReplacements(message, replacements);
    }


    public String performReplacements(String message, Object... replacements) {
        String result = processPositionalArgumentReplacement(message, replacements);
        return processLanguageReplacements(result, replacements);
    }

    public Component renderMessage(String message, Object... replacements) {
        MiniMessage miniMessage = MiniMessage
                .builder()
                .preProcessor(string -> performReplacements(string, replacements))
                .build();
        return miniMessage.deserialize(message);
    }

    private void processNamedArguments(String[] arguments, Object... replacements) {
        for (int i = 0; i < arguments.length; i++) {
            if (i >= replacements.length) {
                continue;
            }
            String argument = arguments[i];
            Matcher namedMatcher = NAMED_ARGUMENT_PATTERN.matcher(argument);
            if (!namedMatcher.matches()) {
                continue;
            }
            Object rawReplacement = replacements[i];
            if (!(rawReplacement instanceof ReplacementProvider provider)) {
                continue;
            }
            String replacement = parseString(provider.provideReplacement(argument));
            arguments[i] = replacement;
        }
    }

    private String processLanguageReplacements(String text, Object... replacements) {
        Matcher langMatcher = LANG_PATTERN.matcher(text);
        return langMatcher.replaceAll(result -> {
            String langKey = result.group(1);
            String rawArguments = result.group(2);
            String[] arguments = VARIABLE_DELIMITER.split(rawArguments);
            processNamedArguments(arguments, replacements);
            String innerMessage = this.adventureLanguageManager.messageFor(langKey);
            return processInnerPositionalArgumentReplacements(innerMessage, arguments);
        });
    }

    private String processInnerPositionalArgumentReplacements(String text, String[] replacements) {
        final Matcher matcher = POSITIONAL_ARGUMENT_PATTERN.matcher(text);
        return matcher.replaceAll(result -> {
            int position = Integer.parseInt(result.group(1));
            if (position < 0 || position > replacements.length - 1) {
                return "";
            }
            return replacements[position];
        });
    }

    private String processPositionalArgumentReplacement(String text, Object... replacements) {
        final Matcher matcher = POSITIONAL_ARGUMENT_PATTERN.matcher(text);
        return matcher.replaceAll(result -> {
            int position = Integer.parseInt(result.group(1));
            if (position < 0 || position > replacements.length - 1) {
                return "";
            }
            Object replacement = replacements[position];
            if (!(replacement instanceof ReplacementProvider)) {
                return replacement.toString();
            }
            return "";
        });
    }

    private String parseString(Object replacementResult) {
        if (replacementResult instanceof String s) {
            return s;
        }
        if (replacementResult instanceof Component component) {
            return MiniMessage.miniMessage().serialize(component);
        }
        if (replacementResult instanceof Number number) {
            return NumberFormat.getNumberInstance().format(number);
        }
        return replacementResult.toString();
    }

}
