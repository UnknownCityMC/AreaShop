package me.wiefferink.areashop.commands;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.unknowncity.astralib.common.structure.KeyValue;
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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.NodePath;

import java.util.Objects;

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

        var regionSize = getRegionSize(region.getRegion());

        messenger.sendMessage(
                sender,
                NodePath.path("command", "info"),
                Placeholder.parsed("id", region.getName()),
                Placeholder.parsed("owner", Objects.requireNonNullElse(region.getOwnerName(), "N/A")),
                Placeholder.parsed("landlord", Objects.requireNonNullElse(region.getLandlordName(), "N/A")),
                Placeholder.parsed("length", String.valueOf(regionSize.key())),
                Placeholder.parsed("depth", String.valueOf(regionSize.value())),
                Placeholder.parsed("trusted", region.getFriendsFeature().getFriendNames().stream().reduce((string, string2) -> String.join(", ", string, string2)).orElse("N/A")),
                Placeholder.parsed("banned", "N/A")
        );
    }

    @Override
    public @Nullable String getHelpKey(@NotNull CommandSender target) {
        return "";
    }

    @Override
    protected @NonNull CommandProperties properties() {
        return null;
    }

    private KeyValue<Integer, Integer> getRegionSize(ProtectedRegion region) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        BlockVector3 max = region.getMaximumPoint();
        BlockVector3 min = region.getMinimumPoint();
        int xDim = (int) (double) Math.abs(max.x() - min.x()) + 1;
        int zDim = (int) (double) Math.abs(max.z() - min.z()) + 1;

        return KeyValue.of(xDim, zDim);
    }
}
