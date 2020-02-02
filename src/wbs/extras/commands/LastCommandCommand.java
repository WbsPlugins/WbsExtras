package wbs.extras.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerStore;
import wbs.extras.util.WbsMessenger;

public class LastCommandCommand extends WbsMessenger implements CommandExecutor, TabCompleter {

	private ExtrasSettings settings;
	public LastCommandCommand(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}
	
	private final String permission = "wbsextras.staff.lastcommand";
	private final String permissionMessage = "&wYou do not have access to this command!";
	private final String usage = "Usage: &h/lastcommand <username> [amount]";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!checkPermission(sender, permission)) {
			return true;
		}

		if (!settings.logLastCommand()) {
			sendMessage("&wThis command is disabled in the config.", sender);
			return true;
		}

		int length = args.length;
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		String username = args[0];
		PlayerStore store = PlayerStore.getInstance();
		
		if (!store.exists(username)) {
			sendMessage("No commands logged for that player.", sender);
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
		
		PlayerData data = store.getPlayerData(username);
		
		List<String> toDisplay = data.getLastNCommands(amount);

		sendMessage("&6" + username + "'s last &h" + amount + "&6 commands were:", sender);
		
		for (String commandString : toDisplay) {
			sendMessageNoPrefix("&h" + commandString, sender);
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
			
			if (length == 2) {
				choices.add("1");
				choices.add(settings.getLastCommandBufferSize() + "");
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





