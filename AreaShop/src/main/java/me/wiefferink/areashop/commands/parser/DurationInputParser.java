package me.wiefferink.areashop.commands.parser;

import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.tools.DurationInput;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.configurate.NodePath;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DurationInputParser<C> implements ArgumentParser<C, DurationInput>, SuggestionProvider<C> {

    public static <C> ParserDescriptor<C, DurationInput> durationInputParser() {
        return ParserDescriptor.of(new DurationInputParser<>(), DurationInput.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull DurationInput> parse(@NonNull CommandContext<@NonNull C> commandContext,
                                                                      @NonNull CommandInput commandInput) {
        String input = commandInput.peekString();
        int index = 0;
        for (int i = 0; i < input.length(); i++) {
            if (Character.isAlphabetic(input.charAt(i))) {
                index = i;
                break;
            }
        }
        if (index == commandInput.length() - 1) {
            return ArgumentParseResult.failure(new AreaShopCommandException(NodePath.path("exception", "duration", "wrong-input"),
                    Placeholder.parsed("input", input))
            );
        }
        String duration = input.substring(0, index);
        String durationUnit = input.substring(index);
        int durationInt;
        try {
            durationInt = Integer.parseInt(duration);
        } catch (NumberFormatException ex) {
            return ArgumentParseResult.failure(new AreaShopCommandException(NodePath.path("exception", "duration", "wrong-amount"),
                    Placeholder.parsed("input", input)));
        }
        Optional<TimeUnit> timeUnit = DurationInput.getTimeUnit(durationUnit.toLowerCase(Locale.ENGLISH));
        if (timeUnit.isEmpty()) {
            return ArgumentParseResult.failure(new AreaShopCommandException(NodePath.path("exception", "duration", "wrong-format"),
                    Placeholder.parsed("input", input)));
        }
        DurationInput durationInput = new DurationInput(durationInt, timeUnit.get());
        commandInput.readString();
        return ArgumentParseResult.success(durationInput);
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
            @NonNull CommandContext<C> context,
            @NonNull CommandInput input
    ) {
        String text = input.peekString();
        int firstText = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isAlphabetic(c)) {
                firstText = c;
                break;
            }
        }
        if (firstText == 0) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        String timeUnit = text.substring(firstText);
        List<String> suffixes = DurationInput.SUFFIXES;
        List<Suggestion> suggestions = suffixes.stream()
                .filter(suffix -> suffix.startsWith(timeUnit))
                .map(Suggestion::suggestion)
                .toList();
        return CompletableFuture.completedFuture(suggestions);
    }
}
