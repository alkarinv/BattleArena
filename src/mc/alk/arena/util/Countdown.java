package mc.alk.arena.util;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Countdown implements Runnable{
	public static interface CountdownCallback{
		/**
		 * 
		 * @param secondsRemaining
		 * @return whether to cancel
		 */
		public boolean intervalTick(int secondsRemaining);
	}

	int interval,seconds;
	CountdownCallback callback;
	Integer timerId;
	Plugin plugin;

	public Countdown(final Plugin plugin, int seconds, int interval, CountdownCallback callback){
		this.interval = interval;
		this.callback = callback;
		this.plugin = plugin;
		final int rem = seconds % interval;
		/// Lets get rid of the remainder first, so that the rest of the events
		/// are a multiple of the timeInterval
		final long time = (rem != 0? rem : interval) * 20L;
		this.seconds = seconds - (rem != 0? rem : interval);
		this.timerId  = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this,
				(long)(time * Defaults.TICK_MULT));
	}

	public void run() {
		timerId = null;
		final boolean continueOn = callback.intervalTick(seconds);
		if (!continueOn)
			return;
		TimeUtil.testClock();
		final Plugin plugin = BattleArena.getSelf();
		seconds -= interval;

		if (seconds >= 0){
			timerId  = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this,
					(long) (interval*20L * Defaults.TICK_MULT));
		}
	}
	public void stop(){
		if (timerId != null){
			Bukkit.getScheduler().cancelTask(timerId);
		}
	}
	public String toString(){
		return "[Countdown " + seconds+":"+interval+"]";
	}
}

