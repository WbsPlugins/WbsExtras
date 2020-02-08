package wbs.extras.commands.misc;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsPlugin;
import wbs.extras.util.WbsStrings;

public class EditBookCommand extends WbsMessenger implements CommandExecutor {

	public EditBookCommand(WbsPlugin plugin) {
		super(plugin);
	}
	
	private String permission = "wbsextras.editbook";
	private String otherPerm = permission + ".other";
	
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
			sendMessage("Hold a written book.", player);
			return true;
		}
		
		ItemMeta meta;
		if (item.hasItemMeta()) {
			meta = item.getItemMeta(); 
		} else {
			sendMessage("The book has no metadata.", sender);
			return true;
		}
		
		if (meta instanceof BookMeta) {
			BookMeta bookMeta = (BookMeta) meta;
			
			String author = bookMeta.getAuthor();
			if (author == null || !author.equalsIgnoreCase(player.getName())) {
				if (!checkPermission(player, otherPerm)) {
					return true;
				}
			}

			List<String> pages = bookMeta.getPages();
			
			List<String> colouredPages = new LinkedList<>();
			for (String page : pages) {
				colouredPages.add(WbsStrings.undoColourText(page));
			}
			
			bookMeta.setPages(colouredPages);
			
			item.setType(Material.WRITABLE_BOOK);
			item.setItemMeta(bookMeta);
			sendMessage("Book unlocked!", player);
		} else {
			sendMessage("&wSomething went wrong.", player);
		}
		
		return true;
	}

}
