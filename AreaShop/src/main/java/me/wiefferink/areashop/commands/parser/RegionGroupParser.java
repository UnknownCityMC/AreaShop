package me.wiefferink.areashop.commands.parser;

import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.RegionGroup;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegionGroupParser<C> implements ArgumentParser<C, RegionGroup>, SuggestionProvider<C> {

    private final IFileManager fileManager;
    private final String failureMessageKey;

    public RegionGroupParser(@Nonnull IFileManager fileManager, @Nonnull String failureMessageKey) {
        this.fileManager = fileManager;
        this.failureMessageKey = failureMessageKey;
    }


    @Override
    public @Nonnull ArgumentParseResult<RegionGroup> parse(
            @Nonnull CommandContext<C> commandContext,
            @Nonnull CommandInput commandInput
    ) {
        String input = commandInput.peekString();
        RegionGroup regionGroup = this.fileManager.getGroup(input);
        if (regionGroup != null) {
            commandInput.readString();
            return ArgumentParseResult.success(regionGroup);
        }
        return ArgumentParseResult.failure(new AreaShopCommandException(NodePath.path(failureMessageKey), input));
    }

    @Override
    public @Nonnull CompletableFuture<Iterable<Suggestion>> suggestionsFuture(
            @Nonnull CommandContext<C> context,
            @Nonnull CommandInput input
    ) {
        String text = input.peekString();
        List<Suggestion> suggestions = this.fileManager.getGroupNames().stream()
                .filter(name -> name.startsWith(text))
                .map(Suggestion::suggestion)
                .toList();
        return CompletableFuture.completedFuture(suggestions);
    }
}

