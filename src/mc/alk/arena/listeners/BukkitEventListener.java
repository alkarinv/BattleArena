package mc.alk.arena.listeners;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.MatchEventMethod;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.MapOfHash;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;


/**
 *
 * @author alkarin
 *
 */
public class BukkitEventListener extends BAEventListener{
	public MapOfHash<String,ArenaListener> listeners = new MapOfHash<String,ArenaListener>();
	public HashSet<ArenaListener> mlisteners = new HashSet<ArenaListener>();

	final Method getPlayerMethod;
	public boolean hasListeners(){
		return (!listeners.isEmpty() || !mlisteners.isEmpty());
	}
	public MapOfHash<String,ArenaListener> getListeners(){
		return listeners;
	}
	public HashSet<ArenaListener> getMatchListeners(){
		return mlisteners;
	}

	public Collection<String> getPlayers(){
		return listeners.keySet();
	}
	public BukkitEventListener(final Class<? extends Event> bukkitEvent, Method getPlayerMethod) {
		super(bukkitEvent);
		if (Defaults.DEBUG_EVENTS) System.out.println("Registering GenericPlayerEventListener for type " + bukkitEvent);
		this.getPlayerMethod = getPlayerMethod;
	}

	public void addListener(ArenaListener arenaListener, MatchState matchState, MatchEventMethod mem, Collection<String> players) {
		if (Defaults.DEBUG_EVENTS) System.out.println("    adding listener " + matchState +"   players="+players+"   mem="+mem);
		if (players != null && mem.getPlayerMethod() != null){
			for (String player: players){
				addSPListener(player, arenaListener);}
		} else {
			addMatchListener(arenaListener);
		}
	}

	public void removeListener(ArenaListener arenaListener, MatchState matchState, MatchEventMethod mem, Collection<String> players) {
		if (Defaults.DEBUG_EVENTS) System.out.println("    removing listener " + matchState +"   player="+players+"   mem="+mem);
		if (players != null && mem.getPlayerMethod() != null){
			for (String player: players){
				removeSPListener(player, arenaListener);}
		} else {
			removeMatchListener(arenaListener);
		}
	}

	public boolean removeMatchListener(ArenaListener spl) {
		final boolean removed = mlisteners.remove(spl);
		if (removed && !hasListeners()){
			if (Defaults.DEBUG_EVENTS) System.out.println(" @@@@@@@@@@@@@@@@@@@@@@  STOPPING LISTEN " + bukkitEvent);
			stopListening();}
		return removed;
	}

	public void addMatchListener(ArenaListener spl) {
		if (!hasListeners()){
			if (Defaults.DEBUG_EVENTS) System.out.println(" @@@@@@@@@@@@@@@@@@@@@@  STARTING LISTEN " + bukkitEvent);
			startMatchListening();}
		if (Defaults.DEBUG_EVENTS) System.out.println("   SPLS now listening for match " + spl);
		mlisteners.add(spl);
	}

	public boolean removeSPListener(String p, ArenaListener spl) {
		final boolean removed = listeners.remove(p,spl);
		if (removed && !hasListeners()){
			if (Defaults.DEBUG_EVENTS) System.out.println(" @@@@@@@@@@@@@@@@@@@@@@  STOPPING LISTEN " + bukkitEvent);
			stopListening();}
		return removed;
	}

	public void addSPListener(String p, ArenaListener spl) {
		if (!hasListeners()){
			if (Defaults.DEBUG_EVENTS) System.out.println(" @@@@@@@@@@@@@@@@@@@@@@  STARTING LISTEN " + bukkitEvent);
			startSpecificPlayerListening();}
		if (Defaults.DEBUG_EVENTS) System.out.println("   SPLS now listening for player " + p +"   " + bukkitEvent);
		listeners.add(p,spl);
	}

	@Override
	public void doSpecificPlayerEvent(Event event){
		if (Defaults.DEBUG_EVENTS) System.out.println("Event " +event + "   " + bukkitEvent + "  " + getPlayerMethod.getName());
		/// Need special handling of Methods that have 2 entities involved, as either entity may be in a match
		if (event instanceof EntityDamageByEntityEvent){
			doEntityDamageByEntityEvent((EntityDamageByEntityEvent)event);
			return;
		} else if (event instanceof EntityDamageEvent){
			doEntityDamageEvent((EntityDamageEvent)event);
			return;
		}
		if (event.getClass() != bukkitEvent) /// This can happen with subclasses such as PlayerDeathEvent and EntityDeathEvent
			return;
		final Entity entity = getEntityFromMethod(event, getPlayerMethod);
		if (Defaults.DEBUG_EVENTS) System.out.println("Event " +event + "   " + entity);
		if (!(entity instanceof Player))
			return;
		final Player p = (Player) entity;
		callListeners(event, p);
	}

	private void callListeners(Event event, final Player p) {
		HashSet<ArenaListener> spls = listeners.getSafe(p.getName());
		if (spls == null){
			if (Defaults.DEBUG_EVENTS) System.out.println("   NO SPLS listening for player " + p.getName());
			return;
		}
		/// For each ArenaListener class that is listening
		if (Defaults.DEBUG_EVENTS) System.out.println("   SPLS splisteners .get " + spls);
		for (ArenaListener spl: spls){
			List<MatchEventMethod> methods = MethodController.getMethods(spl,event);
			if (Defaults.DEBUG_EVENTS) System.out.println("    SPL = " + spl.getClass() +"    getting methods "+methods );
			if (methods != null){
				doMethods(event, p, spl, methods);}
			if (event instanceof EntityDamageByEntityEvent){
				methods = MethodController.getMethods(spl,EntityDamageEvent.class);}
			else if (event instanceof EntityDamageByBlockEvent){
				methods = MethodController.getMethods(spl,EntityDamageEvent.class);}
			else
				methods = null;
			if (methods != null){
				doMethods(event, p, spl, methods);}
		}
	}
	private void doMethods(Event event, final Player p, ArenaListener spl, List<MatchEventMethod> methods) {
		/// For each of the splisteners methods that deal with this BukkitEvent
		for(MatchEventMethod method: methods){
			final Class<?>[] types = method.getMethod().getParameterTypes();
			if (Defaults.DEBUG_EVENTS) System.out.println(" method = " + method + "  types.length=" +types.length);
			final Object[] os = new Object[types.length];
			os[0] = event;

			try {
				ArenaPlayer arenaPlayer = p != null ? PlayerController.toArenaPlayer(p) : null;
				for (int i=1;i< types.length;i++){
					final Class<?> t = types[i];
					if (Player.class.isAssignableFrom(t)){
						os[i] = p;
					} else if (Team.class.isAssignableFrom(t)){
						os[i] = TeamController.getTeam(arenaPlayer);
					} else if (ArenaPlayer.class.isAssignableFrom(t)){
						os[i] = arenaPlayer;
					}
				}
				method.getMethod().invoke(spl, os); /// Invoke the listening arenalisteners method
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private void doEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player){
			callListeners(event, (Player) event.getEntity());
			return;
		}
		if (event.getDamager() instanceof Player){
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
		Player damager = null;
		if (lastDamage != null){
			Entity entity = lastDamage.getEntity();
			if (entity instanceof Player){
				damager = (Player) entity;
			} else if (entity instanceof Projectile){
				if (((Projectile)entity).getShooter() instanceof Player)
					damager =(Player) ((Projectile)entity).getShooter();
			}
		}
		if (damager != null){
			callListeners(event, damager);
		}
	}

	@Override
	public void doMatchEvent(Event event){
		if (Defaults.DEBUG_EVENTS) System.out.println("MatchEvent " +event + "   " + bukkitEvent + "  " + getPlayerMethod);

		if (event.getClass() != bukkitEvent) /// This can happen with subclasses such as PlayerDeathEvent and EntityDeathEvent
			return;

		/// For each ArenaListener class that is listening
		if (Defaults.DEBUG_EVENTS) System.out.println("   Match splisteners .get " + mlisteners.size() +"  " + event);
		for (ArenaListener spl: mlisteners){
			List<MatchEventMethod> methods = MethodController.getMethods(spl,event);
			/// For each of the splisteners methods that deal with this BukkitEvent
			for(MatchEventMethod method: methods){
				if (Defaults.DEBUG_EVENTS) System.out.println("    MatchSPL = " + spl.getClass() +"    getting method "+method);
				try {
					method.getMethod().invoke(spl, event); /// Invoke the listening arenalisteners method
				} catch (Exception e){
					e.printStackTrace();
				}
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