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

	public BukkitEventListener(final Class<? extends Event> bukkitEvent, EventPriority bukkitPriority) {
		if (Defaults.DEBUG_EVENTS) System.out.println("Registering BAEventListener for type " + bukkitEvent);
		this.bukkitEvent = bukkitEvent;
		this.bukkitPriority = bukkitPriority;
	}

	public Class<? extends Event> getEvent(){
		return bukkitEvent;
	}

	public void stopListening(){
		HandlerList.unregisterAll(this);
	}

	public void startListening(){
		EventExecutor executor = new EventExecutor() {
			public void execute(final Listener listener, final Event event) throws EventException {
				if (event.getClass() != bukkitEvent && !bukkitEvent.isAssignableFrom(event.getClass())){
					return;}
				invokeEvent(event);
			}
		};
		Bukkit.getPluginManager().registerEvent(bukkitEvent, this, bukkitPriority, executor,BattleArena.getSelf());
	}

	public abstract void invokeEvent(Event event);

	public abstract boolean hasListeners();

	public abstract void removeAllListeners(RListener rl);

}
