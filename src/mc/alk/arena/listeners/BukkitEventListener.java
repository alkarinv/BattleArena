package mc.alk.arena.listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.teams.Team;
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


/**
 *
 * @author alkarin
 *
 */
public class BukkitEventListener extends BAEventListener{
	/** map of player to listeners listening for that player */
	final public MapOfTreeSet<String,RListener> listeners = new MapOfTreeSet<String,RListener>();

	/** Set of arena listeners */
	final public EnumMap<EventPriority, List<RListener>> mlisteners = new EnumMap<EventPriority, List<RListener>>(EventPriority.class);

	private volatile RListener[] handlers = null;

	/** The method which will return a Player if invoked */
	final Method getPlayerMethod;

	/**
	 * Construct a listener to listen for the given bukkit event
	 * @param bukkitEvent : which event we will listen for
	 * @param getPlayerMethod : a method which when not null and invoked will return a Player
	 */
	public BukkitEventListener(final Class<? extends Event> bukkitEvent,
			org.bukkit.event.EventPriority bukkitPriority, Method getPlayerMethod) {
		super(bukkitEvent, bukkitPriority);
		if (Defaults.DEBUG_EVENTS) Log.info("Registering GenericPlayerEventListener for type " + bukkitEvent +" pm="+getPlayerMethod);
		this.getPlayerMethod = getPlayerMethod;
	}

	/**
	 * Does this event even have any listeners
	 * @return
	 */
	public boolean hasListeners(){
		return (!listeners.isEmpty() || !mlisteners.isEmpty());
	}

	/**
	 * Get the map of players to listeners
	 * @return
	 */
	public MapOfTreeSet<String,RListener> getListeners(){
		return listeners;
	}
	/**
	 * Get the set of arena listeners
	 * @return
	 */
	public EnumMap<EventPriority, List<RListener>> getMatchListeners(){
		return mlisteners;
	}

	/**
	 * Returns the players being listened for in this event
	 * @return
	 */
	public Collection<String> getPlayers(){
		return listeners.keySet();
	}

	/**
	 * Add a player listener to this bukkit event
	 * @param rl
	 * @param matchState
	 * @param mem
	 * @param players
	 */
	public void addListener(RListener rl, Collection<String> players) {
		if (Defaults.DEBUG_EVENTS) Log.info("--adding listener   players="+players+" listener="+rl + "  " +
				((players != null && rl.isSpecificPlayerMethod()) ? " MatchListener" : " SpecificPlayerListener" ));
		if (players != null && rl.isSpecificPlayerMethod()){
			for (String player: players){
				addSPListener(player, rl);}
		} else {
			addMatchListener(rl);
		}
	}

	/**
	 * remove a player listener from this bukkit event
	 * @param arenaListener
	 * @param matchState
	 * @param mem
	 * @param players
	 */
	public synchronized void removeListener(RListener rl, Collection<String> players) {
		if (Defaults.DEBUG_EVENTS) System.out.println("    removing listener  player="+players+"   listener="+rl);

		if (players != null && rl.isSpecificPlayerMethod()){
			for (String player: players){
				removeSPListener(player, rl);}
		} else {
			removeMatchListener(rl);
		}
	}

	public synchronized void removeAllListener(RListener rl) {
		if (Defaults.DEBUG_EVENTS) System.out.println("    removing all listeners  listener="+rl);
		synchronized(listeners){
			for (String name : listeners.keySet()){
				listeners.remove(name, rl);
			}
		}
		removeMatchListener(rl);

	}

	/**
	 * add an arena listener to this bukkit event
	 * @param spl
	 * @return
	 */
	public synchronized void addMatchListener(RListener spl) {
		if (!hasListeners()){
			startMatchListening();}
		List<RListener> l = mlisteners.get(spl.getPriority());
		if (l == null){
			l = new ArrayList<RListener>();
			mlisteners.put(spl.getPriority(), l);
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
	public boolean removeMatchListener(RListener listener) {
		final List<RListener> list = mlisteners.get(listener.getPriority());
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
		for (Entry<EventPriority,List<RListener>> entry : mlisteners.entrySet()){
			entries.addAll(entry.getValue());}
		handlers = entries.toArray(new RListener[entries.size()]);
	}
	/**
	 * Add a listener for a specific player
	 * @param player
	 * @param spl
	 */
	public void addSPListener(String p, RListener spl) {
		if (!hasListeners()){
			startSpecificPlayerListening();}
		listeners.add(p,spl);
	}

	/**
	 * Remove a listener for a specific player
	 * @param player
	 * @param spl
	 * @return
	 */
	public boolean removeSPListener(String p, RListener spl) {
		final boolean removed = listeners.remove(p,spl);
		if (removed && !hasListeners()){
			stopListening();}
		return removed;
	}


	/**
	 * do the bukkit event for players
	 * @param event
	 */
	@Override
	public void doSpecificPlayerEvent(Event event){
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
		callListeners(event, (Player) entity);
	}

	private void callListeners(Event event, final Player p) {
		TreeSet<RListener> spls = listeners.getSafe(p.getName());
		if (spls == null){
			return;}
		doMethods(event,p, new ArrayList<RListener>(spls));
	}

	private void doMethods(Event event, final Player p, List<RListener> lmethods) {
		/// For each of the splisteners methods that deal with this BukkitEvent
		ArenaPlayer arenaPlayer = null;
		for(RListener lmethod: lmethods){
			final Method method = lmethod.getMethod().getMethod();
			final Class<?>[] types = method.getParameterTypes();
			final Object[] os = new Object[types.length];
			os[0] = event;

			try {
				for (int i=1;i< types.length;i++){
					final Class<?> t = types[i];
					/// Assign the correct values for method parameters
					if (Player.class.isAssignableFrom(t)){
						os[i] = p;
					} else if (Team.class.isAssignableFrom(t)){
						if (arenaPlayer == null){
							arenaPlayer = p != null ? PlayerController.toArenaPlayer(p) : null;}
						if (arenaPlayer != null)
							os[i] = arenaPlayer.getTeam();
					} else if (ArenaPlayer.class.isAssignableFrom(t)){
						if (arenaPlayer == null){
							arenaPlayer = p != null ? PlayerController.toArenaPlayer(p) : null;}
						if (arenaPlayer != null)
							os[i] = arenaPlayer;
					}
				}
				method.invoke(lmethod.getListener(), os); /// Invoke the listening arenalisteners method
			} catch (Exception e){
				Log.err("[BA:Error] method=" + method + ",  types.length=" +types.length +",  p=" + p +",  listener="+lmethod);
				e.printStackTrace();
			}
		}
	}

	private void doEntityDeathEvent(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player && listeners.containsKey(((Player)event.getEntity()).getName()) ){
			callListeners(event, (Player) event.getEntity());
			return;
		}
		ArenaPlayer ap = DmgDeathUtil.getPlayerCause(event.getEntity().getLastDamageCause());
		if (ap == null)
			return;
		if (listeners.containsKey(ap.getName())){
			callListeners(event, ap.getPlayer());
		}
	}

	private void doEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && listeners.containsKey(((Player)event.getEntity()).getName()) ){
			callListeners(event, (Player) event.getEntity());
			return;
		}
		if (event.getDamager() instanceof Player && listeners.containsKey(((Player)event.getDamager()).getName())){
			callListeners(event, (Player) event.getDamager());
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
			callListeners(event, player);
			return;
		}
		/// Else the target wasnt a player, and the shooter wasnt a player.. nothing to do
	}

	private void doEntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player){
			callListeners(event, (Player) event.getEntity());
			return;
		}

		EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
		ArenaPlayer damager = DmgDeathUtil.getPlayerCause(lastDamage);
		if (damager != null){
			callListeners(event, damager.getPlayer());
		}
	}
	public RListener[] getRegisteredListeners() {
		RListener[] handlers;
		while ((handlers = this.handlers) == null) bake(); // This prevents fringe cases of returning null
		return handlers;
	}

	@Override
	public void doMatchEvent(final Event event){
		/// For each ArenaListener class that is listening
		final RListener[] rls = getRegisteredListeners();
		for (RListener rl: rls){
			try {
				rl.getMethod().getMethod().invoke(rl.getListener(), event); /// Invoke the listening arenalisteners method
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private Entity getEntityFromMethod(final Event event, final Method method) {
		try{
			Object o = method.invoke(event);
			if (o instanceof Entity)
				return (Entity) o;
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}