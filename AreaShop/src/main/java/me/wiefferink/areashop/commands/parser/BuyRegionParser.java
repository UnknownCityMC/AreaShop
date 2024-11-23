package me.wiefferink.areashop.commands.parser;

import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

public class BuyRegionParser<C> implements ArgumentParser<C, BuyRegion> {

    private final IFileManager fileManager;
    private final SuggestionProvider<C> suggestionProvider;

    private BuyRegionParser(@Nonnull IFileManager fileManager, @Nonnull SuggestionProvider<C> suggestionProvider) {
        this.fileManager = fileManager;
        this.suggestionProvider = suggestionProvider;
    }

    private BuyRegionParser(@Nonnull IFileManager fileManager) {
        this(fileManager, defaultSuggestionProvider(fileManager));
    }

    public static <C> ParserDescriptor<C, BuyRegion> buyRegionParser(IFileManager fileManager, SuggestionProvider<C> suggestionProvider) {
        return ParserDescriptor.of(new BuyRegionParser<>(fileManager, suggestionProvider), BuyRegion.class);
    }

    public static <C> ParserDescriptor<C, BuyRegion> buyRegionParser(IFileManager fileManager) {
        return ParserDescriptor.of(new BuyRegionParser<>(fileManager, defaultSuggestionProvider(fileManager)), BuyRegion.class);
    }

    private static <C> SuggestionProvider<C> defaultSuggestionProvider(@Nonnull IFileManager fileManager) {
        return SuggestionProvider.blockingStrings((ctx, input) -> {
                    String text = input.peekString();
                    return fileManager.getBuyNames()
                            .stream()
                            .filter(name -> name.startsWith(text))
                            .toList();
                }
        );
    }

    @Override
    public @Nonnull ArgumentParseResult<BuyRegion> parse(@Nonnull CommandContext<C> commandContext,
                                                         @Nonnull CommandInput commandInput) {
        String input = commandInput.peekString();
        BuyRegion region = this.fileManager.getBuy(input);
        if (region != null) {
            commandInput.readString();
            return ArgumentParseResult.success(region);
        }
        AreaShopCommandException exception = new AreaShopCommandException(NodePath.path("command", "buy", "not-buy-able"), input);
        return ArgumentParseResult.failure(exception);
    }

    @Override
    public @Nonnull SuggestionProvider<C> suggestionProvider() {
        return this.suggestionProvider;
    }
}
