package wbs.extras.commands.misc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsPlugin;

public class DisplayCommand extends WbsMessenger implements CommandExecutor, TabCompleter {
	
    public DisplayCommand(WbsPlugin plugin) {
		super(plugin);
		
		permission = plugin.getCommand("display").getPermission();
	}

    private String permission = "";
    
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
 		switch (args[0].toUpperCase()) {
 		case "NAME": 
 			if (!checkPermission(sender, permission + ".name")) {
 				return true;
 			}
 			if (args.length >= 2) {
 				String[] nameStrings = new String[args.length - 1];
				for (int i = 1; i < args.length; i++) {
					nameStrings[i-1] = args[i];
				}
				String nameString = String.join(" ", nameStrings);
 				meta.setDisplayName(nameString.replace("&", "§"));
 			} else {
 				sendMessage("Usage: &b/display name <new name>", sender);
 			}
 			item.setItemMeta(meta);
 			break;
 		case "SHINY": 
 			if (!checkPermission(sender, permission + ".shiny")) {
 				return true;
 			}
 			boolean add = false;
 			if (args.length >= 2) {
 				switch (args[1].toUpperCase()) {
 				case "TRUE":
 				case "YES":
 				case "ON":
 				case "ADD":
 					add = true;
 					break;
 				case "FALSE":
 				case "NO":
 				case "OFF":
 				case "REMOVE":
 					add = false;
 					break;
 				default:
 	 				sendMessage("Usage: &b/display shiny <true|false>", sender);
 	 				return true;
 				}
 			} else {
	 			sendMessage("Usage: &b/display shiny <true|false>", sender);
 			}
 			if (add) {
     			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
 			} else {
     			meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
 			}

 			item.setItemMeta(meta);

 			Enchantment ench = (item.getType() == Material.TRIDENT) ? Enchantment.WATER_WORKER : Enchantment.LOYALTY;
 			if (add) {
 	 			item.addUnsafeEnchantment(ench, 1);
 			} else {
 	 			item.removeEnchantment(ench);
 			}
 			break;
 		case "LORE": 
 			if (!checkPermission(sender, permission + ".lore")) {
 				return true;
 			}
 			if (args.length >= 2) {

				String[] loreStrings = new String[args.length - 1];
				for (int i = 1; i < args.length; i++) {
					loreStrings[i-1] = args[i];
				}
				String loreString = String.join(" ", loreStrings);
 				String[] lines = loreString.split("\\|\\|");
 				List<String> listLines = new ArrayList<>();
 				for (String line : lines) {
 					listLines.add(line.replace("&", "§"));
 				}
 				meta.setLore(listLines);
 			} else {
 				sendMessage("Usage: &b/display lore <new lore>", sender);
 			}
 			item.setItemMeta(meta);
 			break;
 		}
	return true;
    }
    
	private final String[] OPTIONS = {"name", "shiny", "lore"};
    
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    	List<String> choices = new LinkedList<>();

		if (sender.hasPermission(permission)) {
			int length = args.length;
			if (length == 1) {
				for (String option : OPTIONS) {
		 			if (sender.hasPermission(permission + "." + option)) {
						choices.add(option);
		 			}
				}
			}
			
			if (length == 2) {
				if (args[0].equalsIgnoreCase("shiny")) {
					choices.add("true");
					choices.add("false");
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
