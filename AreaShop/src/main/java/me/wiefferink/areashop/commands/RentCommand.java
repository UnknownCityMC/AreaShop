package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.RegionParseUtil;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.commands.util.commandsource.PlayerCommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.RentRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;

import static me.wiefferink.areashop.commands.parser.RentRegionParser.rentRegionParser;

@Singleton
public class RentCommand extends AreashopCommandBean {

    private final IFileManager fileManager;

    @Inject
    public RentCommand(@Nonnull IFileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public String stringDescription() {
        return "Allows you to rent a region";
    }

    @Override
    public String getHelpKey(@NotNull CommandSender target) {
        if(target.hasPermission("areashop.rent")) {
            return "help-rent";
        }
        return null;
    }

    @Override
    protected @Nonnull CommandProperties properties() {
        return CommandProperties.of("rent");
    }


    @Override
    protected @Nonnull Command.Builder<? extends CommandSource<?>> configureCommand(@Nonnull Command.Builder<CommandSource<?>> builder) {
        return builder
                .literal("rent")
                .optional("region-rent", rentRegionParser(fileManager))
                .senderType(PlayerCommandSource.class)
                .handler(this::handleCommand);
    }

    private void handleCommand(@Nonnull CommandContext<PlayerCommandSource> context) {
        if (!context.hasPermission("areashop.rent")) {
            throw new AreaShopCommandException(NodePath.path("exception", "no-permission"));
        }
        Player sender = context.sender().sender();
        RentRegion region = RegionParseUtil.getOrParseRentRegion(context, sender);
        region.rent(sender);
    }

}
