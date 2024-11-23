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
import me.wiefferink.areashop.regions.RegionFactory;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.tools.Utils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static me.wiefferink.areashop.commands.parser.GeneralRegionParser.generalRegionParser;

@Singleton
public class GroupDelCommand extends AreashopCommandBean {
    
    private static final CloudKey<String> KEY_GROUP = CloudKey.of("group", String.class);

    private final IFileManager fileManager;
    private final RegionFactory regionFactory;
    private final MessageBridge messageBridge;

    @Inject
    public GroupDelCommand(
            @Nonnull MessageBridge messageBridge,
            @Nonnull IFileManager fileManager,
            @Nonnull RegionFactory regionFactory

    ) {
        this.messageBridge = messageBridge;
        this.fileManager = fileManager;
        this.regionFactory = regionFactory;
    }

    @Override
    public String stringDescription() {
        return null;
    }

    @Override
    public String getHelpKey(@NotNull CommandSender target) {
        if(target.hasPermission("areashop.groupdel")) {
            return "help-groupdel";
        }
        return null;
    }

    @Override
    protected @Nonnull Command.Builder<? extends CommandSource<?>> configureCommand(@Nonnull Command.Builder<CommandSource<?>> builder) {
        return builder.literal("group")
                .literal("delete")
                .required(KEY_GROUP, StringParser.stringParser(), this::suggestGroupNames)
                .optional("region", generalRegionParser(fileManager))
                .handler(this::handleCommand);
    }

    @Override
    protected @Nonnull CommandProperties properties() {
        return CommandProperties.of("groupdel");
    }

    private void handleCommand(@Nonnull CommandContext<CommandSource<?>> context) {
        CommandSender sender = context.sender().sender();
        if (!sender.hasPermission("groupdel")) {
            throw new AreaShopCommandException(NodePath.path("exception", "no-permission"));
        }
        String rawGroup = context.get(KEY_GROUP);
        RegionGroup group = fileManager.getGroup(rawGroup);
        if (group == null) {
            group = regionFactory.createRegionGroup(rawGroup);
            fileManager.addGroup(group);
        }
        GeneralRegion declaredRegion = context.getOrDefault("region", null);
        if (declaredRegion != null) {
            if (!group.removeMember(declaredRegion)) {
                throw new AreaShopCommandException(NodePath.path("command", "group", "delete", "failed"),
                        Placeholder.parsed("group", group.getName()),
                        TagResolver.resolver(declaredRegion.tagResolvers())
                );
            }
            this.messageBridge.message(sender,
                    "groupdel-success",
                    group.getName(),
                    group.getMembers().size(),
                    declaredRegion);
            return;
        }

        Collection<GeneralRegion> regions = RegionParseUtil.getOrParseRegionsInSel(context, sender);
        Set<GeneralRegion> regionsSuccess = new TreeSet<>();
        Set<GeneralRegion> regionsFailed = new TreeSet<>();
        for (GeneralRegion region : regions) {
            if (group.removeMember(region)) {
                regionsSuccess.add(region);
            } else {
                regionsFailed.add(region);
            }
        }
        if (!regionsSuccess.isEmpty()) {
            messageBridge.message(sender,
                    "groupdel-weSuccess",
                    group.getName(),
                    Utils.combinedMessage(regionsSuccess, "region"));
        }
        if (!regionsFailed.isEmpty()) {
            messageBridge.message(sender,
                    "groupdel-weFailed",
                    group.getName(),
                    Utils.combinedMessage(regionsFailed, "region"));
        }
        // Update all regions, this does it in a task, updating them without lag
        fileManager.updateRegions(List.copyOf(regionsSuccess), sender);
        group.saveRequired();
    }

    private CompletableFuture<Iterable<Suggestion>> suggestGroupNames(
            @Nonnull CommandContext<CommandSource<?>> context,
            @Nonnull CommandInput input
    ) {
        String text = input.peekString();
        List<Suggestion> suggestions = this.fileManager.getGroupNames().stream()
                .filter(name -> name.startsWith(text))
                .map(Suggestion::suggestion)
                .toList();
        return CompletableFuture.completedFuture(suggestions);
    }
}










