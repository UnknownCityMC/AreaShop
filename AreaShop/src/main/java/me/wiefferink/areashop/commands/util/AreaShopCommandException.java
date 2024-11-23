package me.wiefferink.areashop.commands.util;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

public class AreaShopCommandException extends RuntimeException {

    private final NodePath path;
    private final TagResolver[] tagResolvers;

    public AreaShopCommandException(@Nonnull NodePath path, TagResolver... tagResolvers) {
        this.path = path;
        this.tagResolvers = tagResolvers;
    }

    public NodePath messageKey() {
        return this.path;
    }

    public TagResolver[] tagResolvers() {
        return tagResolvers;
    }

    @Override
    public String getMessage() {
        return this.path.toString();
    }
}
