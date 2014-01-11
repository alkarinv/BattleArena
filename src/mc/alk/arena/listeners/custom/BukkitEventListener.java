package mc.alk.arena.listeners.custom;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * @author alkarin
 *
 */
public abstract class BukkitEventListener implements Listener  {
	final Class<? extends Event> bukkitEvent;
	final EventPriority bukkitPriority;
	static long total = 0;
	static long count=0;
	AtomicBoolean listening = new AtomicBoolean();
	AtomicBoolean registered = new AtomicBoolean();
	Integer timerid = null;
	EventExecutor executor = null;
	static Map<String,TimingStat> timings = new HashMap<String,TimingStat>();
	static boolean useTimings = false;

	public BukkitEventListener(final Class<? extends Event> bukkitEvent, EventPriority bukkitPriority) {
		if (Defaults.DEBUG_EVENTS) System.out.println("Registering BAEventListener for type " + bukkitEvent.getSimpleName());
		this.bukkitEvent = bukkitEvent;
		this.bukkitPriority = bukkitPriority;
	}

	public Class<? extends Event> getEvent(){
		return bukkitEvent;
	}

	public void stopListening(){
		listening.set(false);

		if (BattleArena.getSelf().isEnabled()){
			final BukkitEventListener bel = this;
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
		if (isListening() || Defaults.TESTSERVER)
			return;

		listening.set(true);
		if (timerid != null){
			Bukkit.getScheduler().cancelTask(timerid);
			timerid= null;
		}
		if (executor == null){
			if (Bukkit.getPluginManager().useTimings() || useTimings){
				executor = new EventExecutor() {
					public void execute(final Listener listener, final Event event) throws EventException {
						long startTime = System.nanoTime();
						if (!isListening() ||
								(event.getClass() != bukkitEvent &&
								!bukkitEvent.isAssignableFrom(event.getClass()))){
							return;}
						TimingStat t = timings.get(event.getClass().getSimpleName());
						if (t == null){
							t = new TimingStat();
							timings.put(event.getClass().getSimpleName(),t);
						}
						invokeEvent(event);
						t.count+=1;
						t.totalTime += System.nanoTime() - startTime;
					}
				};
			} else {
				executor = new EventExecutor() {
					public void execute(final Listener listener, final Event event) throws EventException {
						if (!isListening() ||
								(event.getClass() != bukkitEvent &&
								!bukkitEvent.isAssignableFrom(event.getClass()))){
							return;}

						invokeEvent(event);
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

	public static Map<String,TimingStat> getTimings(){
		return timings;
	}

	public static void setTimings(boolean set){
		useTimings = set;
	}
}
