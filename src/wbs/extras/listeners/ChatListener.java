package wbs.extras.listeners;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.TabCompleteEvent;

import wbs.extras.ExtrasSettings;
import wbs.extras.WbsExtras;
import wbs.extras.configurations.Replacement;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerStore;
import wbs.extras.util.WbsMessenger;
import wbs.extras.util.WbsPlugin;

public class ChatListener extends WbsMessenger implements Listener {

	private ExtrasSettings settings;
	public ChatListener(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}
	
	@EventHandler
	public void onTabComplete(TabCompleteEvent event) {
		String buffer = event.getBuffer();
		
		String[] args = buffer.split(" ", -1);
		
		String command = args[0];
		int argNumber = args.length - 1;
		
		Collection<String> additions = settings.getExtraTabsFor(command, argNumber);
		if (additions != null) {
			List<String> completions = null;
			if (settings.overrideTabs(command)) {
				completions = new LinkedList<>();
			} else {
				completions = event.getCompletions();
			}
			
			completions.addAll(additions);
			
	    	List<String> result = new ArrayList<String>();
			for (String add : completions) {
	    		if (add.toLowerCase().startsWith(args[args.length-1].toLowerCase())) {
	    			result.add(add);
	    		}
			}
			
			event.setCompletions(result);
		}
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onCoordsMention(AsyncPlayerChatEvent event) {
		if (!settings.doChatCoords()) return;
		
		Player player = event.getPlayer();
		String permission = settings.getChatCoordsPermission();
		
		if (permission != "") {
			if (!player.hasPermission(permission)) return;
		}
		
			
		String message = event.getMessage();
		
		if (message.contains("[c]") || message.contains("[coords]")) {
			// Auto rounds
			Location loc = player.getLocation().getBlock().getLocation();
			
			String withFormat = event.getFormat();
			Formatter formatter = new Formatter();
			formatter.format(withFormat, player.getDisplayName(), message);
			
			String formatted = formatter.toString();
			
			formatter.close();
			
			String coordsString = "&b" + ((int) loc.getX()) + ", " + ((int) loc.getY()) + ", " + ((int) loc.getZ()) + "&r";
			
			Replacement replacement = new Replacement(coordsString);
			replacement.addMatch("[c]");
			replacement.addMatch("[coords]");
			
			if (replacement.isEnabled()) {
				message = replacement.run(message, formatted, player);
			}
			
			event.setMessage(message);
		}
	}
	
	/************************************************/
	/*					NAME NOTIFIER				*/
	/************************************************/
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onNameMention(AsyncPlayerChatEvent event) {
		if (!settings.doChatNotifiers()) {
			return;
		}
		
		String message = event.getMessage();
		Set<Player> recipients = event.getRecipients();
		Set<Player> removeRecipients = new HashSet<>();
		
		Player player = event.getPlayer();

		String format = event.getFormat();
		PlayerStore store = PlayerStore.getInstance();
		for (Player receiver : event.getRecipients()) {
			if (receiver.equals(player)) {
				continue;
			}
			
			PlayerData data = null;
			if (store.exists(receiver)) {
				data = store.getPlayerData(receiver);
			}
		
			if (data != null && !data.doChatNotifications) {
				continue;
			}
			
			String formatted = String.format(format, player.getDisplayName(), message);
			String finalMessage = formatted;
			boolean changed = false;
			String check = receiver.getName();
			if (data != null && data.needsTag) {
				check = "@" + check;
			}
			String oldMessage = finalMessage;
			if (message.toUpperCase().contains(check.toUpperCase())) {
				if ((data == null || !data.needsTag) || message.indexOf("@") != 0) {
					if (message.toUpperCase().contains(check.toUpperCase())) {
						finalMessage = highlightAll(check, finalMessage, 'b');
						changed = (changed || !finalMessage.equals(oldMessage));
						oldMessage = finalMessage;
					}
				}
			}

			if (data != null) {
				List<String> triggers = data.getTriggers();
				for (String trigger : triggers) {
					if (message.toUpperCase().contains(trigger.toUpperCase())) {
						finalMessage = highlightAll(trigger, finalMessage, 'e');
						changed = (changed || !finalMessage.equals(oldMessage));
						oldMessage = finalMessage;
					}
				}
			}
			
			if (changed) {
				removeRecipients.add(receiver);
				receiver.sendMessage(finalMessage);
				receiver.playSound(receiver.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
			}
		
		}
		recipients.removeAll(removeRecipients);
	}
	

	private String highlightAll(String trigger, String message, char colour) {
		String finalMessage = message;

		if (message.toUpperCase().equalsIgnoreCase(trigger)) {
			finalMessage = highlightString(trigger, finalMessage, colour);
		} else {
			
			if (message.toUpperCase().startsWith(trigger.toUpperCase() + " ")) {
				finalMessage = highlightFirst(trigger, finalMessage, colour);
			}
			
			if (message.toUpperCase().endsWith(" " + trigger.toUpperCase())) {
				finalMessage = highlightLast(trigger, finalMessage, colour);
			}
			
			if (message.toUpperCase().contains(" " + trigger.toUpperCase() + " ")) {
				finalMessage = highlightString(trigger, finalMessage, colour);
			}
			
		}
		
		return finalMessage;
	}

	private String highlightLast(String trigger, String message, char colour) {
		if (message.toUpperCase().contains(trigger.toUpperCase())) {

			int triggerIndex = message.length() - trigger.length();
			String name = message.substring(triggerIndex, triggerIndex + trigger.length());
			String start = message.substring(0, triggerIndex);
			
			message = start + "§" + colour + name;
		}
		return message;
	}
	
	private String highlightFirst(String trigger, String message, char colour) {
		if (message.toUpperCase().contains(trigger.toUpperCase())) {

			int triggerIndex = message.toUpperCase().indexOf(trigger.toUpperCase());
			String name = message.substring(triggerIndex, triggerIndex + trigger.length());
			
			String firstHalf = message.substring(0, triggerIndex);
			String secondHalf = message.substring(triggerIndex+(name.length()));
			
			int indexOfColour = firstHalf.lastIndexOf("§") + 1;
			char oldColour = message.charAt(indexOfColour);
			
			message = firstHalf + "§" + colour + name + "§" + oldColour + secondHalf;
		}
		return message;
	}
	
	private String highlightString(String trigger, String message, char colour) {
		if (message.toUpperCase().contains(" " + trigger.toUpperCase() + " ")) {

			int triggerIndex = message.toUpperCase().indexOf(" " + trigger.toUpperCase() + " ")+1;
			String name = message.substring(triggerIndex, triggerIndex + trigger.length());
			
			String firstHalf = message.substring(0, triggerIndex);
				
			int indexOfColour = firstHalf.lastIndexOf("§") + 1;
			char oldColour = message.charAt(indexOfColour);
			
			String secondHalf = highlightAll(trigger, "§" + oldColour + message.substring(triggerIndex+name.length()), colour);
			
			message = firstHalf + "§" + colour + name + secondHalf;
		}
		return message;
	}
	
	/************************************************/
	/*					CHAT REPLACERS				*/
	/************************************************/
	
	private final String grave = "`";
	
	@EventHandler(priority=EventPriority.HIGH)
	public void graveHighlighting(AsyncPlayerChatEvent event) {
		if (!settings.doGraveHighlighting()) {
			return;
		}

		String message = event.getMessage();
		if (!message.contains(grave)) {
			return;
		}
		
		Player player = event.getPlayer();
		if (!player.hasPermission("wbsextras.chat.grave")) {
			return;
		}
		
		message += " "; // To make ending with ` and having something after it the same
		
		String[] components = message.split(grave);
		ChatColor colour = settings.getHighlightColour();
		
		String withFormat = event.getFormat();
		Formatter formatter = new Formatter();
		formatter.format(withFormat, player.getDisplayName(), message);
		String formatted = formatter.toString();
		formatter.close();
		
		for (int i = 0; i < components.length; i++) {
			if (i % 2 == 1 && i != components.length - 1) {
				Replacement replacement = new Replacement(colour + components[i]);
				replacement.addMatch(grave + components[i] + grave);
				message = replacement.run(message, formatted, player);
			}
		}

		event.setMessage(message);
		
	}

	/************************************************/
	/*					CHAT REPLACERS				*/
	/************************************************/
	
	@EventHandler(priority=EventPriority.LOW)
	public void replaceConfiguredOptions(AsyncPlayerChatEvent event) {
		String message = event.getMessage();
		Player player = event.getPlayer();
		
		String withFormat = event.getFormat();
		Formatter formatter = new Formatter();
		formatter.format(withFormat, player.getDisplayName(), message);
		
		String formatted = formatter.toString();
		
		formatter.close();
		
		for (Replacement replacement : Replacement.getAll().values()) {
			if (replacement.isEnabled()) {
				message = replacement.run(message, formatted, player);
			}
		}
		
		event.setMessage(message);
	}
	
}
