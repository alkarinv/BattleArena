package mc.alk.arena.listeners.custom;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.listeners.custom.RListener.RListenerPriorityComparator;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MapOfTreeSet;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.lang.reflect.Method;
import java.util.Collection;


/**
 *
 * @author alkarin
 *
 */
class SpecificPlayerEventListener extends BaseEventListener {
    /** map of player to listeners listening for that player */
    final protected MapOfTreeSet<String,RListener> listeners =
            new MapOfTreeSet<String,RListener>(RListener.class,
                    new RListenerPriorityComparator());

    /** The method which will return a Player if invoked */
    final Method getPlayerMethod;

    /**
     * Construct a listener to listen for the given bukkit event
     * @param bukkitEvent : which event we will listen for
     * @param getPlayerMethod : a method which when not null and invoked will return a Player
     */
    public SpecificPlayerEventListener(final Class<? extends Event> bukkitEvent,
                                       org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
        super(bukkitEvent, bukkitPriority);
        if (Defaults.DEBUG_EVENTS) Log.info("Registering GenericPlayerEventListener for type " + bukkitEvent +" pm="+getPlayerMethod);
        this.getPlayerMethod = getPlayerMethod;
    }

    /**
     * Does this event even have any listeners
     * @return true if has listeners
     */
    @Override
    public boolean hasListeners(){
        return !listeners.isEmpty() ;
    }

    /**
     * Get the map of players to listeners
     * @return players
     */
    public MapOfTreeSet<String,RListener> getListeners(){
        return listeners;
    }

    /**
     * Returns the players being listened for in this event
     * @return players
     */
    public Collection<String> getPlayers(){
        return listeners.keySet();
    }

    /**
     * Add a player listener to this bukkit event
     * @param rl RListener
     * @param players the players
     */
    public void addListener(RListener rl, Collection<String> players) {
        if (Defaults.DEBUG_EVENTS) Log.info("--adding listener   players="+players+" listener="+rl + "  " +
                ((players != null && rl.isSpecificPlayerMethod()) ? " MatchListener" : " SpecificPlayerListener" ));
        for (String player: players){
            addSPListener(player, rl);}
    }

    /**
     * remove a player listener from this bukkit event
     * @param rl RListener
     * @param players the players
     */
    public synchronized void removeListener(RListener rl, Collection<String> players) {
        if (Defaults.DEBUG_EVENTS) System.out.println("    removing listener  player="+players+"   listener="+rl);

        for (String player: players){
            removeSPListener(player, rl);}
    }

    @Override
    public synchronized void removeAllListeners(RListener rl) {
        if (Defaults.DEBUG_EVENTS) System.out.println("    removing all listeners  listener="+rl);
        synchronized(listeners){
            for (String name : listeners.keySet()){
                listeners.remove(name, rl);
            }
        }
    }

    /**
     * Add a listener for a specific player
     * @param p player
     * @param spl RListener
     */
    public void addSPListener(String p, RListener spl) {
        if (!isListening()){
            startListening();}
        listeners.add(p,spl);
    }

    /**
     * Remove a listener for a specific player
     * @param p the player
     * @param spl RListener
     * @return player was removed from collection
     */
    public boolean removeSPListener(String p, RListener spl) {
        final boolean removed = listeners.remove(p,spl);
        if (removed && !hasListeners() && isListening()){
            stopListening();}
        return removed;
    }


    /**
     * do the bukkit event for players
     * @param event Event
     */
    @Override
    public void invokeEvent(Event event){
        /// Need special handling of Methods that have 2 entities involved,
        /// as either entity may be in a match
        /// These currently use getClass() for speed and the fact that there aren't bukkit
        /// subclasses at this point.  These would need to change to instanceof if subclasses are
        /// created
        if (event.getClass() == EntityDamageByEntityEvent.class){
            doEntityDamageByEntityEvent((EntityDamageByEntityEvent)event);
            return;
        } else if (event.getClass() == EntityDeathEvent.class){
            doEntityDeathEvent((EntityDeathEvent)event);
            return;
        } else if (event instanceof EntityDamageEvent){
            doEntityDamageEvent((EntityDamageEvent)event);
            return;
        }

        final Entity entity = getEntityFromMethod(event, getPlayerMethod);
        if (!(entity instanceof Player))
            return;
        doMethods(event, (Player) entity);
    }

    private void doMethods(final Event event, final Player p) {
        RListener[] lmethods = listeners.getSafe(p.getName());
        if (lmethods == null){
            return;}
        /// For each of the splisteners methods that deal with this BukkitEvent
        for(RListener lmethod: lmethods){
            try {
                lmethod.getMethod().getMethod().invoke(lmethod.getListener(), event); /// Invoke the listening arenalisteners method
            } catch (Exception e){
                Log.err("["+BattleArena.getNameAndVersion()+" Error] method=" + lmethod.getMethod().getMethod() +
                        ",  types.length=" +lmethod.getMethod().getMethod().getParameterTypes().length +
                        ",  p=" + p +",  listener="+lmethod);
                Log.printStackTrace(e);
            }
        }
    }

    private void doEntityDeathEvent(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player && listeners.containsKey(((Player)event.getEntity()).getName()) ){
            doMethods(event, (Player) event.getEntity());
            return;
        }
        ArenaPlayer ap = DmgDeathUtil.getPlayerCause(event.getEntity().getLastDamageCause());
        if (ap == null)
            return;
        if (listeners.containsKey(ap.getName())){
            doMethods(event, ap.getPlayer());
        }
    }

    private void doEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && listeners.containsKey(((Player)event.getEntity()).getName()) ){
            doMethods(event, (Player) event.getEntity());
            return;
        }
        if (event.getDamager() instanceof Player && listeners.containsKey(((Player)event.getDamager()).getName())){
            doMethods(event, (Player) event.getDamager());
            return;
        }

        Player player = null;
        if (event.getDamager() instanceof Projectile){ /// we have some sort of projectile, maybe shot by a player?
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player){ /// projectile was shot by a player
                player= (Player) proj.getShooter();
            }
        }
        if (player != null){
            doMethods(event, player);
        }
        /// Else the target wasnt a player, and the shooter wasnt a player.. nothing to do
    }

    private void doEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player){
            doMethods(event, (Player) event.getEntity());
            return;
        }

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
        ArenaPlayer damager = DmgDeathUtil.getPlayerCause(lastDamage);
        if (damager != null){
            doMethods(event, damager.getPlayer());
        }
    }

    private Entity getEntityFromMethod(final Event event, final Method method) {
        try{
            Object o = method.invoke(event);
            if (o instanceof Entity)
                return (Entity) o;
            return null;
        }catch(Exception e){
            Log.printStackTrace(e);
            return null;
        }
    }
}
