package wbs.extras.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import wbs.extras.util.WbsMessenger;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;

public class ItemHistoryCommand extends WbsMessenger implements CommandExecutor, TabCompleter {

	private ExtrasSettings settings;
	public ItemHistoryCommand(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}

	private final String permission = "wbsextras.staff.itemhistory";
	private final String permissionMessage = "&wYou do not have access to this command!";
	private final String usage = "Usage: &h/itemhistory <username>";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!checkPermission(sender, permission)) {
			sendMessage(permissionMessage, sender);
			return true;
		}
		
		if (!settings.doItemHistory()) {
			sendMessage("&wThis command is disabled in the config.", sender);
			return true;
		}

		int length = args.length;
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		String username = args[0];
		if (!PlayerData.exists(username)) {
			sendMessage("No item drops/pickups logged for that player.", sender);
			return true;
		}
		
		int amount = 5;
		if (length >= 2) {
			try {
				amount = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sendMessage(usage, sender);
				return true;
			}
		}
		
		PlayerData data = PlayerData.getPlayerData(username);
		
		sendMessage(ChatColor.GOLD + "Last " + amount + " item pickups/drops for " + data.getName() + ":", sender);
		for (BaseComponent[] component : data.getLastNItemInteractions(amount)) {
			sender.spigot().sendMessage(component);
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> choices = new LinkedList<>();

		if (sender.hasPermission(permission)) {
			int length = args.length;
			if (length == 1) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					choices.add(player.getName());
				}
			}
		}
		
    	List<String> result = new ArrayList<String>();
		for (String add : choices) {
    		if (add.toLowerCase().startsWith(args[args.length-1].toLowerCase())) {
    			result.add(add);
    		}
		}
		
		return result;
	}

}
