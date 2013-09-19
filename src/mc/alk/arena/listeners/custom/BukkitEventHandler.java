package mc.alk.arena.listeners.custom;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventMethod;
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
	public BukkitEventHandler(final Class<? extends Event> bukkitEvent, ArenaEventMethod aem) {
		if (aem.getPlayerMethod() != null){
			if (aem.isSpecificArenaPlayerMethod()){
				sapl = new SpecificArenaPlayerEventListener(bukkitEvent,aem.getBukkitPriority(), aem.getPlayerMethod());
			} else {
				spl = new SpecificPlayerEventListener(bukkitEvent,aem.getBukkitPriority(), aem.getPlayerMethod());
			}
		} else {
			ael = new ArenaEventListener(bukkitEvent,aem.getBukkitPriority(), aem.getPlayerMethod());
		}
		if (Defaults.DEBUG_EVENTS) Log.info("Registering BukkitEventListener for type " + bukkitEvent +" pm="+aem.getPlayerMethod());
	}

	/**
	 * Does this event even have any listeners
	 * @return
	 */
	public boolean hasListeners(){
		return (ael != null && ael.hasListeners()) || (spl != null && spl.hasListeners()) ||
				(sapl != null && sapl.hasListeners());
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
		if (spl != null) spl.removeAllListeners(rl);
		if (ael != null) ael.removeAllListeners(rl);
		if (sapl != null) sapl.removeAllListeners(rl);
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

	public void invokeArenaEvent(Set<ArenaListener> listeners, Event event) {
		if (ael != null) ael.invokeEvent(listeners, event);
	}

}