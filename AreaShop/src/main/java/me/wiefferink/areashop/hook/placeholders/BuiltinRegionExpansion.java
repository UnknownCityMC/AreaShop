package me.wiefferink.areashop.hook.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.regions.IRegionContainer;
import me.wiefferink.interactivemessenger.processing.ReplacementProvider;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.regex.Pattern;

public class BuiltinRegionExpansion extends PlaceholderExpansion {

    private final Pattern pattern = Pattern.compile("_");
    private final IRegionContainer regionContainer;

    public BuiltinRegionExpansion(@NotNull IRegionContainer regionContainer) {
        this.regionContainer = regionContainer;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "region";
    }

    @Override
    public @NotNull String getAuthor() {
        return "md5sha256";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        final String[] split = pattern.split(params);
        if (split.length < 2) {
            return null;
        }
        final String regionId = split[0];
        final GeneralRegionInterface region = regionContainer.getRegionInterface(regionId);
        if (!(region instanceof ReplacementProvider provider)) {
            return null;
        }
        final String replacementKey = split[1];
        final Object replacement = provider.provideReplacement(replacementKey);
        return handleReplacement(replacement);
    }

     @Nullable String handleReplacement(@Nullable Object repl) {
        if (repl instanceof String s) {
            return s;
        } else if (repl instanceof Number number) {
            return formatNumber(number);
        } else if (repl != null) {
            return repl.toString();
        }
        return null;
    }

    private @NotNull String formatNumber(@NotNull Number number) {
        return NumberFormat.getInstance().format(number);
    }
}
