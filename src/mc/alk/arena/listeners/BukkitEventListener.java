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
import mc.alk.arena.objects.MatchEventMethod;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.MapOfHash;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;


/**
 * 
 * @author alkarin
 *
 */
public class BukkitEventListener extends BEventListener{
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
//		System.out.println("Event " +event + "   " + bukkitEvent + "  " + getPlayerMethod);
		if (event.getClass() != bukkitEvent) /// This can happen with subclasses such as PlayerDeathEvent and EntityDeathEvent
			return;
		final Entity entity = getEntityFromMethod(event, getPlayerMethod);
		if (Defaults.DEBUG_EVENTS) System.out.println("Event " +event + "   " + entity);
		if (!(entity instanceof Player))
			return;
		final Player p = (Player) entity;
		HashSet<ArenaListener> spls = listeners.getSafe(p.getName());
		if (spls == null){
//			if (Defaults.DEBUG_EVENTS) System.out.println("   NO SPLS listening for player " + p.getName());			
			return;
		}
		/// For each ArenaListener class that is listening
		if (Defaults.DEBUG_EVENTS) System.out.println("   SPLS splisteners .get " + spls);
		for (ArenaListener spl: spls){
			List<MatchEventMethod> methods = MethodController.getMethods(spl,event);
//			if (Defaults.DEBUG_EVENTS) System.out.println("    SPL = " + spl.getClass() +"    getting methods "+methods);
			/// For each of the splisteners methods that deal with this BukkitEvent
			for(MatchEventMethod method: methods){
				final Class<?>[] types = method.getMethod().getParameterTypes();
				if (Defaults.DEBUG_EVENTS) System.out.println(" method = " + method +"   method="+method +"  " + types.length);
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
	}

	@Override
	public void doMatchEvent(Event event){
		System.out.println("MatchEvent " +event + "   " + bukkitEvent + "  " + getPlayerMethod);

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
//			 System.out.println("event " + event +"   method = " + method);
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
