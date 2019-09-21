package wbs.extras;

import java.time.LocalDate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import net.milkbowl.vault.economy.Economy;
import wbs.extras.commands.ItemHistoryCommand;
import wbs.extras.commands.LastCommandCommand;
import wbs.extras.configurations.BarAnnouncement;
import wbs.extras.listeners.ChatListener;
import wbs.extras.listeners.MiscListener;
import wbs.extras.listeners.StaffListener;
import wbs.extras.player.PlayerData;
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

	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/
	
	public ExtrasSettings settings;
	
	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		settings = new ExtrasSettings(this);

		settings.reload();

		PlayerData.setPlugin(this);
		PlayerData.loadAll();
		
		startBackupTimers();
		
		if (!setupEconomy()) {
			logger.severe("No Vault dependency found! Monetary commands disabled.");
		}
		if (!setupEssentials()) {
			logger.info("Essentials not found.");
		}
		
		getCommand("lastcommand").setExecutor(new LastCommandCommand(this));
		getCommand("lastcommand").setTabCompleter(new LastCommandCommand(this));

		getCommand("itemhistory").setExecutor(new ItemHistoryCommand(this));
		getCommand("itemhistory").setTabCompleter(new ItemHistoryCommand(this));
		
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
					PlayerData.saveAll();
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
		PlayerData.saveAll();
		BarAnnouncement.stop();
	}
}
