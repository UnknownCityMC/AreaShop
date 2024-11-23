package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.adapters.platform.OfflinePlayerHelper;
import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.parser.OfflinePlayerParser;
import me.wiefferink.areashop.commands.util.RegionParseUtil;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.features.FriendsFeature;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.BukkitSchedulerExecutor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static me.wiefferink.areashop.commands.parser.GeneralRegionParser.generalRegionParser;

@Singleton
public class DelFriendCommand extends AreashopCommandBean {


    private static final CloudKey<String> KEY_PLAYER = CloudKey.of("player", String.class);
    private final MessageBridge messageBridge;
    private final OfflinePlayerHelper offlinePlayerHelper;
    private final BukkitSchedulerExecutor executor;
    private final IFileManager fileManager;

    @Inject
    public DelFriendCommand(
            @Nonnull MessageBridge messageBridge,
            @Nonnull IFileManager fileManager,
            @Nonnull OfflinePlayerHelper offlinePlayerHelper,
            @Nonnull BukkitSchedulerExecutor executor
    ) {
        this.messageBridge = messageBridge;
        this.offlinePlayerHelper = offlinePlayerHelper;
        this.executor = executor;
        this.fileManager = fileManager;
    }

    /**
     * Check if a person can remove friends.
     *
     * @param person The person to check
     * @param region The region to check for
     * @return true if the person can remove friends, otherwise false
     */
    public static boolean canUse(CommandSender person, GeneralRegion region) {
        if (person.hasPermission("areashop.delfriendall")) {
            return true;
        }
        if (person instanceof Player player) {
            return region.isOwner(player) && player.hasPermission("areashop.delfriend");
        }
        return false;
    }

    @Override
    public String getHelpKey(@NotNull CommandSender target) {
        if (target.hasPermission("areashop.delfriendall") || target.hasPermission("areashop.delfriend")) {
            return "help-delFriend";
        }
        return null;
    }

    @Override
    public String stringDescription() {
        return null;
    }

    @Override
    protected @Nonnull CommandProperties properties() {
        return CommandProperties.of("deletefriend", "delfriend");
    }

    @Override
    protected @Nonnull Command.Builder<? extends CommandSource<?>> configureCommand(@Nonnull Command.Builder<CommandSource<?>> builder) {
        return builder.literal("delfriend")
                .required(KEY_PLAYER, OfflinePlayerParser.parser(), this::suggestFriends)
                .optional("region", generalRegionParser(fileManager))
                .handler(this::handleCommand);
    }

    private void handleCommand(@Nonnull CommandContext<CommandSource<?>> context) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("areashop.delfriend") && !sender.hasPermission("areashop.delfriendall")) {
            throw new AreaShopCommandException(NodePath.path("exception", "no-permission"));
        }
        GeneralRegion region = RegionParseUtil.getOrParseRegion(context, sender);
        this.offlinePlayerHelper.lookupOfflinePlayerAsync(context.get(KEY_PLAYER))
                .whenCompleteAsync((offlinePlayer, exception) -> {
                    if (exception != null) {
                        sender.sendMessage("failed to lookup offline player!");
                        exception.printStackTrace();
                        return;
                    }
                    if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                        this.messageBridge.message(sender, "cmd-invalidPlayer", offlinePlayer.getName());
                        return;
                    }
                    processWithSender(sender, region, offlinePlayer);
                }, this.executor);
    }

    private void processWithSender(@Nonnull CommandSender sender,
                                   @Nonnull GeneralRegion region,
                                   @Nonnull OfflinePlayer friend) {
        FriendsFeature friendsFeature = region.getFriendsFeature();
        if (sender.hasPermission("areashop.delfriendall")) {
            if ((region instanceof RentRegion rentRegion && !rentRegion.isRented())
                    || (region instanceof BuyRegion buyRegion && !buyRegion.isSold())) {
                throw new AreaShopCommandException(NodePath.path("command", "delfriend", "no-owner"), region.tagResolvers());

            }
            if (!friendsFeature.getFriends().contains(friend.getUniqueId())) {
                throw new AreaShopCommandException(NodePath.path("command", "delfriend", "not-added"), Placeholder.parsed("friend", friend.getName()), TagResolver.resolver(region.tagResolvers()));

            }
            if (friendsFeature.deleteFriend(friend.getUniqueId(), sender)) {
                region.update();
                this.messageBridge.message(sender, "delfriend-successOther", friend.getName(), region);
            }
            return;
        }
        if (!sender.hasPermission("areashop.delfriend") || !(sender instanceof Player player)) {
            throw new AreaShopCommandException(NodePath.path("exception", "no-permission"), region.tagResolvers());
        }
        if (!region.isOwner(player)) {
            throw new AreaShopCommandException(NodePath.path("exception", "no-permission"), region.tagResolvers());
        }
        if (!friendsFeature.getFriends().contains(friend.getUniqueId())) {
            throw new AreaShopCommandException(NodePath.path("command", "delfriend", "not-added"), Placeholder.parsed("friend", friend.getName()), TagResolver.resolver(region.tagResolvers()));
        } else if (friendsFeature.deleteFriend(friend.getUniqueId(), sender)) {
            region.update();
            this.messageBridge.message(sender, "delfriend-success", friend.getName(), region);
        }

    }

    private CompletableFuture<Iterable<Suggestion>> suggestFriends(
            @Nonnull CommandContext<CommandSource<?>> context,
            @Nonnull CommandInput input
    ) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("areashop.delfriend")) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        GeneralRegion region;
        try {
            region = RegionParseUtil.getOrParseRegion(context, sender);
        } catch (AreaShopCommandException ignored) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        if (!sender.hasPermission("areashop.delfriendall")
                && sender instanceof Player player
                && !region.isOwner(player)
        ) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        String text = input.peekString();
        FriendsFeature friendsFeature = region.getFriendsFeature();
        Collection<Suggestion> suggestion = friendsFeature.getFriendNames().stream()
                .filter(name -> name.startsWith(text))
                .map(Suggestion::suggestion)
                .toList();
        return CompletableFuture.completedFuture(suggestion);
    }
}








