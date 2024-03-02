package me.wiefferink.areashop.commands.cloud;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.CommandAreaShop;
import me.wiefferink.areashop.commands.util.AreaShopCommandException;
import me.wiefferink.areashop.features.signs.RegionSign;
import me.wiefferink.areashop.features.signs.SignManager;
import me.wiefferink.areashop.tools.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.bean.CommandProperties;
import org.incendo.cloud.context.CommandContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class DelSignCloudCommand extends CloudCommandBean {

	private final SignManager signManager;
	private final MessageBridge messageBridge;
	@Inject
	public DelSignCloudCommand(@Nonnull MessageBridge messageBridge, @Nonnull SignManager signManager) {
		this.signManager = signManager;
		this.messageBridge = messageBridge;
	}

	@Override
	public String stringDescription() {
		return null;
	}


	@Override
	protected @NonNull CommandProperties properties() {
		return CommandProperties.of("deletesign", "delsign");
	}

	@Override
	protected Command.@NonNull Builder<? extends CommandSender> configureCommand(Command.@NonNull Builder<CommandSender> builder) {
		return builder.literal("deletesign", "delsign")
				.permission("delsign-noPermission")
				.senderType(Player.class)
				.handler(this::handleCommand);
	}

	private void handleCommand(@Nonnull CommandContext<Player> context) {
		Player sender = context.sender();
		// Get the sign
		Block block = null;
		BlockIterator blockIterator = new BlockIterator(sender, 100);
		while(blockIterator.hasNext() && block == null) {
			Block next = blockIterator.next();
			if(!next.getType().isAir()) {
				block = next;
			}
		}
		if(block == null || !Materials.isSign(block.getType())) {
			throw new AreaShopCommandException("delsign-noSign");
		}
		Optional<RegionSign> optionalSign = signManager.removeSign(block.getLocation());
		if(optionalSign.isEmpty()) {
			throw new AreaShopCommandException("delsign-noRegion");
		}
		RegionSign regionSign = optionalSign.get();
		messageBridge.message(sender, "delsign-success", regionSign.getRegion());
		regionSign.remove();
		// Sometimes the RegionSign data is corrupted. Forcefully set the block to air
		block.setType(Material.AIR);
	}

}










