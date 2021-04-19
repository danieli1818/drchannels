package me.danieli1818.drchannels.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class SchedulerUtils {

	private static SchedulerUtils instance = null;
	
	private Plugin plugin;
	
	private SchedulerUtils(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public static SchedulerUtils getInstance(Plugin plugin) {
		if (instance == null) {
			instance = new SchedulerUtils(plugin);
		}
		return instance;
	}
	
	public static SchedulerUtils getInstance() {
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	public int scheduleAsyncTask(long delay, Runnable runnable) {
		return getScheduler().scheduleAsyncDelayedTask(this.plugin, runnable, delay);
	}
	
	private BukkitScheduler getScheduler() {
		return this.plugin.getServer().getScheduler();
	}
	
}
