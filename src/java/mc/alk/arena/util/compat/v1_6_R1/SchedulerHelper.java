package mc.alk.arena.util.compat.v1_6_R1;

import mc.alk.arena.util.compat.ISchedulerHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SchedulerHelper implements ISchedulerHelper {


    @Override
    public int scheduleAsyncTask(Plugin plugin, Runnable task, long ticks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,task,ticks).getTaskId();
    }

}
