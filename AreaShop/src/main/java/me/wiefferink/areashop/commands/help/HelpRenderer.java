package me.wiefferink.areashop.commands.help;

import de.unknowncity.astralib.paper.api.message.PaperMessenger;
import org.bukkit.command.CommandSender;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class HelpRenderer {

    private final PaperMessenger messenger;
    private final List<HelpProvider> providers;

    public HelpRenderer(@Nonnull PaperMessenger messenger, @Nonnull List<? extends HelpProvider> providers) {
        this.providers = List.copyOf(providers);
        this.messenger = messenger;
    }

    public void showHelp(@Nonnull CommandSender target) {
        if (!target.hasPermission("areashop.help")) {
            this.messenger.sendMessage(target, NodePath.path("exception", "no-permission"));
            return;
        }
        // Add all messages to a list
        List<String> messages = new ArrayList<>();
        this.messenger.sendMessage(target, NodePath.path("help-header"));
        this.messenger.sendMessage(target, NodePath.path("help-alias"));
        for (HelpProvider provider : providers) {
            String help = provider.getHelpKey(target);
            if (help != null && !help.isEmpty()) {
                messages.add(help);
            }
        }
        // Send the messages to the target
        for (String message : messages) {
            this.messenger.sendMessage(target, NodePath.path(message));
        }
    }

}
