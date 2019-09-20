package wbs.extras.configurations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BarAnnouncement {
	
	private static int timerID = -1;
	private static BossBar bar = null;
	
	public static void stop() {
		if (timerID != -1) {
			Bukkit.getScheduler().cancelTask(timerID);
			bar.setVisible(false);
			bar.removeAll();

			for (Player player : Bukkit.getOnlinePlayers()) {
				bar.removePlayer(player);
			}
		}
	}
	
	public static void addPlayer(Player player) {
		if (bar != null) {
			bar.addPlayer(player);
		}
	}
	
	public static void start(long ticks) {
		stop();
		final Plugin pl = Bukkit.getPluginManager().getPlugin("WbsExtras");
		
		BarAnnouncement initial = bars.get(0);
		bar = Bukkit.createBossBar(initial.getMessage(), initial.getColour(), initial.getStyle());
		bar.setProgress(initial.getProgress());
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			bar.addPlayer(player);
		}
		
		timerID = new BukkitRunnable() {
			int currentIndex = 1;
			BarAnnouncement current;
			@Override
			public void run() {
				// Do this first in case there is only one entry and the initial 1 would
				// be out of bounds
				currentIndex = currentIndex % bars.size(); 
				
				current = bars.get(currentIndex);
				
				bar.setTitle(current.getMessage());
				bar.setColor(current.getColour());
				bar.setStyle(current.getStyle());
				bar.setProgress(current.getProgress());
				
				currentIndex = (currentIndex + 1);
			}
		}.runTaskTimerAsynchronously(pl, ticks, ticks).getTaskId();
	}
	
	private static ArrayList<BarAnnouncement> bars = new ArrayList<>();
	
	public static void addBar(BarAnnouncement announcement) {
		bars.add(announcement);
	}

	/************************************************/
	/*					END OF STATIC				*/
	/************************************************/
	
	private String message;
	private BarColor colour;
	private BarStyle style;
	private double progress;

	private String parseColours(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	public BarAnnouncement(String message, BarColor colour, BarStyle style, double progress) {
		this.message = parseColours(message);
		this.colour = colour;
		this.style = style;
		this.progress = progress;
	}
	
	public String getMessage() {
		return message;
	}
	public BarColor getColour() {
		return colour;
	}
	public BarStyle getStyle() {
		return style;
	}
	public double getProgress() {
		return progress;
	}
}










