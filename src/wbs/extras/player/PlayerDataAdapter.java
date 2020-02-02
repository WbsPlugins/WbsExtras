package wbs.extras.player;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

import wbs.extras.WbsExtras;

/**
 * A data adapter for the old config saving of
 * player data files. This should load the old 
 * player data into the new system to be serialized
 * and saved in the new way.
 * @author Weber588
 *
 */
public class PlayerDataAdapter {
	
	private static PlayerStore store = null;
	
	/**
	 * Load players from the old config system.
	 * @return The amount of legacy players loaded.
	 */
	public static int loadOldPlayers() {
		WbsExtras plugin = WbsExtras.getInstance();
		
		File playerDir = new File(plugin.getDataFolder() + File.separator + "player_data");
		if (!playerDir.exists()) {
			return 0;
		}
		
		int count = 0;
		for (File file : playerDir.listFiles()) {
			if (file.getName().endsWith(".yml")) {
				count++;
				loadPlayerFromFile(file);
			}
		}
		
		return count;
	}
	
	private static void loadPlayerFromFile(File dataFile) {
		if (store == null) {
			store = PlayerStore.getInstance();
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

		PlayerData data = new PlayerData(config.getString("username"));
		
		data.lastCommands = config.getStringList("lastCommands");
		data.spyCommands = config.getStringList("spyCommands");
		data.spyUsernames = config.getStringList("spyUsernames");
		
		for (String watchedCommand : data.spyCommands) {
			store.watchCommand(watchedCommand, data);
		}
		
		for (String watchedPlayer : data.spyUsernames) {
			store.watchCommand(watchedPlayer, data);
		}
		
		data.triggers = config.getStringList("triggers");
		data.doChatNotifications = config.getBoolean("doChatNotifications");
		data.needsTag = config.getBoolean("needsTag");
	}

	public static int removeLegacyFolder() {
		WbsExtras plugin = WbsExtras.getInstance();
		File playerDir = new File(plugin.getDataFolder() + File.separator + "player_data");
		int count = 0;
		if (playerDir.exists()) {
			for (File file : playerDir.listFiles()) {
				if (file.getName().endsWith(".yml")) {
					if (file.delete()) {
						count++;
					} else {
						plugin.logger.info("A legacy player file failed to remove correctly.");
					}
				}
			}
			
			playerDir.delete();
		} else {
			return 0;
		}
		
		return count;
	}
}
