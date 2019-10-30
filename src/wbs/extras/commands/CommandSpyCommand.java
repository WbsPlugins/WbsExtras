package wbs.extras.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsPlugin;

public class CommandSpyCommand extends WbsMessenger implements CommandExecutor, TabCompleter  {

	private ExtrasSettings settings;
	public CommandSpyCommand(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}

	private final String permission = "wbsextras.staff.commandspy";
	private final String permissionMessage = "&wYou do not have access to this command!";
	private final String usage = "Usage: &h/commandspy <commands|players> <add|list|remove> [args]";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!settings.doCommandSpy()) {
			sendMessage("&wThis command is disabled in the config.", sender);
			return true;
		}
		
		if (!sender.hasPermission(permission)) {
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sendMessage("This command is only usable by players.", sender);
		}
		Player player = (Player) sender;
		
		int length = args.length;
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		boolean playerSpy = false; // true to spy on players, false to spy on commands
		
		if (length >= 1) {
			switch (args[0].toLowerCase()) {
			case "player":
			case "players":
				playerSpy = true;
				break;
			case "command":
			case "commands":
				playerSpy = false;
				break;
			default:
				sendMessage(usage, sender);
				return true;
			}
		}
		
		if (length >= 2) {
			
			switch (args[1].toLowerCase()) {
			case "add":
				if (length == 2) {
					sendMessage("Usage: &h/commandspy " + args[0] + " " + args[1] + " " + (playerSpy ? "<player>" : "<command to spy on>"), sender);
					return true;
				}
				break;
			case "remove":
				if (length == 2) {
					sendMessage("Usage: &h/commandspy " + args[0] + " " + args[1] + " " + (playerSpy ? "<player>" : "<command to remove>"), sender);
					return true;
				}
				break;
			case "list":
				showList(args, player);
				return true;
			default:
				sendMessage("Usage: &h/commandspy " + args[0] + " <add|list|remove>", sender);
				return true;
			}
		} else {
			sendMessage("Usage: &h/commandspy " + args[0] + " <add|list|remove>", sender);
			return true;
		}

		if (length >= 3) {
			switch (args[1].toLowerCase()) {
			case "add":
				addCommand(args, player);
				break;
			case "remove":
				removeCommand(args, player);
				break;
			}
		}
		
		return true;
	}

	private void removeCommand(String[] args, Player player) {
		PlayerData data = PlayerData.getPlayerData(player);

		switch (args[0].toLowerCase()) {
		case "player":
		case "players":
			if (!data.removeSpyPlayer(args[2])) {
				sendMessage("You are not spying on that player!", player);
			} else {
				sendMessage("You will no longer see when " + args[2] + " runs commands.", player);
			}
			break;
		case "command":
		case "commands":
			if (!data.removeSpyCommand(args[2])) {
				sendMessage("You are not spying on that command!", player);
			} else {
				sendMessage("You will no longer see when a player runs &h/" + args[2], player);
			}
			break;
		}
	}
	
	private void addCommand(String[] args, Player player) {
		PlayerData data = PlayerData.getPlayerData(player);

		switch (args[0].toLowerCase()) {
		case "player":
		case "players":
			if (!data.addSpyPlayer(args[2])) {
				sendMessage("You are already spying on that player!", player);
			} else {
				sendMessage("You will now see when " + args[2] + " runs a command!", player);
			}
			break;
		case "command":
		case "commands":
			if (!data.addSpyCommand(args[2])) {
				sendMessage("You are already spying on that command!", player);
			} else {
				sendMessage("You will now see when a player runs &h/" + args[2], player);
			}
			break;
		}
	}
	
	private void showList(String[] args, Player player) {
		PlayerData data = PlayerData.getPlayerData(player);

		switch (args[0].toLowerCase()) {
		case "player":
		case "players":
			Set<String> watchedPlayers = data.getSpyPlayers();
			if (watchedPlayers.isEmpty()) {
				sendMessage("You are not currently watching any players commands!", player);
			} else {
				sendMessage("Players you're watching: ", player);
				int index = 1;
				for (String watched : watchedPlayers) {
					sendMessage(index + ") &h" + watched, player);
					index++;
				}
			}
			break;
		case "command":
		case "commands":
			Set<String> watchedCommands = data.getSpyCommands();
			if (watchedCommands.isEmpty()) {
				sendMessage("You are not currently watching any commands!", player);
			} else {
				sendMessage("Commands you're watching: ", player);
				int index = 1;
				for (String watched : watchedCommands) {
					sendMessage(index + ") &h/" + watched, player);
					index++;
				}
			}
			break;
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> choices = new LinkedList<>();

		
		// TODO Update tab complete from default lastcommand syntax
		if (sender.hasPermission(permission)) {
			int length = args.length;
			if (length == 1) {
				choices.add("players");
				choices.add("commands");
			}
			
			if (length == 2) {
				choices.add("add");
				choices.add("remove");
				choices.add("list");
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
