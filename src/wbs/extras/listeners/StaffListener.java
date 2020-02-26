package wbs.extras.listeners;

import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerStore;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.WbsTime;

public class StaffListener extends WbsMessenger implements Listener {

	private WbsExtras plugin;
	private ExtrasSettings settings;
	public StaffListener(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}
	
	/************************************************/
	
	@EventHandler(ignoreCancelled=true)
	public void onIronDoorClick(PlayerInteractEvent event) {
		if (!settings.bypassIronDoors()) {
			return;
		}
		
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		ItemStack item = event.getItem();
		if (item != null) {
			return;
		}
		
		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		if (block.getType() != Material.IRON_DOOR && block.getType() != Material.IRON_TRAPDOOR) {
			return;
		}
		
		Player player = event.getPlayer();
		if (player.hasPermission("wbsextras.staff.irondoorbypass")) {
			if (block.getBlockData() instanceof Openable) {
				Openable door = (Openable) block.getBlockData();

				door.setOpen(!door.isOpen());
				
				block.setBlockData(door);
			}
		}
	}

	/************************************************/
	/*                  COMMAND SPY                 */
	/************************************************/
	
	private final String commandSpyPrefix = "&f[&6CS&f]";
	
	@EventHandler
	public void commandSpy(PlayerCommandPreprocessEvent event) {
		if (!settings.doCommandSpy()) {
			return;
		}
		
		Player player = event.getPlayer();
		String username = player.getName();
		if (player.hasPermission("wbsextras.staff.commandspy.exempt")) {
			return;
		}

		String fullCommand = event.getMessage();
		String command = fullCommand.split(" ")[0].substring(1);
		
		for (String blacklisted : settings.getCommandSpyBlacklist()) {
			if (command.equalsIgnoreCase(blacklisted)) {
				return;
			}
		}
		
		PlayerStore store = PlayerStore.getInstance();
		if (store.isWatched(command)) {
			for (PlayerData data : store.getWatchingPlayers(command)) {
				String watcherUsername = data.getName();
				if (watcherUsername.equals(username)) {
					continue;
				}
				Player watcher = Bukkit.getPlayer(watcherUsername);
				if (watcher != null) { // If watcher is online
					sendMessageNoPrefix(commandSpyPrefix + " " + player.getDisplayName() + "&f: &b" + fullCommand, watcher);
				}
			}
		} else if (store.isWatchedPlayer(player.getName())) {
			for (PlayerData data : store.getPlayersWatchingPlayer(username)) {
				String watcherUsername = data.getName();
				if (watcherUsername.equalsIgnoreCase(username)) {
					continue;
				}
				Player watcher = Bukkit.getPlayer(watcherUsername);
				if (watcher != null) { // If watcher is online
					sendMessageNoPrefix(commandSpyPrefix + " " + player.getDisplayName() + "&f: &b" + fullCommand, watcher);
				}
			}
		}
	}

	/************************************************/
	/*                  ITEM HISTORY                */
	/************************************************/
	
	private final String PICKUP_STRING = "Picked up";
	@EventHandler
	public void onPickup(EntityPickupItemEvent event) {
		if (!settings.doItemHistory()) {
			return;
		}
		LivingEntity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			ItemStack item = event.getItem().getItemStack();
			
			PlayerData data = PlayerStore.getInstance().getPlayerData(player);
			
			data.addItemInteraction(item, PICKUP_STRING);
		}
	}

	private final String DROP_STRING = "Dropped";
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (!settings.doItemHistory()) {
			return;
		}
		Player player = event.getPlayer();
		ItemStack item = event.getItemDrop().getItemStack();
		
		PlayerData data = PlayerStore.getInstance().getPlayerData(player);
		
		data.addItemInteraction(item, DROP_STRING);
	}
	
	/************************************************/
	/*                  STAFF CHAT                  */
	/************************************************/
	
	@EventHandler(priority=EventPriority.LOW)
	public void staffChat(AsyncPlayerChatEvent event) {
		if (!settings.doStaffChat()) {
			return;
		}
		
		char chatChar = settings.getStaffChatChar();
		String message = event.getMessage();
		
		if (message.startsWith(chatChar + "")) {
			Player player = event.getPlayer();
			
			String permission = "wbsextras.staff.chat";
			
			if (player.hasPermission(permission)) {
				event.setCancelled(true);
				
				String fullMessage = getStaffChatFormat(player) + message.substring(1);
				
				for (Player staff : Bukkit.getOnlinePlayers()) {
					if (staff.hasPermission(permission)) {
						sendMessageNoPrefix(fullMessage, staff);
					}
				}
			}
		}
	}
	
	private String getStaffChatFormat(Player player) {
		return settings.getStaffChatPrefix() + player.getDisplayName() + settings.getStaffChatSuffix();
	}
	
	/************************************************/
	/*                  LASTCOMMAND                 */
	/************************************************/
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (!settings.logLastCommand()) {
			return;
		}
		
		Player player = event.getPlayer();
		
		if (player.hasPermission("wbsextras.staff.lastcommand.exempt")) {
			return;
		}
		
		String command = event.getMessage();
		
		if (command.startsWith("/plugman")) { // Probably a better way to do this, but atm unloading causes issues
			return;
		}
		
		for (String ignore : settings.getLastCommandBlacklist()) {
			if (command.startsWith("/" + ignore)) {
				return;
			}
		}
		
		// Don't check, always needed
		PlayerData data = PlayerStore.getInstance().getPlayerData(player);
		
		LocalDateTime timeStamp = LocalDateTime.now();
		
		data.addCommand(command + " &7(" + WbsStringify.toString(timeStamp) + ")");
	}
}
