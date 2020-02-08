package wbs.extras.commands.misc;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsPlugin;

public class ItemDataCommand extends WbsMessenger implements CommandExecutor, TabCompleter {

	protected ItemDataCommand(WbsPlugin plugin) {
		super(plugin);
		
		permission = plugin.getCommand("display").getPermission();
	}

    private String permission = null;
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		
		return null;
	}

}
