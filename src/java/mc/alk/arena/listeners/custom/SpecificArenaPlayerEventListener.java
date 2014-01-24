package mc.alk.arena.listeners.custom;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.Log;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.TreeSet;


/**
 *
 * @author alkarin
 *
 */
public class SpecificArenaPlayerEventListener extends SpecificPlayerEventListener{
	/**
	 * Construct a listener to listen for the given bukkit event
	 * @param bukkitEvent : which event we will listen for
	 * @param getPlayerMethod : a method which when not null and invoked will return a Player
	 */
	public SpecificArenaPlayerEventListener(final Class<? extends Event> bukkitEvent,
			org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
		super(bukkitEvent, bukkitPriority,getPlayerMethod);
		if (Defaults.DEBUG_EVENTS) Log.info("Registering SpecificArenaPlayerEventListener for type " + bukkitEvent +" pm="+getPlayerMethod);
	}

	/**
	 * do the bukkit event for players
	 * @param event Event
	 */
	@Override
	public void invokeEvent(Event event){
		final ArenaPlayer player = getEntityFromMethod(event, getPlayerMethod);
		callListeners(event, player);
	}

	private void callListeners(Event event, final ArenaPlayer p) {
		TreeSet<RListener> spls = listeners.getSafe(p.getName());
		if (spls == null){
			return;}
		doMethods(event,p, spls);
	}

	private void doMethods(Event event, final ArenaPlayer p, Collection<RListener> lmethods) {
		/// For each of the splisteners methods that deal with this BukkitEvent
		for(RListener lmethod: lmethods){
			final Method method = lmethod.getMethod().getMethod();
			final Class<?>[] types = method.getParameterTypes();
			final Object[] os = new Object[1];
			os[0] = event;
			try {
				method.invoke(lmethod.getListener(), os); /// Invoke the listening arenalisteners method
			} catch (Exception e){
				Log.err("["+BattleArena.getNameAndVersion()+" Error] method=" + method + ",  types.length=" +types.length +",  p=" + p +",  listener="+lmethod);
				Log.printStackTrace(e);
			}
		}
	}

	private ArenaPlayer getEntityFromMethod(final Event event, final Method method) {
		try{
			Object o = method.invoke(event);
			if (o instanceof ArenaPlayer)
				return (ArenaPlayer) o;
			return null;
		}catch(Exception e){
			Log.printStackTrace(e);
			return null;
		}
	}
}
