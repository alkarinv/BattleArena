package mc.alk.arena.listeners.custom;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventMethod;
import mc.alk.arena.util.Log;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.Set;



/**
 *
 * @author alkarin
 *
 */
public class BukkitEventHandler {
    BukkitEventListener bel;
    ArenaEventListener ael;
    SpecificPlayerEventListener spl;
    SpecificArenaPlayerEventListener sapl;

    /**
     * Construct a listener to listen for the given bukkit event
     * @param bukkitEvent : which event we will listen for
     * @param aem : a method which when not null and invoked will return a Player
     */
    public BukkitEventHandler(final Class<? extends Event> bukkitEvent, ArenaEventMethod aem) {
        if (aem.getPlayerMethod() != null){
            if (aem.isSpecificArenaPlayerMethod()){
                sapl = new SpecificArenaPlayerEventListener(bukkitEvent,aem.getBukkitPriority(), aem.getPlayerMethod());
            } else {
                spl = new SpecificPlayerEventListener(bukkitEvent,aem.getBukkitPriority(), aem.getPlayerMethod());
            }
        } else {
            if (aem.isBAEvent()){
                ael = new ArenaEventListener(bukkitEvent,aem.getBukkitPriority(), null);
            } else{
                bel = new BukkitEventListener(bukkitEvent,aem.getBukkitPriority(), null);
            }
        }
        if (Defaults.DEBUG_EVENTS) Log.info("Registering BaseEventListener for type &6" +
                bukkitEvent.getSimpleName() + " &fpm=" + (aem.getPlayerMethod() == null ? "null" : aem.getPlayerMethod().getName()));
    }

    /**
     * Does this event even have any listeners
     * @return true if there are listeners
     */
    public boolean hasListeners(){
        return (ael != null && ael.hasListeners()) || (bel != null && bel.hasListeners()) ||
                (sapl != null && sapl.hasListeners() || (spl != null && spl.hasListeners()));
    }

    /**
     * Add a player listener to this bukkit event
     * @param rl RListener
     * @param players the players
     */
    public void addListener(RListener rl, Collection<String> players) {
        if (players != null && rl.isSpecificPlayerMethod()){
            if (rl.isSpecificArenaPlayerMethod()){
                sapl.addListener(rl, players);
            } else {
                spl.addListener(rl, players);
            }
        } else {
            if (rl.getMethod().isBAEvent()){
                ael.addListener(rl);
            } else{
                bel.addListener(rl);
            }

        }
    }

    /**
     * remove a player listener from this bukkit event
     * @param rl RListener
     * @param players the players
     */
    public void removeListener(RListener rl, Collection<String> players) {
        if (players != null && rl.isSpecificPlayerMethod()){
            if (rl.isSpecificArenaPlayerMethod()){
                sapl.removeListener(rl, players);
            } else {
                spl.removeListener(rl, players);
            }
        } else {
            if (rl.getMethod().isBAEvent()){
                ael.removeListener(rl);
            } else{
                bel.removeListener(rl);
            }
        }
    }

    /**
     * Remove all listeners for this bukkit event
     * @param rl RListener
     */
    public void removeAllListener(RListener rl) {
        if (spl != null) spl.removeAllListeners(rl);
        if (sapl != null) sapl.removeAllListeners(rl);
        if (ael != null) ael.removeAllListeners(rl);
        if (bel != null) bel.removeAllListeners(rl);
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
