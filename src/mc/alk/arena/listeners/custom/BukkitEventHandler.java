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
	MatchEventListener ml;
	SpecificPlayerEventListener spl;

	/**
	 * Construct a listener to listen for the given bukkit event
	 * @param bukkitEvent : which event we will listen for
	 * @param getPlayerMethod : a method which when not null and invoked will return a Player
	 */
	public BukkitEventHandler(final Class<? extends Event> bukkitEvent,
			org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
		ml = new MatchEventListener(bukkitEvent,bukkitPriority, getPlayerMethod);
		spl = new SpecificPlayerEventListener(bukkitEvent,bukkitPriority, getPlayerMethod);
		if (Defaults.DEBUG_EVENTS) Log.info("Registering BukkitEventListener for type " + bukkitEvent +" pm="+getPlayerMethod);
	}

	/**
	 * Does this event even have any listeners
	 * @return
	 */
	public boolean hasListeners(){
		return ml.hasListeners() || spl.hasListeners();
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
			spl.addListener(rl, players);
		} else {
			ml.addListener(rl);
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
			spl.removeListener(rl, players);
		} else {
			ml.removeListener(rl);
		}
	}

	/**
	 * Remove all listeners for this bukkit event
	 * @param rl
	 */
	public void removeAllListener(RListener rl) {
		spl.removeAllListeners(rl);
		ml.removeAllListeners(rl);
	}

	public MatchEventListener getMatchListener(){
		return ml;
	}

	public SpecificPlayerEventListener getSpecificPlayerListener(){
		return spl;
	}

}