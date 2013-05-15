package mc.alk.arena.util;

import mc.alk.arena.Defaults;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Countdown implements Runnable{
	static int count = 0;
	int id = count++;

	public static interface CountdownCallback{
		/**
		 *
		 * @param secondsRemaining
		 * @return whether to cancel
		 */
		public boolean intervalTick(int secondsRemaining);
	}

	Long startTime, expectedEndTime;
	int interval,seconds;
	CountdownCallback callback;
	Integer timerId;
	Plugin plugin;

	public Countdown(final Plugin plugin, Integer seconds, Integer interval, CountdownCallback callback){
		this.interval = interval == null || interval <= 0 ? seconds : interval;
		this.callback = callback;
		this.plugin = plugin;
		final int rem = seconds % this.interval;
		/// Lets get rid of the remainder first, so that the rest of the events
		/// are a multiple of the timeInterval
		final long time = (rem != 0? rem : this.interval) * 20L;
		this.seconds = seconds - (rem != 0? rem : this.interval);
		startTime = System.currentTimeMillis();
		expectedEndTime = startTime + seconds*1000;
		this.timerId  = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this,
				(long)(time * Defaults.TICK_MULT));
	}

	public void run() {
		timerId = null;

		final boolean continueOn = callback.intervalTick(seconds);
		if (!continueOn || seconds <=0)
			return;
		TimeUtil.testClock();

		if (seconds > 0){
			timerId  = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this,
					(long) (interval*20L * Defaults.TICK_MULT));
		}
		seconds -= interval;
	}
	public void stop(){
		if (timerId != null){
			Bukkit.getScheduler().cancelTask(timerId);
			timerId = null;
		}
	}
	@Override
	public String toString(){
		return "[Countdown " + seconds+":"+interval+"]";
	}

	public Long getTimeRemaining(){
		return expectedEndTime == null ? null : expectedEndTime - System.currentTimeMillis();
	}
}

