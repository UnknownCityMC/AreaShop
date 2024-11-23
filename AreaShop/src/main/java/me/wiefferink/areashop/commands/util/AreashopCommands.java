package me.wiefferink.areashop.commands.util;

import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.commands.*;
import me.wiefferink.areashop.commands.help.HelpRenderer;
import me.wiefferink.areashop.commands.util.commandsource.CommandSource;
import me.wiefferink.areashop.commands.util.commandsource.CommandSourceMapper;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.handling.ExceptionController;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.processors.cache.GuavaCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AreashopCommands {

    private static final List<Class<? extends AreashopCommandBean>> COMMAND_CLASSES = List.of(
            AddCommand.class,
            AddFriendCommand.class,
            AddSignCommand.class,
            InfoCommand.class,
            BuyCommand.class,
            DelCommand.class,
            DelFriendCommand.class,
            DelSignCommand.class,
            FindCommand.class,
            GroupAddCommand.class,
            GroupDelCommand.class,
            GroupInfoCommand.class,
            GroupListCommand.class,
            HelpCommand.class,
            AdminInfoCommand.class,
            AdminInfoBaseCommand.class,
            AdminInfoPlayerCommand.class,
            AdminInfoRegionCommand.class,
            LinkSignsCommand.class,
            MeCommand.class,
            MessageCommand.class,
            QuickBuyCommand.class,
            QuickDeleteCommand.class,
            QuickRentCommand.class,
            ReloadCommand.class,
            RentCommand.class,
            ResellCommand.class,
            SchematicEventCommand.class,
            SellCommand.class,
            SetDurationCommand.class,
            SetLandlordCommand.class,
            SetOwnerCommand.class,
            SetPriceCommand.class,
            SetRestoreCommand.class,
            SetTeleportCommand.class,
            SetTransferCommand.class,
            StackCommand.class,
            StopResellCommand.class,
            TeleportCommand.class,
            ToggleHomeCommand.class,
            TransferCommand.class,
            UnrentCommand.class
    );

    private final AreaShop plugin;

    private final Injector injector;
    private final PaperCommandManager<CommandSource<?>> commandManager;

    private final List<AreashopCommandBean> commands = new ArrayList<>();
    private HelpRenderer helpRenderer;

    @Inject
    AreashopCommands(@Nonnull Injector injector, @Nonnull AreaShop plugin) {
        this.injector = injector;
        this.commandManager = PaperCommandManager.builder(new CommandSourceMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
        this.plugin = plugin;
    }

    public void registerCommands() {
        this.commands.clear();
        initCommandManager();
        var builder = this.commandManager.commandBuilder("plot", "p");
        for (Class<? extends AreashopCommandBean> commandClass : COMMAND_CLASSES) {
            AreashopCommandBean commandBean = injector.getInstance(commandClass);
            this.commands.add(commandBean);
            var configuredBuilder = commandBean.configureCommand(builder);
            this.commandManager.command(configuredBuilder);
        }
        // Show help by default
        this.commandManager.command(builder.handler(context -> showHelp(context.sender().sender())));
        this.helpRenderer = new HelpRenderer(this.plugin.messenger(), this.commands);
    }

    private void initCommandManager() {
        ExceptionController<CommandSource<?>> exceptionController = this.commandManager.exceptionController();
        // We need to unwrap ArgumentParseException because they wrap the custom exception messages
        exceptionController.registerHandler(ArgumentParseException.class,
                ExceptionHandler.unwrappingHandler(AreaShopCommandException.class));
        exceptionController.registerHandler(CommandExecutionException.class,
                ExceptionHandler.unwrappingHandler(AreaShopCommandException.class));
        exceptionController.registerHandler(InvalidCommandSenderException.class,
                new InvalidCommandSenderHandler(this.plugin.messenger()));
        exceptionController.registerHandler(AreaShopCommandException.class,
                new ArgumentParseExceptionHandler<>(this.plugin.messenger()));
        var confirmationConfiguration = ConfirmationConfiguration.<CommandSource<?>>builder()
                .cache(GuavaCache.of(CacheBuilder.newBuilder().build()))
                .noPendingCommandNotifier(x -> {
                })
                .confirmationRequiredNotifier((x, y) -> {
                })
                .build();
        var confirmationManager = ConfirmationManager.confirmationManager(confirmationConfiguration);
        commandManager.registerCommandPostProcessor(confirmationManager.createPostprocessor());
    }

    public void showHelp(@Nonnull CommandSender sender) {
        if (this.helpRenderer == null) {
            throw new IllegalStateException("Command handler not yet initialized!");
        }
        this.helpRenderer.showHelp(sender);
    }
}
