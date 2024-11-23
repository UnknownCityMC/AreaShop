package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.RegionParseUtil;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

import static me.wiefferink.areashop.commands.parser.GeneralRegionParser.generalRegionParser;

@Singleton
public class SetTransferCommand extends AreashopCommandBean {

    private static final CloudKey<Boolean> KEY_ENABLED = CloudKey.of("enabled", Boolean.class);
    private final MessageBridge messageBridge;
    private final IFileManager fileManager;

    @Inject
    public SetTransferCommand(
            @Nonnull MessageBridge messageBridge,
            @Nonnull IFileManager fileManager
    ) {
        this.messageBridge = messageBridge;
        this.fileManager = fileManager;
    }

    @Override
    public String getHelpKey(CommandSender target) {
        if (target.hasPermission("areashop.settransfer")) {
            return "help-settransfer";
        }
        return null;
    }

    @Override
    public String stringDescription() {
        return null;
    }

    @Override
    protected Command.Builder<? extends CommandSource<?>> configureCommand(Command.@NotNull Builder<CommandSource<?>> builder) {
        return builder.literal("settransfer")
                .required(KEY_ENABLED, BooleanParser.booleanParser(true))
                .optional("region", generalRegionParser(fileManager))
                .handler(this::handleCommand);
    }

    @Override
    protected @NonNull CommandProperties properties() {
        return CommandProperties.of("settransfer");
    }

    private void handleCommand(@Nonnull CommandContext<CommandSource<?>> context) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("areashop.settransfer")) {
            throw new AreaShopCommandException(NodePath.path("exception", "no-permission"));
        }
        GeneralRegion region = RegionParseUtil.getOrParseRegion(context, sender);
        boolean enabled = context.get(KEY_ENABLED);
        region.setTransferEnabled(enabled);
        messageBridge.message(sender, "settransfer-success", enabled, region);
        region.update();
    }
}
