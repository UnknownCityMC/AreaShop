package me.wiefferink.areashop.commands;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestCommand implements CommandExecutor {

    private AreaShop areaShop = AreaShop.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            final Location location = player.getLocation();
            ApplicableRegionSet region = areaShop.getRegionManager(player.getWorld()).getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
            for (ProtectedRegion r : region.getRegions()) {
                player.sendMessage(r.getId());
            }
        }
        return true;
    }
}
