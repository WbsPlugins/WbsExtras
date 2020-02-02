package wbs.extras.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.util.WbsMessenger;

public class SignEditCommand extends WbsMessenger implements CommandExecutor, TabCompleter  {

	private ExtrasSettings settings;
	public SignEditCommand(WbsExtras plugin) {
		super(plugin);
		
		settings = plugin.settings;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sendMessage("Usage: &b/signedit <line> <text>", sender);
			return true;
		}
		
		if (args.length == 1) {
			sendMessage("Usage: &b/signedit " + args[0] + " <text>", sender);
			return true;
		}
		
		byte line;
		
		try {
			line = Byte.parseByte(args[0]);
		} catch (NumberFormatException e) {
			sendMessage("Invalid line number. Use 1-4.", sender);
			return true;
		}
		
		
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return null;
	}

}
