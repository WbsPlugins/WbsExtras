package wbs.extras.commands.misc;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;

import wbs.utils.util.plugin.WbsMessenger;

public class ColourBookCommand extends WbsMessenger implements CommandExecutor {

	private ExtrasSettings settings;
	public ColourBookCommand(WbsExtras plugin) {
		super(plugin);
		
		settings = plugin.settings;
		permission = plugin.getCommand("colourbook").getPermission();
		otherPerm = permission + ".other";
	}

	private String permission = "";
	private String otherPerm = "";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage("This command is only usable by players.", sender);
			return true;
		}
		
		if (!checkPermission(sender, permission)) {
			return true;
		}
		
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR) {
			sendMessage("Hold the item to change.", player);
			return true;
		}
		
		if (item.getType() != Material.WRITTEN_BOOK) {
			sendMessage("Hold a written book (not book and quill).", player);
			return true;
		}
		
		ItemMeta meta;
		if (item.hasItemMeta()) {
			meta = item.getItemMeta(); 
		} else {
			sendMessage("The book has no writing.", sender);
			return true;
		}
		
		if (meta instanceof BookMeta) {
			BookMeta bookMeta = (BookMeta) meta;
			
			if (!bookMeta.hasPages()) {
				sendMessage("The book has no writing.", sender);
				return true;
			}
			
			String author = bookMeta.getAuthor();
			if (author == null || !author.equalsIgnoreCase(player.getName())) {
				if (!checkPermission(player, otherPerm)) {
					return true;
				}
			}
			
			List<String> pages = bookMeta.getPages();
			
			List<String> colouredPages = new LinkedList<>();
			for (String page : pages) {
				colouredPages.add(ChatColor.translateAlternateColorCodes('&', page));
			}
			
			bookMeta.setPages(colouredPages);
			
			item.setItemMeta(bookMeta);
			sendMessage("Book coloured!", player);
		} else {
			sendMessage("&wSomething went wrong.", player);
		}
		
		return true;
	}
}
