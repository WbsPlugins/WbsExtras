package wbs.extras;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import wbs.extras.configurations.BarAnnouncement;
import wbs.extras.configurations.Replacement;
import wbs.extras.util.WbsEnums;
import wbs.extras.util.WbsPlugin;
import wbs.extras.util.WbsSettings;
import wbs.extras.configurations.WbsFilter;

public class ExtrasSettings extends WbsSettings {

	public ExtrasSettings(WbsPlugin plugin) {
		super(plugin);

		reload();
	}

	/********************/
	//		SETUP		//
	/********************/
	
	public void reload() {
		loadConfigs();
		
		loadMain();
		loadChat();
		loadMisc();
        loadStaff();
	}
	
	private Map<String, FileConfiguration> configs;

	private void loadConfigs() {
		configs = new HashMap<>();
		
		File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) { 
        	plugin.saveResource("config.yml", false);
        }
        configs.put("main", loadConfigSafely(configFile));

        configs.put("staff", loadConfigSafely(genConfig("staff.yml")));
        configs.put("misc", loadConfigSafely(genConfig("misc.yml")));
        configs.put("chat", loadConfigSafely(genConfig("chat.yml")));
	}

	/********************/
	//		MAIN		//
	/********************/
	
	private void loadMain() {
		FileConfiguration main = configs.get("main");
		
        String newPrefix = main.getString("message-prefix");
        ChatColor newColour = ChatColor.getByChar(main.getString("message-colour"));
        ChatColor newHighlight = ChatColor.getByChar(main.getString("highlight-colour"));
        ChatColor newErrorColour = ChatColor.getByChar(main.getString("error-colour"));
        plugin.setDisplays(newPrefix, newColour, newHighlight, newErrorColour);
	}
	
	/********************/
	//		CHAT		//
	/********************/
	
	private void loadChat() {
		FileConfiguration chat = configs.get("chat");
		
		ConfigurationSection tabCompletions = chat.getConfigurationSection("tab-completions");
		if (tabCompletions != null) {
			for (String command : tabCompletions.getKeys(false)) {
				ConfigurationSection commandSection = tabCompletions.getConfigurationSection(command);
				
				command = command.toLowerCase();
				
				HashMultimap<Integer, String> argAdditions = HashMultimap.create();
				for (String argString : commandSection.getKeys(false)) {
					try {
						int argNumber = Integer.parseInt(argString);
						List<String> additions = commandSection.getStringList(argString);
						
						argAdditions.putAll(argNumber, additions);
					} catch (NumberFormatException e) { // Not a number; assume its "override:"
						boolean override = commandSection.getBoolean("override");
						
						tabOverrideBools.put(command, override);
					}
				}
				extraTabs.put(command, argAdditions);
			}
		}

		ConfigurationSection graveHighlighting = chat.getConfigurationSection("grave-highlighting");
		if (graveHighlighting != null) {
			doGraveHighlighting = graveHighlighting.getBoolean("enabled", false);
			highlightColour = ChatColor.getByChar(graveHighlighting.getString("colour"));
		}
		
		ConfigurationSection replacements = chat.getConfigurationSection("chat-replacements");
        if (replacements.getBoolean("enabled")) {
    		parseChatReplacements(replacements.getConfigurationSection("replacements"));
        }

		ConfigurationSection notifier = chat.getConfigurationSection("chat-notifier");
		if (notifier != null) {
			doChatNotifiers = notifier.getBoolean("enabled", false);
		}

		ConfigurationSection chatCoords = chat.getConfigurationSection("chat-coords");
		if (chatCoords != null) {
			doChatCoords = chatCoords.getBoolean("enabled", false);
			chatCoordsPerm = chatCoords.getString("permission", chatCoordsPerm);
		}

	}
	
	private Map<String, Multimap<Integer, String>> extraTabs = new HashMap<>();
	private Map<String, Boolean> tabOverrideBools = new HashMap<>();
	
	public boolean overrideTabs(String command) {
		command = command.toLowerCase();
		command = command.replaceFirst("/", "");
		if (tabOverrideBools.containsKey(command)) {
			return tabOverrideBools.get(command);
		} else {
			return false;
		}
	}
	
	public Collection<String> getExtraTabsFor(String command, int argNumber) {
		command = command.toLowerCase();
		command = command.replaceFirst("/", "");
		Multimap<Integer, String> argCompletions = extraTabs.get(command);
		if (argCompletions == null) {
			return null;
		}
		return argCompletions.get(argNumber);
	}
	
	private boolean doGraveHighlighting = false;
	public boolean doGraveHighlighting() {
		return doGraveHighlighting;
	}
	
	private ChatColor highlightColour = ChatColor.AQUA;
	public ChatColor getHighlightColour() {
		return highlightColour;
	}
	
	private void parseChatReplacements(ConfigurationSection specs) {
		for (String replacementName : specs.getKeys(false)) {
			ConfigurationSection section = specs.getConfigurationSection(replacementName);
			List<String> matches = section.getStringList("catch");
			String replace = section.getString("replace");
			boolean ignoreCase = section.getBoolean("ignore-case");
			Replacement replacement = new Replacement(replace);
			replacement.setIgnoreCase(ignoreCase);
			
			String permission = section.getString("permission", null);
			replacement.setPermission(permission);
			for (String match : matches) { 
				replacement.addMatch(match);
			}
			
			Replacement.addReplacement(replacementName, replacement);
		}
	}

	private boolean doChatNotifiers = false;
	public boolean doChatNotifiers() {
		return doChatNotifiers;
	}
	
	private boolean doChatCoords = false;
	public boolean doChatCoords() {
		return doChatCoords;
	}
	
	private String chatCoordsPerm = "wbsextras.chat.coords";
	public String getChatCoordsPermission() {
		return chatCoordsPerm;
	}

	/********************/
	//		MISC		//
	/********************/
	
	private void loadMisc() {
		FileConfiguration misc = configs.get("misc");
		
		String parentDirectory = "misc.yml/";

        ConfigurationSection damageIndicator = misc.getConfigurationSection("damage-indicator");
        if (damageIndicator.getBoolean("enabled")) {
        	doDamageIndicator = true;
        	showPlayerHealth = damageIndicator.getBoolean("show-player-health");
        }
        
        ConfigurationSection blockLavaPvp = misc.getConfigurationSection("block-lava-pvp");
        if (blockLavaPvp.getBoolean("enabled")) {
        	lavaPlaceDistance = blockLavaPvp.getDouble("distance");
        }
        

        ConfigurationSection voteSleep = misc.getConfigurationSection("vote-sleep");
        if (voteSleep.getBoolean("enabled")) {
        	doVoteSleep = true;
        	voteSleepPercent = voteSleep.getInt("percent-required", 50);
        	worlds = voteSleep.getStringList("worlds");
        	voteSleepActionBar = voteSleep.getBoolean("use-actionbar");
        }
        
        ConfigurationSection worldTweaks = misc.getConfigurationSection("world-tweaks");
        blockCoralFade = worldTweaks.getBoolean("disable-coral-fade", false);
        blockSpawnerChange = worldTweaks.getBoolean("disable-spawner-change", false);
        allowTntDamagesWaterlogged = worldTweaks.getBoolean("tnt-breaks-waterlogged", false);
        
        ConfigurationSection bossBar = misc.getConfigurationSection("boss-bar");
        if (bossBar.getBoolean("enabled")) {
        	parseBossBar(bossBar);
        }
        
        ConfigurationSection books = misc.getConfigurationSection("book-commands");
        if (books != null && books.getBoolean("enabled")) {
        	ConfigurationSection commandsSection = books.getConfigurationSection("books");
        	
        	final Material bookMat = Material.WRITTEN_BOOK;
        	
        	for (String command : commandsSection.getKeys(false)) {
        		ConfigurationSection commandSection = commandsSection.getConfigurationSection(command);
        		
        		ItemStack bookItem = new ItemStack(bookMat);
        		
        		BookMeta bookMeta = (BookMeta) Bukkit.getItemFactory().getItemMeta(bookMat);
        		
        		for (String pageNumString : commandSection.getKeys(false)) {
        			int page = -1;
        			try {
        				page = Integer.parseInt(pageNumString);
        			} catch (NumberFormatException e) {
        				continue;
        			}
        			
        			List<String> lines = commandSection.getStringList(pageNumString);
        			String pageString = String.join("\n&r", lines);
        			
        			pageString = ChatColor.translateAlternateColorCodes('&', pageString);
        			
        			bookMeta.setPage(page, pageString);
        		}
        		
        		bookItem.setItemMeta(bookMeta);
        		
        		bookCommands.put(command, bookItem);
        	}
        }
        
        ConfigurationSection cancelContainerSection = misc.getConfigurationSection("cancel-container-drops");
        if (cancelContainerSection != null) {
        	cancelContainerDrops = cancelContainerSection.getBoolean("enabled");
        }

        ConfigurationSection cancelPotionSection = misc.getConfigurationSection("cancel-custom-potions");
        if (cancelPotionSection != null) {
        	cancelCustomPotions = cancelPotionSection.getBoolean("enabled");
        }

        ConfigurationSection dispenserCooldownSection = misc.getConfigurationSection("dispenser-cooldown");
        if (dispenserCooldownSection != null) {
        	doDispenserCooldown = dispenserCooldownSection.getBoolean("enabled");
        	dispenserCooldown = dispenserCooldownSection.getDouble("cooldown") * 20;
        }
        
        ConfigurationSection preventOPFireworksSection = misc.getConfigurationSection("prevent-op-fireworks");
        if (preventOPFireworksSection != null) {
        	preventOPFireworks = preventOPFireworksSection.getBoolean("enabled");
        	effectsThreshold = preventOPFireworksSection.getDouble("effect-amount");
        }

        ConfigurationSection itemCooldownSection = misc.getConfigurationSection("item-cooldowns");
        if (itemCooldownSection != null) {
        	doItemCooldowns = itemCooldownSection.getBoolean("enabled");
        	ConfigurationSection cooldowns = itemCooldownSection.getConfigurationSection("cooldowns");
        	double cooldown = 0;
        	for (String materialString : cooldowns.getKeys(false)) {
        		Material mat = WbsEnums.materialFromString(materialString);
        		String directory = parentDirectory + "item-cooldowns/cooldowns/" + materialString;
        		
        		if (mat == null) {
        			logError("Invalid material: " + materialString, directory);
        		} else {
        			cooldown = cooldowns.getDouble(materialString);
        			if (cooldown <= 0) {
            			logError("Invalid cooldown length (use a positive number).", directory);
        			} else {
        				itemCooldowns.put(mat, cooldown * 20);
        			}
        		}
        	}
        }
        
        ConfigurationSection hideConsoleSpamSection = misc.getConfigurationSection("filter-console-messages");
		if (hideConsoleSpamSection != null) {
			if (hideConsoleSpamSection.getBoolean("enabled")) {
				if (filter != null) {
					filter.stop();
				}
				filter = new WbsFilter();

				for (String ignoreString : hideConsoleSpamSection.getStringList("ignore")) {
					filter.addIgnoreString(ignoreString);
				}
				
				Logger rootLogger = (Logger) LogManager.getRootLogger();
				rootLogger.addFilter(filter);
			}
		}

	}
	
	private WbsFilter filter = null;

	private boolean doItemCooldowns = false;
	public boolean doItemCooldowns() {
		return doItemCooldowns;
	}
	
	private Map<Material, Double> itemCooldowns = new HashMap<>();
	public Set<Material> getCooldownItems() {
		return itemCooldowns.keySet();
	}
	
	public Double getCooldownFor(Material type) {
		return itemCooldowns.get(type);
	}
	
	private double effectsThreshold = 5;
	public double getEffectsThreshold() {
		return effectsThreshold;
	}
	
	private boolean preventOPFireworks = false;
	public boolean preventOPFireworks() {
		return preventOPFireworks;
	}
	
	private boolean doDispenserCooldown = false;
	public boolean doDispenserCooldown() {
		return doDispenserCooldown;
	}
	private double dispenserCooldown = 40; // In ticks
	public double getDispenserCooldown() {
		return dispenserCooldown;
	}
	
	private boolean cancelCustomPotions = false;
	public boolean cancelCustomPotions() {
		return cancelCustomPotions;
	}
	
	// Cancel container drops
	private boolean cancelContainerDrops = false;
	public boolean cancelContainerDrops() {
		return cancelContainerDrops;
	}
	
	
	// Book commands
	private boolean doBookCommands = false;
	public boolean doBookCommands() {
		return doBookCommands;
	}
	
	private Map<String, ItemStack> bookCommands = new HashMap<>();
	public ItemStack getBookForCommand(String command) {
		return bookCommands.get(command);
	}
	
	
	// Boss bar
	private void parseBossBar(ConfigurationSection specs) {
		double intervalDouble = (specs.getDouble("interval") * 20);
		long interval = (long) intervalDouble;
		if (interval <= 0) {
			logError("Progress must be positive.", "misc.yml/boss-bar/interval");
			return;
		}
		
		ConfigurationSection messages = specs.getConfigurationSection("messages");
		
		String parent = "misc.yml/boss-bar/messages/";
		for (String messageName : messages.getKeys(false)) {
			String directory = parent + messageName;
			ConfigurationSection section = messages.getConfigurationSection(messageName);
			
			String message = section.getString("message");
			if (message == null) {
				logError("Message must not be null.", directory + "/message");
				continue;
			}
			
			BarColor colour;
			String colourString = section.getString("colour");
			try {
				colour = BarColor.valueOf(colourString.toUpperCase());
			} catch (IllegalArgumentException e) {
				String colourList = "";
				for (BarColor barColour : BarColor.values()) {
					colourList += ", " + barColour.name();
				}
				logError("Invalid colour; use one of the following:" + colourList, directory + "/colour");
				continue;
			}

			BarStyle style;
			String styleString = section.getString("style");
			try {
				style = BarStyle.valueOf(styleString.toUpperCase());
			} catch (IllegalArgumentException e) {
				String styleList = "";
				for (BarStyle barStyle : BarStyle.values()) {
					styleList += ", " + barStyle.name();
				}
				logError("Invalid style; use one of the following:" + styleList, directory + "/style");
				continue;
			}

			double progress = section.getDouble("progress");
			if (progress < 0) {
				logError("Progress must not be negative.", directory + "/progress");
				progress = 0;
			}
			
			BarAnnouncement announcement = new BarAnnouncement(message, colour, style, progress);
			BarAnnouncement.addBar(announcement);
		}
    	BarAnnouncement.start(interval);
	}

	// Vote sleep
	private boolean doVoteSleep = false;
	public boolean doVoteSleep() {
		return doVoteSleep;
	}
	
	private int voteSleepPercent = 50;
	public int getSleepPercent() {
		return voteSleepPercent;
	}
	
	private List<String> worlds = new LinkedList<>();
	public List<String> getVoteSleepWorlds() {
		return worlds;
	}
	
	private boolean voteSleepActionBar = false;
	public boolean voteSleepActionBar() {
		return voteSleepActionBar;
	}
	
	// Damage indicator
	private boolean doDamageIndicator = false;
	public boolean doDamageIndicator() {
		return doDamageIndicator;
	}
	
	private boolean showPlayerHealth = true;
	public boolean showPlayerHealth() {
		return showPlayerHealth;
	}
	
	// Block lava pvp
	private double lavaPlaceDistance = 0;
	public double lavaPlaceDistance() {
		return lavaPlaceDistance;
	}

	// Coral fade disabled
	public boolean blockCoralFade = false;
	public boolean blockCoralFade() {
		return blockCoralFade;
	}
	
	// Block spawners being changed with eggs
	private boolean blockSpawnerChange = false;
	public boolean blockSpawnerChange() {
		return blockSpawnerChange;
	}
	
	// Allow tnt to damage waterlogged blocks
	private boolean allowTntDamagesWaterlogged = false;
	public boolean allowTntDamagesWaterlogged() {
		return allowTntDamagesWaterlogged;
	}

	/********************/
	//		STAFF		//
	/********************/
	
	private void loadStaff() {
		FileConfiguration staff = configs.get("staff");

        ConfigurationSection lastCommand = staff.getConfigurationSection("last-command");
        if (lastCommand != null && lastCommand.getBoolean("enabled")) {
        	logLastCommand = true;
        	lastCommandBuffer = lastCommand.getInt("amount", lastCommandBuffer);
        	lastCommandBlacklist = lastCommand.getStringList("blacklist");
        }
        
        ConfigurationSection staffChat = staff.getConfigurationSection("staff-chat");
        if (staffChat != null && staffChat.getBoolean("enabled")) {
        	doStaffChat = true;
        	staffChatChar = staffChat.getString("character").charAt(0);
        	staffChatPrefix = staffChat.getString("prefix");
        	staffChatSuffix = staffChat.getString("suffix");
        }
        
        ConfigurationSection itemHistory = staff.getConfigurationSection("item-history");
        if (itemHistory != null && itemHistory.getBoolean("enabled")) {
        	doItemHistory = true;
        	itemHistoryBuffer = lastCommand.getInt("amount", lastCommandBuffer);
        }

        ConfigurationSection commandSpy = staff.getConfigurationSection("command-spy");
        if (commandSpy != null && commandSpy.getBoolean("enabled")) {
        	doCommandSpy = true;
        	commandSpyBlacklist = commandSpy.getStringList("blacklist");
        }
        
        ConfigurationSection essentialsLocalFilter = staff.getConfigurationSection("essentials-local-filter");
		if (essentialsLocalFilter != null) {
			doLocalSpyFilter = essentialsLocalFilter.getBoolean("enabled", false);
			List<String> characters = essentialsLocalFilter.getStringList("characters");
			
			if (characters != null) {
				for (String charString : characters) {
					localFilters.add(charString);
				}
			}
		}
		
        ConfigurationSection bypassIronDoorsSection = staff.getConfigurationSection("bypass-iron-doors");
        if (bypassIronDoorsSection != null) {
        	bypassIronDoors = bypassIronDoorsSection.getBoolean("enabled");
        }
	}

	private boolean bypassIronDoors = false;
	public boolean bypassIronDoors() {
		return bypassIronDoors;
	}
	
	private boolean doLocalSpyFilter = false;
	public boolean doLocalSpyFilter() {
		return doLocalSpyFilter;
	}
	
	private Set<String> localFilters = new HashSet<>();
	public Set<String> getLocalFilters() {
		return localFilters;
	}
	
	// Command spy
	private boolean doCommandSpy = false;
	public boolean doCommandSpy() {
		return doCommandSpy;
	}
	private List<String> commandSpyBlacklist = new LinkedList<>();
	public List<String> getCommandSpyBlacklist() {
		return commandSpyBlacklist;
	}
	
	// Item history
	private boolean doItemHistory = false;
	public boolean doItemHistory() {
		return doItemHistory;
	}
	
	private int itemHistoryBuffer = 50;
	public int getItemHistoryBufferSize() {
		return itemHistoryBuffer;
	}
	
	// Last command
	private boolean logLastCommand = false;
	public boolean logLastCommand() {
		return logLastCommand;
	}
	
	private int lastCommandBuffer = 15;
	public int getLastCommandBufferSize() {
		return lastCommandBuffer;
	}
	
	private List<String> lastCommandBlacklist = new LinkedList<>();
	public List<String> getLastCommandBlacklist() {
		return lastCommandBlacklist;
	}
	
	// Staff chat
	private boolean doStaffChat = false;
	public boolean doStaffChat() {
		return doStaffChat;
	}
	
	private char staffChatChar = '#';
	public char getStaffChatChar() {
		return staffChatChar;
	}
	
	private String staffChatPrefix = "&3[Staff] &7";
	public String getStaffChatPrefix() {
		return staffChatPrefix;
	}
	
	private String staffChatSuffix = "&7: &f";
	public String getStaffChatSuffix() {
		return staffChatSuffix;
	}
}











