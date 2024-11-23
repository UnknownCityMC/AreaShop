package me.wiefferink.areashop.commands.parser;

import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.spongepowered.configurate.NodePath;

public class OfflinePlayerParser<C> implements ArgumentParser<C, String>, BlockingSuggestionProvider.Strings<C> {


    public static <C> ParserDescriptor<C, String> parser() {
        return ParserDescriptor.of(new OfflinePlayerParser<>(), String.class);
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull String> parse(@NonNull CommandContext<@NonNull C> commandContext,
                                                               @NonNull CommandInput commandInput) {
        final String input = commandInput.readString();
        if (input.length() > 16) {
            return ArgumentParseResult.failure(new AreaShopCommandException(NodePath.path("exception", "invalid-player"),
                    Placeholder.parsed("input", input)));
        }
        return ArgumentParseResult.success(input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<C> commandContext,
                                                                @NonNull CommandInput input) {
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }
}
