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
		
		int length = args.length;
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		String option = "";
		if (length >= 1) {
			switch (args[0].toLowerCase()) {
			case "player":
			case "players":
				break;
			case "command":
			case "commands":
				break;
			default:
				sendMessage(usage, sender);
				return true;
			}
		}
		
		if (length >= 2) {
			switch (args[1].toLowerCase()) {
			case "add":
				
				break;
			case "remove":

				break;
			case "list":

				break;
			default:
				sendMessage(usage, sender);
				return true;
				
			}
		} else {
			sendMessage(usage, sender);
			return true;
		}

		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> choices = new LinkedList<>();

		
		// TODO Update tab complete from default lastcommand syntax
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
