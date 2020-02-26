package wbs.extras.commands.misc;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

public class ItemDataCommand extends WbsMessenger implements CommandExecutor, TabCompleter {

	public ItemDataCommand(WbsPlugin plugin) {
		super(plugin);
		
		permission = plugin.getCommand("itemdata").getPermission();
	}

    private String permission = null;
    
    private enum ItemDataArg {
    	CAN_PLACE_ON, CAN_BREAK;
    }
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sendMessage("Usage: &b/display <name|lore|shiny>", sender);
			return true;
		}
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR) {
			sendMessage("Hold the item to change.", player);
			return true;
		}
		ItemMeta meta;
		if (item.hasItemMeta()) {
			meta = item.getItemMeta(); 
		} else {
			meta = Bukkit.getItemFactory().getItemMeta(item.getType());
		}
		
		
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

		
		return null;
	}

}
