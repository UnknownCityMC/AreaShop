package me.wiefferink.areashop.commands.parser;

import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.RentRegion;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

public class RentRegionParser<C> implements ArgumentParser<C, RentRegion> {

    private final IFileManager fileManager;
    private final SuggestionProvider<C> suggestionProvider;

    private RentRegionParser(@Nonnull IFileManager fileManager, @Nonnull SuggestionProvider<C> suggestionProvider) {
        this.fileManager = fileManager;
        this.suggestionProvider = suggestionProvider;
    }

    public static <C> ParserDescriptor<C, RentRegion> rentRegionParser(IFileManager fileManager, SuggestionProvider<C> suggestionProvider) {
        return ParserDescriptor.of(new RentRegionParser<>(fileManager, suggestionProvider), RentRegion.class);
    }

    public static <C> ParserDescriptor<C, RentRegion> rentRegionParser(IFileManager fileManager) {
        return rentRegionParser(fileManager, defaultSuggestionProvider(fileManager));
    }

    private static <C> SuggestionProvider<C> defaultSuggestionProvider(@Nonnull IFileManager fileManager) {
        return SuggestionProvider.blockingStrings((ctx, input) -> {
                    String text = input.peekString();
                    return fileManager.getRentNames()
                            .stream()
                            .filter(name -> name.startsWith(text))
                            .toList();
                }
        );
    }

    @Override
    public @Nonnull ArgumentParseResult<RentRegion> parse(@Nonnull CommandContext<C> commandContext,
                                                         @Nonnull CommandInput commandInput) {
        String input = commandInput.peekString();
        RentRegion region = this.fileManager.getRent(input);
        if (region != null) {
            commandInput.readString();
            return ArgumentParseResult.success(region);
        }
        AreaShopCommandException exception = new AreaShopCommandException(NodePath.path("command", "plot", "rent", "not-rent-able"),
                Placeholder.parsed("input", input));
        return ArgumentParseResult.failure(exception);
    }

    @Override
    public @Nonnull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
    }
}
