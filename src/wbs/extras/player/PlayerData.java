package wbs.extras.player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.util.RomanNumerals;
import wbs.extras.util.WbsStrings;
import wbs.extras.util.WbsTime;

public class PlayerData {

	private static Map<String, PlayerData> allData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	public static Map<String, PlayerData> allData() {
		return allData;
	}

	public static boolean exists(Player player) {
		return exists(player.getName());
	}
	public static boolean exists(String username) {
		return allData.containsKey(username);
	}

	public static PlayerData getPlayerData(Player player) {
		String username = player.getName();
		return getPlayerData(username);
	}
	
	public static PlayerData getPlayerData(String username) {
		if (exists(username)) {
			return allData.get(username);
		}
		
		return new PlayerData(username);
	}
	
	public static void saveAll() {
		for (PlayerData data : allData.values()) {
			String username = data.getName();
			File playerFile = new File(plugin.getDataFolder(), "player_data" + File.separator + username + ".yml");
			
			try {
				data.toConfig().save(playerFile);
			} catch (IOException e) {
				logger.warning("Player " + username + " was not able to save.");
			}
		}
	}
	
	public static void loadAll() {
		File playerDir = new File(plugin.getDataFolder() + File.separator + "player_data");
		if (!playerDir.exists()) {
			logger.warning("Player directory didn't exist. Creating...");
			
			playerDir.mkdir();
		}
		for (File file : playerDir.listFiles()) {
			if (file.getName().endsWith(".yml")) {
				new PlayerData(file);
			}
		}
	}
	
	private static WbsExtras plugin;
	private static Logger logger;
	private static ExtrasSettings settings;
	public static void setPlugin(WbsExtras plugin) {
		PlayerData.plugin = plugin;
		logger = plugin.getLogger();
		settings = plugin.settings;
	}
	
	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/
	
	private String username;

	private PlayerData(Player player) {
		username = player.getName();
		allData.put(username, this);
	}
	
	private PlayerData(String username) {
		this.username = username;
		allData.put(username, this);
	}
	
	public PlayerData(File dataFile) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

		username = config.getString("username");
		lastCommands = config.getStringList("lastCommands");
		
		allData.put(username, this);
	}
	
	public YamlConfiguration toConfig() {
		YamlConfiguration config = new YamlConfiguration();

		config.set("username", username);
		config.set("lastCommands", lastCommands);
		
		return config;
	}
	
	public String getName() {
		return username;
	}
	/************************************************/
	/*					ITEM HISTORY				*/
	/************************************************/

	private List<BaseComponent[]> itemHistory = null;

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
				.append(" (" + WbsTime.prettyTime(LocalDateTime.now()) + ")").color(ChatColor.GRAY)
				.create();
		
		itemHistory.add(itemText);
		
		if (itemHistory.size() > settings.getItemHistoryBufferSize()) {
			itemHistory.remove(0);
		}
	}

	/************************************************/
	/*					LAST COMMAND				*/
	/************************************************/
	
	
	private List<String> lastCommands = null;
	
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