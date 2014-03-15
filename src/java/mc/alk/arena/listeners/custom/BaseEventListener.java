package mc.alk.arena.listeners.custom;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TimingUtil;
import mc.alk.arena.util.TimingUtil.TimingStat;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * @author alkarin
 *
 */
public abstract class BaseEventListener implements Listener  {
    final Class<? extends Event> bukkitEvent;
    final EventPriority bukkitPriority;
    static long total = 0;
    static long count=0;
    AtomicBoolean listening = new AtomicBoolean();
    AtomicBoolean registered = new AtomicBoolean();
    Integer timerid = null;
    EventExecutor executor = null;
    static TimingUtil timings;

    public BaseEventListener(final Class<? extends Event> bukkitEvent, EventPriority bukkitPriority) {
        if (Defaults.DEBUG_EVENTS) Log.info("Registering BAEventListener for type &5" + bukkitEvent.getSimpleName());
        this.bukkitEvent = bukkitEvent;
        this.bukkitPriority = bukkitPriority;
    }

    public Class<? extends Event> getEvent(){
        return bukkitEvent;
    }

    public void stopListening(){
        listening.set(false);

        if (BattleArena.getSelf().isEnabled()){
            final BaseEventListener bel = this;
            if (timerid != null){
                Bukkit.getScheduler().cancelTask(timerid);}
            timerid = Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
                @Override
                public void run() {
                    if (registered.getAndSet(false)){
                        HandlerList.unregisterAll(bel);
                    }
                    timerid = null;
                }
            },600L);
        }
    }

    public boolean isListening(){
        return listening.get();
    }

    public void startListening(){
        //noinspection PointlessBooleanExpression
        if (isListening() || Defaults.TESTSERVER)
            return;

        listening.set(true);
        if (timerid != null){
            Bukkit.getScheduler().cancelTask(timerid);
            timerid= null;
        }

        if (executor == null){
            if (Bukkit.getPluginManager().useTimings() || Defaults.DEBUG_TIMINGS){
                if (timings == null) {
                    timings = new TimingUtil();}
                executor = new EventExecutor() {
                    @Override
                    public void execute(final Listener listener, final Event event) throws EventException {
                        long startTime = System.nanoTime();
                        if (!listening.get() || !bukkitEvent.isAssignableFrom(event.getClass())){
                            return;}
                        TimingStat t = timings.getOrCreate(event.getClass().getSimpleName());
                        try{
                            invokeEvent(event);
                        }catch (Throwable e) {
                            Log.printStackTrace(e);
                        }
                        t.count+=1;
                        t.totalTime += System.nanoTime() - startTime;
                    }
                };
            } else {
                executor = new EventExecutor() {
                    @Override
                    public void execute(final Listener listener, final Event event) throws EventException {
                        if (!listening.get() || !bukkitEvent.isAssignableFrom(event.getClass())){
                            return;}
                        try{
                            invokeEvent(event);
                        }catch (Throwable e) {
                            Log.printStackTrace(e);
                        }
                    }
                };
            }

        }

        if (Defaults.TESTSERVER) return;

        if (!registered.getAndSet(true)){
            Bukkit.getPluginManager().registerEvent(bukkitEvent, this, bukkitPriority, executor,BattleArena.getSelf());
        }
    }

    public abstract void invokeEvent(Event event);

    public abstract boolean hasListeners();

    public abstract void removeAllListeners(RListener rl);
}
