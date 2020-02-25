package wbs.extras.player;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;

public class PlayerStore {
	
	private static PlayerStore instance = null;
	public static PlayerStore getInstance() {
		if (instance == null) {
			instance = new PlayerStore();
		}
		return instance;
	}
	
	private static WbsExtras plugin;
	private static Logger logger;
	private static ExtrasSettings settings;
	public static void setPlugin(WbsExtras plugin) {
		PlayerStore.plugin = plugin;
		logger = plugin.getLogger();
		settings = plugin.settings;
	}
	
	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/
	
	private PlayerStore() {
		
	}

	private Map<String, PlayerData> allData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	public Map<String, PlayerData> allData() {
		return allData;
	}
	
	/**
	 * Add a SerializablePlayer to the data set.
	 * @param data The data to add
	 * @return True if the data was added. If the player already
	 * exists, returns false.
	 */
	boolean addPlayer(PlayerData data) {
		if (allData.containsKey(data.getName())) {
			return false;
		}
		
		allData.put(data.getName(), data);
		return true;
	}

	public boolean exists(Player player) {
		return exists(player.getName());
	}
	public boolean exists(String username) {
		return allData.containsKey(username);
	}

	public PlayerData getPlayerData(Player player) {
		String username = player.getName();
		return getPlayerData(username);
	}
	
	public PlayerData getPlayerData(String username) {
		if (exists(username)) {
			return allData.get(username);
		}
		
		return new PlayerData(username);
	}
	
	public void saveAll() {
		if (allData.isEmpty())  {
			logger.info("There was no player data loaded when attempting to save.");
			return;
		}
		
		final String path = plugin.getDataFolder() + File.separator + "player.data";

		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(path))) {
			output.writeObject(allData);
		} catch (FileNotFoundException e) {
			logger.warning("The player data file was missing!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.warning("An unknown error occured while writing to the player data file.");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadAll() {
		final String path = plugin.getDataFolder() + File.separator + "player.data";
		logger = plugin.getLogger();

		logger.info("Attempting to load Player data... ");
		
		File dataFile = new File(path);
		
		if (!dataFile.exists()) {
			logger.info("No existing data found; one will be created.");
			saveAll();
			return;
		}
		
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(path))) {
			try {
				allData = (TreeMap<String, PlayerData>) input.readObject();
			} catch (ClassNotFoundException e) {
				logger.severe("A class definition was missing when reading data!");
				e.printStackTrace();
			} catch (ClassCastException e) {
				logger.severe("The player data file contained corrupt data!");
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			logger.info("ERROR: The player data file was missing!");
			e.printStackTrace();
		} catch (EOFException e) {
			logger.info("ERROR: The player data file was empty.");
		} catch (IOException e) {
			logger.info("ERROR: An unknown error occured while attempting to read player data.");
			e.printStackTrace();
		}
		
		logger.info("Loaded " + allData.size() + " players from memory.");
	}
	

	/*
	 *  Map of watched commands to the players that watch them. This speeds up checking 
	 *  if any players watch a command that was run; instead of iterating over all
	 *  players, we iterate over watched commands and get players accordingly.
	 */
	private Multimap<String, PlayerData> spiedCommands = HashMultimap.create();
	public void watchCommand(String command, PlayerData player) {
		spiedCommands.put(command, player);
	}
	
	public boolean unwatchCommand(String command, PlayerData player) {
		return spiedCommands.remove(command, player);
	}
	
	public boolean isWatched(String command) {
		return spiedCommands.containsKey(command);
	}
	
	public Collection<PlayerData> getWatchingPlayers(String command) {
		return spiedCommands.get(command);
	}
	
	
	private Multimap<String, PlayerData> spiedPlayers = HashMultimap.create();
	public void watchPlayer(String username, PlayerData player) {
		spiedPlayers.put(username, player);
	}
	
	public boolean unwatchPlayer(String username, PlayerData player) {
		return spiedPlayers.remove(username, player);
	}
	
	public boolean isWatchedPlayer(String username) {
		return spiedPlayers.containsKey(username);
	}
	
	public Collection<PlayerData> getPlayersWatchingPlayer(String username) {
		return spiedPlayers.get(username);
	}
}
