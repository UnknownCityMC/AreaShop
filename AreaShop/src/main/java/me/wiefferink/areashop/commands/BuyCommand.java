package me.wiefferink.areashop.commands;

import de.unknowncity.astralib.paper.api.command.sender.PaperCommandSource;
import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.commands.util.*;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.commands.util.commandsource.PlayerCommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static me.wiefferink.areashop.commands.parser.BuyRegionParser.buyRegionParser;

@Singleton
public class BuyCommand extends AreashopCommandBean {

    private final IFileManager fileManager;

    @Inject
    public BuyCommand(@Nonnull IFileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public String getHelpKey(@NotNull CommandSender target) {
        if (target.hasPermission("areashop.buy")) {
            return "help-buy";
        }
        return null;
    }

    @Override
    public String stringDescription() {
        return "Allows you to buy a region";
    }

    @Override
    protected @Nonnull CommandProperties properties() {
        return CommandProperties.of("buy");
    }


    @Override
    protected @Nonnull Command.Builder<? extends CommandSource<?>> configureCommand(@Nonnull Command.Builder<CommandSource<?>> builder) {
        return builder.permission("areashop.buy")
                .senderType(PlayerCommandSource.class)
                .literal("buy")
                .optional("region-buy", buyRegionParser(fileManager))
                .handler(this::handleCommand);
    }

    private void handleCommand(@Nonnull CommandContext<PlayerCommandSource> context) {

        Player sender = context.sender().sender();
        BuyRegion region = RegionParseUtil.getOrParseBuyRegion(context, sender);
        region.buy(sender);
    }

}
