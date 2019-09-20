package wbs.extras.util;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class WbsPlugin extends JavaPlugin {

	public Logger logger = getLogger();

	public void sendMessage(String message, CommandSender sender) {
		message = message.replaceAll("&r", "" + colour); // Replace default with the main colour
		message = message.replaceAll("&h", "" + highlight); // Replace &h with the highlight colour
		message = message.replaceAll("&w", "" + errorColour); // Replace &w with the error colour
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(prefix + ' ' +  colour + message);
	}
	
	public void sendMessageNoPrefix(String message, CommandSender sender) {
		// Don't do reset since default colour is white
		message = message.replaceAll("&h", "" + highlight); // Replace &h with the highlight colour
		message = message.replaceAll("&w", "" + errorColour); // Replace &w with the error colour
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(message);
	}
	
	public void broadcast(String message) {
		message = message.replaceAll("&r", "" + colour); // Replace default with the main colour
		message = message.replaceAll("&h", "" + highlight); // Replace &h with the highlight colour
		message = message.replaceAll("&w", "" + errorColour); // Replace &h with the highlight colour
		message = ChatColor.translateAlternateColorCodes('&', message);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(prefix + ' ' +  colour + message);
		}
	}
	
	public void sendActionBar(String message, Player player) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(message).create());
	}
	
	private ChatColor colour = ChatColor.GREEN;
	private ChatColor highlight = ChatColor.BLUE;
	private ChatColor errorColour = ChatColor.RED;
	public String prefix;
	
	public void setDisplays(String newPrefix, ChatColor newColour, ChatColor newHighlight, ChatColor newErrorColour) {
		prefix = ChatColor.translateAlternateColorCodes('&', newPrefix);
		colour = newColour;
		highlight = newHighlight;
		errorColour = newErrorColour;
	}
	
	
}
