package wbs.extras.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WbsSound {
	
	private Sound sound = null;
	private float pitch = 1;
	private float volume = 1;

	public WbsSound(Sound sound) {
		this.sound = sound;
	}
	public WbsSound(Sound sound, float pitch) {
		this.sound = sound;
		this.pitch = pitch;
	}
	
	public void play(Location loc) {
		World world = loc.getWorld();
		
		world.playSound(loc, sound, volume, pitch);
	}
	
	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}
	
	
}
