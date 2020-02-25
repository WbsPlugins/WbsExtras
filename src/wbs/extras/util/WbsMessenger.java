package wbs.extras.util;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// This class can be extended so I can be lazy and not have to write plugin. before methods c:
public abstract class WbsMessenger {
	
	protected WbsPlugin plugin;
	public WbsMessenger(WbsPlugin plugin) {
		this.plugin = plugin;
	}
	
	protected boolean checkPermission(CommandSender sender, String permission) {
		if (!sender.hasPermission(permission)) {
			sendMessage("&wYou are lacking the permission node: &h" + permission, sender);
			return false;
		}
		return true;
	}

	protected void sendMessage(String message, CommandSender sender) {
		plugin.sendMessage(message, sender);
	}
	
	protected void sendMessageNoPrefix(String message, CommandSender sender) {
		plugin.sendMessageNoPrefix(message, sender);
	}
	
	protected void broadcast(String message) {
		plugin.broadcast(message);
	}
	
	protected void sendActionBar(String message, Player player) {
		plugin.sendActionBar(message, player);
	}
	
	protected void broadcastActionBar(String message, double radius, Location loc) {
		plugin.broadcastActionBar(message, radius, loc);
	}
}
