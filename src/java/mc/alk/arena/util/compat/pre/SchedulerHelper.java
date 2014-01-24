package mc.alk.arena.util.compat.pre;

import mc.alk.arena.util.compat.ISchedulerHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SchedulerHelper implements ISchedulerHelper {

    @SuppressWarnings("deprecation")
    @Override
    public int scheduleAsyncTask(Plugin plugin, Runnable task, long ticks) {
        return Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, task,ticks);
    }
}
