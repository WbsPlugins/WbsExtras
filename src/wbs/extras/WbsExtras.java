package wbs.extras;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import net.milkbowl.vault.economy.Economy;
import wbs.extras.commands.CommandSpyCommand;
import wbs.extras.commands.ItemHistoryCommand;
import wbs.extras.commands.LastCommandCommand;
import wbs.extras.commands.NotificationsCommand;
import wbs.extras.configurations.BarAnnouncement;
import wbs.extras.listeners.ChatListener;
import wbs.extras.listeners.MiscListener;
import wbs.extras.listeners.StaffListener;
import wbs.extras.player.PlayerStore;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerDataAdapter;
import wbs.extras.util.WbsPlugin;

public class WbsExtras extends WbsPlugin {
	
	private static boolean essentialsInstalled = false;
	public static boolean isEssentialsInstalled() {
		return essentialsInstalled;
	}
	
	private static Economy econ = null;
	public static Economy getEconomy() {
		return econ;
	}
	
	private static WbsExtras instance = null;
	public static WbsExtras getInstance() {
		return instance;
	}

	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/
	
	public ExtrasSettings settings;
	
	@Override
	public void onEnable() {
		instance = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		settings = new ExtrasSettings(this);

		PlayerStore.setPlugin(this);
		PlayerData.setPlugin(this);
		
		PlayerStore.getInstance().loadAll();

		final File data = new File(getDataFolder() + File.separator + "player.data");
		
		if (!data.exists()) {
			try {
				data.createNewFile();
			} catch (IOException e) {
				logger.severe("An unknown error occured when creating the player data file.");
				Bukkit.getPluginManager().disablePlugin(this);
			}
		}
		
		int legacyLoaded = PlayerDataAdapter.loadOldPlayers();
		
		logger.info("Loaded " + legacyLoaded + " legacy players from configs.");
		
		if (legacyLoaded > 0) {
			PlayerStore.getInstance().saveAll();

			int legacyDeleted = PlayerDataAdapter.removeLegacyFolder();
			logger.info("Successfully deleted " + legacyDeleted + " legacy player files.");
		}
		
		
	//	startBackupTimers();
		
		if (!setupEconomy()) {
			logger.warning("No Vault dependency found! Monetary commands disabled.");
		}
		if (!setupEssentials()) {
			logger.warning("Essentials not found.");
		}
		
		getCommand("lastcommand").setExecutor(new LastCommandCommand(this));
		getCommand("lastcommand").setTabCompleter(new LastCommandCommand(this));

		getCommand("itemhistory").setExecutor(new ItemHistoryCommand(this));
		getCommand("itemhistory").setTabCompleter(new ItemHistoryCommand(this));

		getCommand("commandspy").setExecutor(new CommandSpyCommand(this));
		getCommand("commandspy").setTabCompleter(new CommandSpyCommand(this));
		
		getCommand("notifications").setExecutor(new NotificationsCommand(this));
		getCommand("notifications").setTabCompleter(new NotificationsCommand(this));
		
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new MiscListener(this), this);
		pm.registerEvents(new StaffListener(this), this);
	}
	
	private void startBackupTimers() {
		new BukkitRunnable() {
			LocalDate lastLoop = LocalDate.now();
			@Override
			public void run() {
				if (!lastLoop.equals(LocalDate.now())) { // Don't repeat same day
					PlayerStore.getInstance().saveAll();
				}
				lastLoop = LocalDate.now();
			}
		}.runTaskTimerAsynchronously(this, 0, 864000L); // Every 12 hours
	}

	private boolean setupEssentials() {
		if (getServer().getPluginManager().getPlugin("Essentials") == null) {
			return false;
		}
		essentialsInstalled = true;
		return true;
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	@Override
	public void onDisable() {
		PlayerStore.getInstance().saveAll();
		BarAnnouncement.stop();
	}
}
