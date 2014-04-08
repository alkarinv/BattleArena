package mc.alk.arena.listeners.competition;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.Scheduler;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveMatchEvent;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.ServerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public enum InArenaListener implements Listener {
    INSTANCE;

    final Set<UUID> inArena = Collections.synchronizedSet(new HashSet<UUID>());
    final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    AtomicBoolean registered = new AtomicBoolean();
    AtomicBoolean listening = new AtomicBoolean();
    Integer timerid = null;

    @EventHandler
    public void onArenaPlayerEnterEvent(ArenaPlayerEnterMatchEvent event){
        if (Defaults.DEBUG_TRACE)Log.info( "  - onArenaPlayerEnterEvent " + event.getPlayer().getName());
        inArena.add(event.getPlayer().getID());
        if (listening.getAndSet(true))
            return;
        if (!registered.getAndSet(true)){
            if (timerid != null){
                Bukkit.getScheduler().cancelTask(timerid);
                timerid= null;
            }

            for (Listener l: listeners){
                Bukkit.getPluginManager().registerEvents(l, BattleArena.getSelf());
            }
        }
    }

    @EventHandler
    public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveMatchEvent event){
        if (Defaults.DEBUG_TRACE)Log.info( "  - onArenaPlayerLeaveEvent " + event.getPlayer().getName());

        if (inArena.remove(event.getPlayer().getID()) && inArena.isEmpty()){
            listening.set(false);
            if (timerid != null){
                Scheduler.cancelTask(timerid);}
            timerid = Scheduler.scheduleSynchronousTask(new Runnable(){
                @Override
                public void run() {
                    if (registered.getAndSet(false)){
                        for (Listener l: listeners){
                            HandlerList.unregisterAll(l);
                        }
                    }
                    timerid = null;
                }
            },600L);
        }
    }

    public boolean isPlayerInArena(UUID id) {
        return inArena.contains(id);
    }

    public static boolean inArena(UUID id) {
        return INSTANCE.inArena.contains(id);
    }
    public static boolean inArena(Player player) {
        return INSTANCE.inArena.contains(PlayerUtil.getID(player));
    }

    public static boolean inQueue(UUID id) {
        return BattleArena.getBAController().getArenaMatchQueue().isInQue(id);
    }
    @Deprecated
    /**
     * Will be switching over to using UUID
     */
    public static boolean inQueue(String name) {
        Player p = ServerUtil.findOnlinePlayer(name);
        return p != null && BattleArena.getBAController().getArenaMatchQueue().isInQue(PlayerUtil.getID(p));
    }

    public static void addListener(Listener listener){
        INSTANCE.listeners.add(listener);
    }


}
