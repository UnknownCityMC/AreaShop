package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfoCommand extends AreashopCommandBean {
    @Override
    public String stringDescription() {
        return "";
    }

    @Override
    protected @NotNull Command.Builder<? extends CommandSource<?>> configureCommand(@NotNull Command.Builder<CommandSource<?>> builder) {
        return builder.literal("info")
                .handler(this::handleCommand);
    }

    private void handleCommand(@NonNull CommandContext<CommandSource<?>> context) {
    }

    @Override
    public @Nullable String getHelpKey(@NotNull CommandSender target) {
        return "";
    }

    @Override
    protected @NonNull CommandProperties properties() {
        return null;
    }
}
