package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.util.AreashopCommandBean;
import me.wiefferink.areashop.commands.parser.BuyRegionParser;
import me.wiefferink.areashop.commands.util.RegionParseUtil;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.wiefferink.areashop.commands.parser.BuyRegionParser.buyRegionParser;

@Singleton
public class StopResellCommand extends AreashopCommandBean {

    private final MessageBridge messageBridge;
    private final IFileManager fileManager;

    @Inject
    public StopResellCommand(@Nonnull MessageBridge messageBridge, @Nonnull IFileManager fileManager) {
        this.messageBridge = messageBridge;
        this.fileManager = fileManager;
    }

    @Override
    public String getHelpKey(CommandSender target) {
        if (target.hasPermission("areashop.stopresellall") || target.hasPermission("areashop.stopresell")) {
            return "help-stopResell";
        }
        return null;
    }

    @Override
    public String stringDescription() {
        return null;
    }

    @Override
    protected Command.Builder<? extends CommandSource<?>> configureCommand(Command.@NotNull Builder<CommandSource<?>> builder) {
        return builder.literal("stopresell")
                .optional("region-buy", buyRegionParser(fileManager, this::suggestBuyRegions))
                .handler(this::handleCommand);
    }

    @Override
    protected @NonNull CommandProperties properties() {
        return CommandProperties.of("stopresell");
    }

    private void handleCommand(@Nonnull CommandContext<CommandSource<?>> context) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("areashop.stopresell") && !sender.hasPermission("areashop.stopresellall")) {
            this.messageBridge.message(sender, "stopresell-noPermissionOther");
            return;
        }

        BuyRegion buy = RegionParseUtil.getOrParseBuyRegion(context, sender);
        if (!buy.isInResellingMode()) {
            this.messageBridge.message(sender, "stopresell-notResell", buy);
            return;
        }
        if (sender.hasPermission("areashop.stopresellall")) {
            buy.disableReselling();
            buy.update();
            this.messageBridge.message(sender, "stopresell-success", buy);
        } else if (sender.hasPermission("areashop.stopresell") && sender instanceof Player player) {
            if (buy.isOwner(player)) {
                buy.disableReselling();
                buy.update();
                this.messageBridge.message(sender, "stopresell-success", buy);
            } else {
                this.messageBridge.message(sender, "stopresell-noPermissionOther", buy);
            }
        } else {
            this.messageBridge.message(sender, "stopresell-noPermission", buy);
        }
    }

    private CompletableFuture<Iterable<Suggestion>> suggestBuyRegions(
            @Nonnull CommandContext<CommandSource<?>> context,
            @Nonnull CommandInput input
    ) {
        String text = input.peekString();
        List<Suggestion> suggestions = this.fileManager.getBuysRef().stream()
                .filter(region -> region.isSold() && region.isInResellingMode())
                .map(GeneralRegion::getName)
                .filter(name -> name.startsWith(text))
                .map(Suggestion::suggestion)
                .toList();
        return CompletableFuture.completedFuture(suggestions);
    }
}
















