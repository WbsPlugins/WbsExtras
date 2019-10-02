package wbs.extras;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import wbs.extras.configurations.BarAnnouncement;
import wbs.extras.configurations.Replacement;
import wbs.extras.util.WbsPlugin;
import wbs.extras.util.WbsSettings;

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
			Replacement replacement = new Replacement(replace);
			
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
        }
        
        ConfigurationSection worldTweaks = misc.getConfigurationSection("world-tweaks");
        blockCoralFade = worldTweaks.getBoolean("disable-coral-fade", false);
        blockSpawnerChange = worldTweaks.getBoolean("disable-spawner-change", false);
        allowTntDamagesWaterlogged = worldTweaks.getBoolean("tnt-breaks-waterlogged", false);
        
        ConfigurationSection bossBar = misc.getConfigurationSection("boss-bar");
        if (bossBar.getBoolean("enabled")) {
        	parseBossBar(bossBar);
        }
        
	}
	
	// Boss bar
	private void parseBossBar(ConfigurationSection specs) {
		double intervalDouble = (specs.getDouble("interval") * 20);
		System.out.println(intervalDouble);
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











