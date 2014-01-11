package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author alkarin
 *
 */
public class Scheduler {
	static int count = 0; /// count of current async timers

	/** Our current async timers */
	static Map<Integer,Timer> timers = new ConcurrentHashMap<Integer,Timer>();

    static class CompletedTask extends TimerTask{
		final Runnable r;
		final int id;
		public CompletedTask(Runnable r, int id) {
			this.r = r;
			this.id = id;
		}

		@Override
		public void run() {
			timers.remove(id);
			r.run();
		}
	}

	public static int scheduleAsynchrounousTask(Runnable task) {
		return scheduleAsynchrounousTask(task, 0);
	}

	public static int scheduleAsynchrounousTask(Runnable task, int ticks) {
		int tid = count++;
		Timer t = new Timer();
		t.schedule(new CompletedTask(task,tid), ticks*20*1000);
		timers.put(tid, t);
		return tid;
	}

	public static int scheduleSynchrounousTask(Runnable task){
		return scheduleSynchrounousTask(task,0);
	}

    public static int scheduleSynchrounousTask(Runnable task, int ticks) {
        return scheduleSynchrounousTask(BattleArena.getSelf(), task, ticks);
    }

    public static int scheduleSynchrounousTask(Plugin plugin, Runnable task){
        return scheduleSynchrounousTask(plugin, task, 0);
    }

    public static int scheduleSynchrounousTask(Plugin plugin, Runnable task, int ticks){
        if (Defaults.TESTSERVER) return scheduleAsynchrounousTask(task,ticks);
        return Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, ticks);
    }

    public static void cancelTask(int taskid) {
        if (Defaults.TESTSERVER) return;
        Bukkit.getScheduler().cancelTask(taskid);
    }

}
