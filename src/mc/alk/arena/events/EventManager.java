package mc.alk.arena.events;


import mc.alk.arena.Defaults;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class EventManager {
    public static void registerEvents(Listener listener, Plugin plugin) {
        if (Defaults.TESTSERVER)
            return;
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
