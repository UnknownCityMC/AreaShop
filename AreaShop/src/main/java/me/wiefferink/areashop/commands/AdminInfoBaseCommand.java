package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@Singleton
public class AdminInfoBaseCommand extends AreashopCommandBean {

    private final MessageBridge messageBridge;

    @Inject
    public AdminInfoBaseCommand(
            @Nonnull MessageBridge messageBridge
    ) {
        this.messageBridge = messageBridge;
    }

    @Override
    public String stringDescription() {
        return null;
    }

    @Override
    public String getHelpKey(@NotNull CommandSender target) {
        if (target.hasPermission("areashop.admininfo")) {
            return "help-info";
        }
        return null;
    }

    @Override
    protected @Nonnull Command.Builder<? extends CommandSource<?>> configureCommand(@Nonnull Command.Builder<CommandSource<?>> builder) {
        return builder.literal("admininfo").handler(this::handleCommand);
    }

    @Override
    protected @Nonnull CommandProperties properties() {
        return CommandProperties.of("info");
    }

    private void handleCommand(@Nonnull CommandContext<CommandSource<?>> context) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("areashop.admininfo")) {
            messageBridge.message(sender, "info-noPermission");
            return;
        }
        this.messageBridge.message(sender, "info-help");
    }

}


























