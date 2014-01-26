package mc.alk.arena.listeners.custom;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.util.Log;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 *
 * Bake and handling based on Bukkit and lahwran's fevents.
 * @author alkarin
 *
 */
public class ArenaEventListener extends BukkitEventListener{
	/** Set of arena listeners */
	final public EnumMap<EventPriority, Map<RListener,Integer>> listeners =
			new EnumMap<EventPriority, Map<RListener,Integer>>(EventPriority.class);

	private volatile RListener[] handlers = null;

	/**
	 * Construct a listener to listen for the given bukkit event
	 * @param bukkitEvent : which event we will listen for
	 * @param getPlayerMethod : a method which when not null and invoked will return a Player
	 */
	public ArenaEventListener(final Class<? extends Event> bukkitEvent,
			org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
		super(bukkitEvent, bukkitPriority);
		if (Defaults.DEBUG_EVENTS) Log.info("Registering GenericPlayerEventListener for type " +
                bukkitEvent.getSimpleName() +" pm="+(getPlayerMethod == null ? "null" : getPlayerMethod.getName()));
	}

	/**
	 * Does this event even have any listeners
	 * @return true if there are listeners, false otherwise
	 */
	@Override
	public boolean hasListeners(){
		return !listeners.isEmpty();
	}

	/**
	 * Get the set of arena listeners
	 * @return map of listeners
	 */
	public EnumMap<EventPriority, Map<RListener,Integer>> getListeners(){
		return listeners;
	}

	/**
	 * Add a player listener to this bukkit event
	 * @param rl RListener
	 */
	public void addListener(RListener rl) {
		addMatchListener(rl);
	}

	/**
	 * remove a player listener from this bukkit event
	 * @param rl RListener
	 */
	public synchronized void removeListener(RListener rl) {
		if (Defaults.DEBUG_EVENTS) System.out.println("    removing listener listener="+rl);
		removeMatchListener(rl);
	}

	@Override
	public synchronized void removeAllListeners(RListener rl) {
		if (Defaults.DEBUG_EVENTS) System.out.println("    removing all listeners  listener="+rl);
		removeMatchListener(rl);
	}

	/**
	 * add an arena listener to this bukkit event
	 * @param rl RListener
	 */
	private synchronized void addMatchListener(RListener rl) {
        if (!isListening()){
            startListening();}

        Map<RListener,Integer> l = listeners.get(rl.getPriority());
		if (l == null){
			l = new TreeMap<RListener,Integer>(new Comparator<RListener>(){
				@Override
				public int compare(RListener o1, RListener o2) {
					return o1.getListener().equals(o2.getListener()) ? 0 :
						new Integer(o1.hashCode()).compareTo(o2.hashCode());
				}
			});
			listeners.put(rl.getPriority(), l);
		}

		Integer count = l.get(rl);
		if (count == null){
			l.put(rl,1);
			handlers = null;
			bake();
		} else {
			l.put(rl,count+1);
		}
	}

	/**
	 * remove an arena listener to this bukkit event
	 * @param listener RListener
	 * @return whether listener was found and removed
	 */
	private boolean removeMatchListener(RListener listener) {
		final Map<RListener,Integer> map = listeners.get(listener.getPriority());
		if (map==null)
			return false;
		Integer count = map.get(listener);
        boolean removed;
        if (count == null || count == 1){
			map.remove(listener);
			handlers = null;
			removed = true;
		} else {
			map.put(listener, count-1);
            removed = false;
		}
        if (removed && !hasListeners() && isListening()){
            stopListening();}
        return removed;
    }

	private synchronized void bake() {
		if (handlers != null) return;
		List<RListener> entries = new ArrayList<RListener>();
		for (Entry<EventPriority,Map<RListener,Integer>> entry : listeners.entrySet()){
			entries.addAll(entry.getValue().keySet());}
		handlers = entries.toArray(new RListener[entries.size()]);
	}

	/**
	 * Bake code from Bukkit
	 * @return array of listeners
	 */
	public RListener[] getRegisteredListeners() {
		RListener[] handlers;
		while ((handlers = this.handlers) == null) bake(); // This prevents fringe cases of returning null
		return handlers;
	}

	public void invokeEvent(final Set<ArenaListener> listeners, final Event event){
		/// For each ArenaListener class that is listening
		final RListener[] rls = getRegisteredListeners();
		for (RListener rl: rls){
			if (!listeners.contains(rl.al))
				continue;
			try {
				rl.getMethod().getMethod().invoke(rl.getListener(), event); /// Invoke the listening arenalisteners method
			} catch (Exception e){
				Log.printStackTrace(e);
			}
		}
	}

	@Override
	public void invokeEvent(final Event event){
		/// For each ArenaListener class that is listening
		final RListener[] rls = getRegisteredListeners();
		for (RListener rl: rls){
			try {
				rl.getMethod().getMethod().invoke(rl.getListener(), event); /// Invoke the listening arenalisteners method
			} catch (Exception e){
				Log.printStackTrace(e);
			}
		}
	}
}
