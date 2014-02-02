package mc.alk.arena.listeners.custom;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.util.Log;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 *
 * Bake and handling based on Bukkit and lahwran's fevents.
 * @author alkarin
 *
 */
public class ArenaEventListener extends GeneralEventListener {

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
     * add an arena listener to this bukkit event
     * @param rl RListener
     */
    protected synchronized void addMatchListener(RListener rl) {
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
    protected boolean removeMatchListener(RListener listener) {
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
            map.put(listener, count - 1);
            removed = false;
        }
        return removed;
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
