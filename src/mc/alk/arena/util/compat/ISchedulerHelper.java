package mc.alk.arena.util.compat;

import org.bukkit.plugin.Plugin;

public interface ISchedulerHelper {
    int scheduleAsyncTask(Plugin plugin, Runnable task, long ticks);
}
