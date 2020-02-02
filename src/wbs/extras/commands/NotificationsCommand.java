package wbs.extras.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import wbs.extras.ExtrasSettings;
import wbs.extras.util.WbsMessenger;
import wbs.extras.WbsExtras;
import wbs.extras.player.PlayerData;
import wbs.extras.player.PlayerStore;

public class NotificationsCommand extends WbsMessenger implements CommandExecutor, TabCompleter {

	private ExtrasSettings settings;
	public NotificationsCommand(WbsExtras plugin) {
		super(plugin);
		settings = plugin.settings;
	}

	private final String permission = "wbsextras.notif";
	private final String usage = "Usage: &h/notifications <add|list|settings>";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!checkPermission(sender, permission)) {
			return true;
		}

		if (!settings.doChatNotifiers()) {
			sendMessage("&wThis command is disabled in the config.", sender);
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sendMessage("This command is only usable by players.", sender);
			return true;
		}
		
		int length = args.length;
		if (length == 0) {
			sendMessage(usage, sender);
			return true;
		}
		
		Player player = (Player) sender;

		PlayerData data = PlayerStore.getInstance().getPlayerData(player);
		
		int index = 0;
		List<String> notificationWords;
		
		switch (args[0].toUpperCase()) {
		case "TOGGLE":
			data.doChatNotifications = !data.doChatNotifications;
			if (data.doChatNotifications) {
				sendMessage("You will now receive notifications for your name, and words in your &h/notif list&r.", player);
			} else {
				sendMessage("You will no longer receive notifications.", player);
			}
			break;
		case "TAG": 
			data.needsTag = !data.needsTag;
			if (data.needsTag) {
				sendMessage("You will now only get notified when someone puts \"@\" before your name.", player);
			} else {
				sendMessage("You will now get notified whenever your name is mentioned, whether or not they use \"@\".", player);
			}
			break;
		case "LIST":
			notificationWords = data.getTriggers();
			sendMessage("You will be notified of the following:", player);
			index = 0;
			for (String phrase : notificationWords) {
				index++;
				sendMessage("&r" + index + ") \"&h" + phrase + "&r\"", player);
			}
			break;
		case "ADD":
			if (args.length >= 2) {
    			notificationWords = data.getTriggers();
				if (notificationWords.size() >= 50) {
    				sendMessage("You have reached the maximum amount of notification phrases (50). Remove one to add another with &h/notif remove", player);
    				return true;
    			}
				
				String[] newStringList = new String[args.length-1];
				for (int i = 1; i < args.length; i++) {
					newStringList[i-1] = args[i];
				}
				String newString = String.join(" ", newStringList); // Combining last args to form a phrase
				
				for (String phrase : notificationWords) {
					if (phrase.equals(newString)) {
	    				sendMessage("That word is already in your notification list!", player);
	    				return true;
					}
				}
				
    			data.addTrigger(newString);
				sendMessage("You will now be notified when someone says \"&h" + newString + "&r\".", player);
				
			} else {
				sendMessage("Usage: &h/notif add <String>", player);
			}
			break;
		case "REMOVE":
		case "DELETE":
		case "REM":
		case "DEL":
			if (args.length >= 2) {
    			notificationWords = data.getTriggers();

         		String newString = null;
         		try {
	         		index = Integer.parseInt(args[1]) - 1;
	         		if (notificationWords.size() > index) {
	         			newString = notificationWords.get(index);
	         		}
				} catch (NumberFormatException e) {

    				String[] newStringList = new String[args.length-1];
    				for (int i = 1; i < args.length; i++) {
    					newStringList[i-1] = args[i];
    				}
    				newString = String.join(" ", newStringList);
	         	}

         		if (newString != null) {
         		
             		if (data.hasTrigger(newString)) {
	    				sendMessage("You will no longer be notified of the phrase \"&h" + newString + "&r\".", player);
	    				data.removeTrigger(newString);
    				} else {
	    				sendMessage("That phrase was not recognised. Use &h/notif list&r to see your notification list.", player);
	    			}
         		} else {
         			sendMessage("Invalid index. Use &h/notif list&r to see your notification list.", player);
	    		}
         		
			} else {
				sendMessage("Usage: &h/notif remove <String>", player);
				sendMessage("Or: &h/notif remove <index>", player);
				sendMessage("You can view your list of words with &h/notif list&r.", player);
			}
			
			break;
		case "SETTINGS":
		case "SETTING":
		case "OPTION":
		case "OPTIONS":
			sendMessage("&r=== &hSettings &r===", player);
			sendMessage("&h/notif toggle:", player);
			sendMessage("When enabled, you will receive notifications. When disabled, you will never be notified, even for your name.", player);
			sendMessage("This feature is currently &h" + boolOnOff(data.doChatNotifications) + "&r.", player);
			
			sendMessage("&h/notif tag:", player);
			sendMessage("When enabled, players must use &h@" + player.getName() + "&r to send you a notification. When disabled, any mention of your name will send a notification.", player);
			sendMessage("This feature is currently &h" + boolOnOff(data.needsTag) + "&r.", player);
			
			sendMessage("&h/notif list:", player);
			sendMessage("This command will display a list of all words that will notify you when another player puts them in chat.", player);

			sendMessage("&h/notif add <String>:", player);
			sendMessage("Use this to add a word or phrase to your notification list.", player);

			sendMessage("&h/notif remove <String>:", player);
			sendMessage("Use this to remove words currently on to your notification list.", player);
		}
		
		return true;
	}
	
	/**
	 * Helper method to parse a boolean into appropriate string
	 * @param bool The boolean to parse
	 * @return A custom string version of the boolean
	 */
	private String boolOnOff(boolean bool) {
		if (bool) {
			return "enabled";
		}
		return "disabled";
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> choices = new LinkedList<>();

		if (sender.hasPermission(permission)) {
			int length = args.length;
			if (length == 1) {
				choices.add("toggle");
				choices.add("tag");
				choices.add("list");
				choices.add("add");
				choices.add("remove");
				choices.add("settings");
			}
		}
		
    	List<String> result = new ArrayList<String>();
		for (String add : choices) {
    		if (add.toLowerCase().startsWith(args[args.length-1].toLowerCase())) {
    			result.add(add);
    		}
		}
		
		return result;
	}


}
