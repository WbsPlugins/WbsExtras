package wbs.extras.commands.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStrings;

public class RunAsOpCommand extends WbsMessenger implements CommandExecutor {

	public RunAsOpCommand(WbsPlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof ConsoleCommandSender)) {
			sendMessage("&wThis command is only runnable from console.", sender);
			return true;
		}
		
		if (args.length == 0) {
			sendMessage("Usage: /runasop <player> <command to run>", sender);
			return true;
		}
		
		String playerString = args[0];
		Player player = Bukkit.getPlayer(playerString);
		
		if (player == null) {
			sendMessage("Invalid player.", sender);
			return true;
		}
		
		if (args.length < 2) {
			sendMessage("Usage: /runasop " + playerString + " <command to run>", sender);
			return true;
		}
		
		String commandToRun = WbsStrings.combineLast(args, 1);

		sendMessage("Forcing player to run \"/" + commandToRun + "\" as if they were an operator.", sender);
		if (player.isOp()) {
			player.performCommand(commandToRun);
		} else {
			player.setOp(true);
			player.performCommand(commandToRun);
			player.setOp(false);
		}
		
		return false;
	}

}
