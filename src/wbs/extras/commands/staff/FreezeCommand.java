package wbs.extras.commands.staff;

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
import wbs.extras.util.WbsMessenger;
import wbs.extras.WbsExtras;

public class FreezeCommand extends WbsMessenger implements CommandExecutor, TabCompleter {

	private ExtrasSettings settings;
	protected FreezeCommand(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}

	private final String permission = "wbsextras.staff.freeze";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
			
		
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
