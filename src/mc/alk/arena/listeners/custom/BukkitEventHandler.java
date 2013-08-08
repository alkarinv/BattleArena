package mc.alk.arena.listeners.custom;

import java.lang.reflect.Method;
import java.util.Collection;

import mc.alk.arena.Defaults;
import mc.alk.arena.util.Log;

import org.bukkit.event.Event;



/**
 *
 * @author alkarin
 *
 */
public class BukkitEventHandler {
	ArenaEventListener ael;
	SpecificPlayerEventListener spl;
	SpecificArenaPlayerEventListener sapl;

	/**
	 * Construct a listener to listen for the given bukkit event
	 * @param bukkitEvent : which event we will listen for
	 * @param getPlayerMethod : a method which when not null and invoked will return a Player
	 */
	public BukkitEventHandler(final Class<? extends Event> bukkitEvent,
			org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
		ael = new ArenaEventListener(bukkitEvent,bukkitPriority, getPlayerMethod);
		spl = new SpecificPlayerEventListener(bukkitEvent,bukkitPriority, getPlayerMethod);
		sapl = new SpecificArenaPlayerEventListener(bukkitEvent,bukkitPriority, getPlayerMethod);
		if (Defaults.DEBUG_EVENTS) Log.info("Registering BukkitEventListener for type " + bukkitEvent +" pm="+getPlayerMethod);
	}

	/**
	 * Does this event even have any listeners
	 * @return
	 */
	public boolean hasListeners(){
		return ael.hasListeners() || spl.hasListeners() || sapl.hasListeners();
	}

	/**
	 * Add a player listener to this bukkit event
	 * @param rl
	 * @param matchState
	 * @param mem
	 * @param players
	 */
	public void addListener(RListener rl, Collection<String> players) {
		if (players != null && rl.isSpecificPlayerMethod()){
			if (rl.isSpecificArenaPlayerMethod()){
				sapl.addListener(rl, players);
			} else {
				spl.addListener(rl, players);
			}
		} else {
			ael.addListener(rl);
		}
	}

	/**
	 * remove a player listener from this bukkit event
	 * @param arenaListener
	 * @param matchState
	 * @param mem
	 * @param players
	 */
	public void removeListener(RListener rl, Collection<String> players) {
		if (players != null && rl.isSpecificPlayerMethod()){
			if (rl.isSpecificArenaPlayerMethod()){
				sapl.removeListener(rl, players);
			} else {
				spl.removeListener(rl, players);
			}
		} else {
			ael.removeListener(rl);
		}
	}

	/**
	 * Remove all listeners for this bukkit event
	 * @param rl
	 */
	public void removeAllListener(RListener rl) {
		spl.removeAllListeners(rl);
		ael.removeAllListeners(rl);
		sapl.removeAllListeners(rl);
	}

	public ArenaEventListener getMatchListener(){
		return ael;
	}

	public SpecificPlayerEventListener getSpecificPlayerListener(){
		return spl;
	}

	public SpecificArenaPlayerEventListener getSpecificArenaPlayerListener(){
		return sapl;
	}

}