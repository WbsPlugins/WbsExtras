package wbs.extras.player;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;

import wbs.utils.util.WbsTime;
import wbs.utils.util.string.RomanNumerals;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

public class PlayerData implements Serializable {

	private static final long serialVersionUID = -1825325812023504431L;

	private static WbsExtras plugin;
	private static Logger logger;
	private static ExtrasSettings settings;
	
	private static PlayerStore store;
	
	public static void setPlugin(WbsExtras plugin) {
		PlayerData.plugin = plugin; 
		logger = plugin.getLogger();
		settings = plugin.settings;
		
		store = PlayerStore.getInstance();
	}

	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/

	PlayerData(Player player) {
		username = player.getName();
		if (!store.addPlayer(this)) {
			logger.warning("A SerializablePlayer object was instantiated when the data already existed.");
		}
	}
	
	PlayerData(String username) {
		this.username = username;
		if (!store.addPlayer(this)) {
			logger.warning("A SerializablePlayer object was instantiated when the data already existed.");
		}
	}
	
	private String username;
	
	public String getName() {
		return username;
	}
	
	/************************************************/
	/*                Transient values				*/
	/************************************************/

	public transient String signEditString = null;
	public transient byte signEditLine = 0;
	
	public transient Material signType = null;
	
	/************************************************/
	/*                 Chat Notifier                */
	/************************************************/

	public boolean doChatNotifications = true;
	
	public boolean needsTag = false;
	
	List<String> triggers = new LinkedList<>();
	
	public List<String> getTriggers() {
		return triggers;
	}
	
	public boolean addTrigger(String trigger) {
		for (String existing : triggers) {
			if (trigger.equalsIgnoreCase(existing)) {
				return false;
			}
		}
		
		triggers.add(trigger);
		return true;
	}

	public boolean removeTrigger(String trigger) {
		for (String existing : triggers) {
			if (trigger.equalsIgnoreCase(existing)) {
				triggers.remove(existing);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasTrigger(String trigger) {
		for (String existing : triggers) {
			if (trigger.equalsIgnoreCase(existing)) {
				return true;
			}
		}
		
		return false;
	}
	
	/************************************************/
	/*                  COMMAND SPY                 */
	/************************************************/
	
	// The commands to watch for this player
	List<String> spyCommands = new LinkedList<>();
	
	public boolean addSpyCommand(String newCommand) {
		for (String existing : spyCommands) {
			if (newCommand.equalsIgnoreCase(existing)) {
				return false;
			}
		}

		store.watchCommand(newCommand, this);
		spyCommands.add(newCommand);
		return true;
	}
	
	public boolean removeSpyCommand(String existingCommand) {
		for (String existing : spyCommands) {
			if (existingCommand.equalsIgnoreCase(existing)) {
				store.unwatchCommand(existingCommand, this);
				spyCommands.remove(existing);
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getSpyCommands() {
		return spyCommands;
	}
	
	// The usernames of players that this player is watching
	List<String> spyUsernames = new LinkedList<>();

	public boolean addSpyPlayer(String player) {
		for (String existing : spyUsernames) {
			if (player.equalsIgnoreCase(existing)) {
				return false;
			}
		}
		
		spyUsernames.add(player);
		store.watchPlayer(player, this);
		return true;
	}
	
	public boolean removeSpyPlayer(String player) {
		for (String existing : spyUsernames) {
			if (player.equalsIgnoreCase(existing)) {
				spyUsernames.remove(existing);
				store.unwatchPlayer(player, this);
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getSpyPlayers() {
		return spyUsernames;
	}
	
	/************************************************/
	/*                  ITEM HISTORY                */
	/************************************************/

	private transient List<BaseComponent[]> itemHistory = null;

	public List<BaseComponent[]> getLastNItemInteractions(int n) {
		List<BaseComponent[]> returnList = new LinkedList<>();
		if (itemHistory == null || itemHistory.isEmpty()) {
			return returnList;
		}
		
		int length = itemHistory.size();
		for (int i = 1; i <= n && i <= length; i++) {
			returnList.add(itemHistory.get(length - i)); 
		}
		
		return returnList;
	}
	
	public void addItemInteraction(ItemStack item, String typeString) {
		if (itemHistory == null) {
			itemHistory = new LinkedList<>();
		}
		
		String itemName = ChatColor.WHITE + "<" + ChatColor.AQUA;
		ItemMeta meta = item.getItemMeta();
		if (meta != null && meta.hasDisplayName()) {
			itemName += meta.getDisplayName();
		} else {
			itemName += WbsStrings.capitalizeAll(item.getType().toString().replaceAll("_", " "));
		}
		
		itemName += ChatColor.AQUA + " x" + item.getAmount() + ChatColor.WHITE + ">";
		
		String fullItemString = itemName;
		
		if (meta != null) {
			Map<Enchantment, Integer> enchants = meta.getEnchants();
			if (enchants != null) {
				for (Enchantment ench : enchants.keySet()) {
					fullItemString += "\n" + ChatColor.GRAY + "" + WbsStrings.capitalizeAll(ench.getKey().toString().substring(10))
								+ " " + RomanNumerals.toRoman(enchants.get(ench));
				}
			}
		}
		
		if (meta != null) {
			List<String> lore = meta.getLore();
			if (lore != null) {
				for (String loreLine : lore) {
					fullItemString += "\n" + ChatColor.DARK_PURPLE + ""
								+ ChatColor.ITALIC + loreLine;
				}
			}
		}
		
		fullItemString += "\n" + ChatColor.DARK_GRAY + item.getType().getKey();
		
		BaseComponent[] hoverText = TextComponent.fromLegacyText(fullItemString);

		HoverEvent event = new HoverEvent(Action.SHOW_TEXT, hoverText);
		
		BaseComponent[] textChat = TextComponent.fromLegacyText(ChatColor.GRAY + typeString + " " + itemName);
		
		
		for (BaseComponent component : textChat) {
			component.setHoverEvent(event);
		}
		
		BaseComponent[] itemText = 
				new ComponentBuilder("").append(textChat)
				.append(" (" + WbsStringify.toString(LocalDateTime.now()) + ")").color(ChatColor.GRAY)
				.create();
		
		itemHistory.add(itemText);
		
		if (itemHistory.size() > settings.getItemHistoryBufferSize()) {
			itemHistory.remove(0);
		}
	}

	/************************************************/
	/*					LAST COMMAND				*/
	/************************************************/
	
	
	List<String> lastCommands = null;
	
	public List<String> getLastNCommands(int n) {
		List<String> returnList = new LinkedList<>();
		if (lastCommands == null || lastCommands.isEmpty()) {
			return returnList;
		}
		
		int length = lastCommands.size();
		for (int i = 1; i <= n && i <= length; i++) {
			returnList.add(lastCommands.get(length - i)); 
		}
		
		return returnList;
	}
	
	public void addCommand(String command) {
		if (lastCommands == null) {
			lastCommands = new LinkedList<>();
		}
		
		lastCommands.add(command);
		
		if (lastCommands.size() > settings.getLastCommandBufferSize()) {
			lastCommands.remove(0);
		}
	}
}
