package wbs.extras.listeners;

import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsTime;

public class StaffListener extends WbsMessenger implements Listener {

	private WbsExtras plugin;
	private ExtrasSettings settings;
	public StaffListener(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}
	
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
			
			PlayerData data = PlayerData.getPlayerData(player);
			
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
		
		PlayerData data = PlayerData.getPlayerData(player);
		
		data.addItemInteraction(item, DROP_STRING);
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
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
		for (String ignore : settings.getLastCommandBlacklist()) {
			if (command.startsWith("/" + ignore)) {
				return;
			}
		}
		
		// Don't check, always needed
		PlayerData data = PlayerData.getPlayerData(player);
		
		LocalDateTime timeStamp = LocalDateTime.now();
		
		data.addCommand(command + " &7(" + WbsTime.prettyTime(timeStamp) + ")");
	}
}
