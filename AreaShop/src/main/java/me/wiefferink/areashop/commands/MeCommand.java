package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.RegionInfoUtil;
import me.wiefferink.areashop.commands.util.ValidatedOfflinePlayerParser;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.commands.util.commandsource.PlayerCommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.key.CloudKey;

import javax.annotation.Nonnull;

@Singleton
public class MeCommand extends AreashopCommandBean {

    private static final CloudKey<OfflinePlayer> KEY_PLAYER = CloudKey.of("player", OfflinePlayer.class);
    private final IFileManager fileManager;
    private final MessageBridge messageBridge;

    @Inject
    public MeCommand(
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
    protected Command.Builder<? extends CommandSource<?>> configureCommand(Command.Builder<CommandSource<?>> builder) {
        return builder.literal("me")
                .senderType(PlayerCommandSource.class)
                .optional(KEY_PLAYER, ValidatedOfflinePlayerParser.validatedOfflinePlayerParser())
                .handler(this::handleCommand);
    }

    @Override
    protected @NonNull CommandProperties properties() {
        return CommandProperties.of("me");
    }

    @Override
    public String getHelpKey(CommandSender target) {
        if (target.hasPermission("areashop.me")) {
            return "help-me";
        }
        return null;
    }

    private void handleCommand(@Nonnull CommandContext<PlayerCommandSource> context) {
        Player sender = context.sender().sender();
        if (!sender.hasPermission("areashop.me")) {
            throw new AreaShopCommandException("me-noPermission");
        }
        OfflinePlayer player = context.getOrDefault(KEY_PLAYER, sender);
        RegionInfoUtil.showRegionInfo(this.messageBridge, this.fileManager, sender, player);
    }

}


























