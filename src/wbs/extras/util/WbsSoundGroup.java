package wbs.extras.util;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WbsSoundGroup {
	public static Plugin pl;
	public static void setPlugin(Plugin pl) {
		WbsSoundGroup.pl = pl;
	}
	
	private ArrayList<WbsSound> allSounds = new ArrayList<>();
	private ArrayList<Long> delay = new ArrayList<>();
	
	public WbsSoundGroup(WbsSound ... sounds) {
		for (WbsSound sound : sounds) {
			addSound(sound, 0);
		}
	}
	
	public void addSound(WbsSound sound, long ticks) {
		allSounds.add(sound);
		delay.add(ticks);
	}
	
	public void play(Location loc) {
		playNext(loc, 0);
	}
	
	private void playNext(Location loc, int index) {
		if (allSounds.size() > index) {
			long tickDelay = delay.get(index);
			if (tickDelay == 0) { // Don't schedule if no delay
				
				allSounds.get(index).play(loc);
				playNext(loc, index + 1);
				
			} else {
				
				new BukkitRunnable() {
					@Override
					public void run() {
						allSounds.get(index).play(loc);
						playNext(loc, index + 1);
					}
				}.runTaskLater(pl, tickDelay);
				
			}
		}
	}
}