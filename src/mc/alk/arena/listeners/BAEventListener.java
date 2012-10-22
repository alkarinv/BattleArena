package mc.alk.arena.listeners;

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
public abstract class BAEventListener implements Listener  {

	final Class<? extends Event> bukkitEvent;

	public BAEventListener(final Class<? extends Event> bukkitEvent) {
		if (Defaults.DEBUG_EVENTS) System.out.println("Registering BAEventListener for type " + bukkitEvent);
		this.bukkitEvent = bukkitEvent;
	}
	public Class<? extends Event> getEvent(){
		return bukkitEvent;
	}
	public void stopListening(){
		HandlerList.unregisterAll(this);
	}

	public void startSpecificPlayerListening(){
		EventExecutor executor = new EventExecutor() {
			public void execute(Listener listener, Event event) throws EventException {
				doSpecificPlayerEvent(event);
			}
		};
		Bukkit.getPluginManager().registerEvent(bukkitEvent, this, EventPriority.HIGHEST, executor,BattleArena.getSelf());
	}

	public void startMatchListening(){
		EventExecutor executor = new EventExecutor() {
			public void execute(Listener listener, Event event) throws EventException {
				doMatchEvent(event);
			}
		};
		Bukkit.getPluginManager().registerEvent(bukkitEvent, this, EventPriority.HIGHEST, executor,BattleArena.getSelf());
	}

	public abstract void doSpecificPlayerEvent(Event event);

	public abstract void doMatchEvent(Event event);

}
