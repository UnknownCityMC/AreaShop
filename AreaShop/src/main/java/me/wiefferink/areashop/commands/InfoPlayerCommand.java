package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.RegionInfoUtil;
import me.wiefferink.areashop.commands.util.ValidatedOfflinePlayerParser;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.key.CloudKey;

import javax.annotation.Nonnull;

@Singleton
public class InfoPlayerCommand extends AreashopCommandBean {

    private static final CloudKey<OfflinePlayer> KEY_PLAYER = CloudKey.of("player", OfflinePlayer.class);

    private final MessageBridge messageBridge;
    private final IFileManager fileManager;

    @Inject
    public InfoPlayerCommand(
            @Nonnull MessageBridge messageBridge,
            @Nonnull IFileManager fileManager
    ) {
        this.messageBridge = messageBridge;
        this.fileManager = fileManager;
    }

    @Override
    public String stringDescription() {
        return null;
    }

    @Override
    public String getHelpKey(CommandSender target) {
        return null;
    }

    @Override
    protected @Nonnull Command.Builder<? extends CommandSource<?>> configureCommand(@Nonnull Command.Builder<CommandSource<?>> builder) {
        return builder.literal("info").literal("player")
                .required(KEY_PLAYER, ValidatedOfflinePlayerParser.validatedOfflinePlayerParser())
                .handler(this::handleCommand);
    }

    @Override
    protected @Nonnull CommandProperties properties() {
        return CommandProperties.of("info region");
    }

    private void handleCommand(@Nonnull CommandContext<CommandSource<?>> context) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("areashop.info")) {
            messageBridge.message(sender, "info-noPermission");
            return;
        }
        OfflinePlayer offlinePlayer = context.get(KEY_PLAYER);
        RegionInfoUtil.showRegionInfo(this.messageBridge, this.fileManager, sender, offlinePlayer);
    }

}


























