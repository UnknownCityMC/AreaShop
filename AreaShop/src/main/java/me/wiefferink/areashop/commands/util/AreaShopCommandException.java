package me.wiefferink.areashop.commands.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

public class AreaShopCommandException extends RuntimeException {

    private static final Object[] EMPTY = new Object[0];

    private final NodePath path;
    private final Object[] replacements;

    public AreaShopCommandException(@Nonnull NodePath path, @Nullable Object... replacements) {
        this.path = path;
        this.replacements = replacements == null ? EMPTY : replacements;
    }

    public NodePath messageKey() {
        return this.path;
    }

    public Object[] replacements() {
        return this.replacements;
    }

    @Override
    public String getMessage() {
        return this.path.toString();
    }
}
