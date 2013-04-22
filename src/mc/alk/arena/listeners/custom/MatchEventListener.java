package mc.alk.arena.listeners.custom;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.util.Log;

import org.bukkit.event.Event;


/**
 *
 * Bake and handling based on Bukkit and lahwran's fevents.
 * @author alkarin
 *
 */
public class MatchEventListener extends BukkitEventListener{
	/** Set of arena listeners */
	final public EnumMap<EventPriority, List<RListener>> listeners = new EnumMap<EventPriority, List<RListener>>(EventPriority.class);

	private volatile RListener[] handlers = null;

	/**
	 * Construct a listener to listen for the given bukkit event
	 * @param bukkitEvent : which event we will listen for
	 * @param getPlayerMethod : a method which when not null and invoked will return a Player
	 */
	public MatchEventListener(final Class<? extends Event> bukkitEvent,
			org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
		super(bukkitEvent, bukkitPriority);
		if (Defaults.DEBUG_EVENTS) Log.info("Registering GenericPlayerEventListener for type " + bukkitEvent +" pm="+getPlayerMethod);
	}

	/**
	 * Does this event even have any listeners
	 * @return
	 */
	@Override
	public boolean hasListeners(){
		return !listeners.isEmpty();
	}

	/**
	 * Get the set of arena listeners
	 * @return
	 */
	public EnumMap<EventPriority, List<RListener>> getListeners(){
		return listeners;
	}

	/**
	 * Add a player listener to this bukkit event
	 * @param rl
	 * @param matchState
	 * @param mem
	 * @param players
	 */
	public void addListener(RListener rl) {
		addMatchListener(rl);
	}

	/**
	 * remove a player listener from this bukkit event
	 * @param arenaListener
	 * @param matchState
	 * @param mem
	 * @param players
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
	 * @param spl
	 * @return
	 */
	public synchronized void addMatchListener(RListener spl) {
		if (!hasListeners()){
			startListening();}
		List<RListener> l = listeners.get(spl.getPriority());
		if (l == null){
			l = new ArrayList<RListener>();
			listeners.put(spl.getPriority(), l);
		}
		l.add(spl);
		handlers = null;
		bake();
	}

	/**
	 * remove an arena listener to this bukkit event
	 * @param spl
	 * @return
	 */
	private boolean removeMatchListener(RListener listener) {
		final List<RListener> list = listeners.get(listener.getPriority());
		if (list==null)
			return false;
		boolean changed = false;
		Iterator<RListener> iter = list.iterator();
		while (iter.hasNext()){
			RListener rl = iter.next();
			if (rl.equals(listener)){
				iter.remove();
				changed = true;
			}
		}
		if (!hasListeners()){
			stopListening();}
		if (changed) bake();
		return changed;
	}

	private synchronized void bake() {
		if (handlers != null) return;
		List<RListener> entries = new ArrayList<RListener>();
		for (Entry<EventPriority,List<RListener>> entry : listeners.entrySet()){
			entries.addAll(entry.getValue());}
		handlers = entries.toArray(new RListener[entries.size()]);
	}

	/**
	 * Bake code from Bukkit
	 * @return
	 */
	public RListener[] getRegisteredListeners() {
		RListener[] handlers;
		while ((handlers = this.handlers) == null) bake(); // This prevents fringe cases of returning null
		return handlers;
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