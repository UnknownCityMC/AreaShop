package me.wiefferink.areashop.commands;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.util.RegionParseUtil;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.commands.util.commandsource.PlayerCommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.NodePath;

import static me.wiefferink.areashop.commands.parser.GeneralRegionParser.generalRegionParser;

@Singleton
public class InfoCommand extends AreashopCommandBean {
    private final PaperMessenger messenger;
    private final IFileManager fileManager;
    private final AreaShop areaShop;

    @Inject
    public InfoCommand(PaperMessenger messenger, IFileManager fileManager, AreaShop areaShop) {
        this.messenger = messenger;
        this.fileManager = fileManager;
        this.areaShop = areaShop;
    }

    @Override
    public String stringDescription() {
        return "";
    }

    @Override
    protected @NotNull Command.Builder<? extends CommandSource<?>> configureCommand(@NotNull Command.Builder<CommandSource<?>> builder) {
        return builder.literal("info")
                .permission("areashop.info")
                .senderType(PlayerCommandSource.class)
                .optional("region", generalRegionParser(fileManager))
                .handler(this::handleCommand);
    }

    private void handleCommand(@NonNull CommandContext<PlayerCommandSource> context) {
        Player sender = context.sender().sender();
        GeneralRegion region = RegionParseUtil.getOrParseRegion(context, sender);

        sender.sendMessage("Bla1");
        Bukkit.getServer().getScheduler().runTask(areaShop, () -> {
            sender.sendMessage("Bla2");
            messenger.sendMessage(sender, NodePath.path("command", "info"));
        });
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
