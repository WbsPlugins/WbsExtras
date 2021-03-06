package wbs.extras.configurations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Replacement {
	
	private static Map<String, Replacement> replacements = new HashMap<>();
	
	public static void addReplacement(String name, Replacement replacement) {
		replacements.put(name, replacement);
	}
	
	public static Map<String, Replacement> getAll() {
		return replacements;
	}

	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/
	
	private String permission = null;
	private List<String> match = new ArrayList<>();
	private String replace;
	private int maxPerMessage = 0;
	private boolean enabled = true;
	private boolean ignoreCase = true;
	
	public Replacement(String replace) {
		this.replace = parseColours(replace);
	}

	private String parseColours(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	public void disable() { 
		enabled = false;
	}
	public void enable() { 
		enabled = true;
	}
	
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public void setMaxPerMessage(int maxPerMessage) {
		this.maxPerMessage = maxPerMessage;
	}
	public void addMatch(String match) {
		this.match.add(parseColours(match));
	}
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	public String getPermission() {
		return permission;
	}
	public List<String> getMatches() {
		return match;
	}
	public String getReplace() {
		return replace;
	}
	public int getMaxPerMessage() {
		return maxPerMessage;
	}
	
	/*****************************************/
	
	public String replaceIn(String message, String formatted) {
		
		String preMessage = formatted.substring(0, formatted.length() - message.length());
		
		for (String caught : match) {
			int firstIndex = -1;
			if (ignoreCase) {
				firstIndex = message.toLowerCase().indexOf(caught.toLowerCase());
			} else {
				firstIndex = message.indexOf(caught);
			}
			while (firstIndex != -1) {
				String returnColours = ChatColor.getLastColors(formatted.substring(0, firstIndex-1 + (preMessage.length())));

				String beforeCatch = message.substring(0, firstIndex);
				int afterCatchIndexStart = firstIndex + caught.length();
				int afterCatchIndexFinish = message.length();
				String afterCatch = message.substring(afterCatchIndexStart, afterCatchIndexFinish);
				
				message = beforeCatch + replace + returnColours + afterCatch;

				formatted = preMessage + message;
				
				if (ignoreCase) {
					firstIndex = message.toLowerCase().indexOf(caught.toLowerCase());
				} else {
					firstIndex = message.indexOf(caught);
				}
			}
		}
		return message;
	}
	
	public boolean canUse(Player p) {
		if (permission == null) {
			return true;
		}
		return (p.hasPermission(permission));
	}
	
	public String run(String message, String formatted, Player p) {
		if (canUse(p)) {
			return replaceIn(message, formatted);
		}
		return message;
	}
}
