package mc.alk.arena.controllers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mc.alk.arena.Defaults;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BukkitEventListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.events.MatchEventMethod;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.Util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;


public class MethodController {

	/** Our listeners*/
	static HashMap<Type, BukkitEventListener> listeners = new HashMap<Type, BukkitEventListener>();

	/** Our registered events and the methods to call when they happen*/
	static HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends Event>,List<MatchEventMethod>>> arenaMethods =
			new HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends Event>,List<MatchEventMethod>>>();

	public MethodController(){}

	public static HashMap<Type, BukkitEventListener> getEventListeners() {
		return listeners;
	}

	public static void updateMatchBukkitEvents(ArenaListener arenaListener, MatchState matchState, List<String> players) {
		try {
			Map<Class<? extends Event>,List<MatchEventMethod>> map = getMethods(arenaListener);
			if (map == null){
				Log.err(arenaListener +" has no registered methods");
				Util.printStackTrace();
				return;
			}
			for (Class<? extends Event> event: map.keySet()){
				updateEventListener(arenaListener,matchState, players, map, event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateAllEventListeners(ArenaListener arenaListener, MatchState matchState, ArenaPlayer player){
		List<String> players = new ArrayList<String>();
		players.add(player.getName());
		updateMatchBukkitEvents(arenaListener,matchState,players);
	}

	public static void updateAllEventListeners(ArenaListener arenaListener, MatchState matchState, Collection<ArenaPlayer> players){
		List<String> strplayers = new ArrayList<String>();
		for (ArenaPlayer ap: players){
			strplayers.add(ap.getName());}
		updateMatchBukkitEvents(arenaListener,matchState,strplayers);
	}

	public static void updateEventListeners(ArenaListener arenaListener, MatchState matchState,
			ArenaPlayer player, Class<? extends Event>... events) {
		try {
			Map<Class<? extends Event>,List<MatchEventMethod>> map = getMethods(arenaListener);
			if (map == null){
				Log.err(arenaListener +" has no registered methods");
				Util.printStackTrace();
				return;
			}
			List<String> players = new ArrayList<String>();
			players.add(player.getName());
			for (Class<? extends Event> event: events){
				updateEventListener(arenaListener,matchState,players, map, event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateEventListener(ArenaListener arenaListener, MatchState matchState, Collection<String> players,
			Map<Class<? extends Event>, List<MatchEventMethod>> map, final Class<? extends Event> event) {
		if (Defaults.DEBUG_EVENTS) System.out.println("updateEventListener "+  event.getSimpleName() +"    " + matchState);
		final List<MatchEventMethod> at = map.get(event);
		if (at == null || at.isEmpty()){
			System.err.println(arenaListener +" has no method that uses the bukkit event +"+event+", but is trying to register it");
			return;
		}
		for (MatchEventMethod mem : at){
			if (Defaults.DEBUG_EVENTS) System.out.println("  updateEventListener "+  event.getSimpleName() +"    " + matchState +":" +
					mem +",al=" + arenaListener +"   " + players);
			if (mem.getBeginState() == matchState){
				BukkitEventListener bel = getCreate(event,mem);
				bel.addListener(arenaListener,matchState, mem,players);
			} else if (mem.getEndState() == matchState) {
				BukkitEventListener bel = listeners.get(event);
				if (bel != null){
					bel.removeListener(arenaListener, matchState,mem,players);
					if (!bel.hasListeners()){
						listeners.remove(event);
					}
				}
			}
		}
	}



	private static BukkitEventListener getCreate(Class<? extends Event> event, MatchEventMethod mem){
		BukkitEventListener gel = listeners.get(event);
		if (Defaults.DEBUG_EVENTS) System.out.println("***************************** checking for " + event);
		if (gel == null){
			if (Defaults.DEBUG_EVENTS) System.out.println("***************************** making new gel for type " + event);
			gel = new BukkitEventListener(event,mem.getPlayerMethod());
			listeners.put(event, gel);
		}
		return gel;
	}

	public static List<MatchEventMethod> getMethods(ArenaListener ael, Event event) {
		return getMethods(ael,event.getClass());
	}

	public static List<MatchEventMethod> getMethods(ArenaListener ael, Class<? extends Event> eventClass) {
		HashMap<Class<? extends Event>,List<MatchEventMethod>> typeMap = arenaMethods.get(ael.getClass());
		if (Defaults.DEBUG_EVENTS) System.out.println("!! getEvent "+ael.getClass()+ " " +eventClass+"  methods="+
				(typeMap==null?"null" :typeMap.size() +":"+ (typeMap.get(eventClass) != null ? typeMap.get(eventClass).size() : 0) ) );
		if (typeMap == null)
			return null;
		return typeMap.get(eventClass);
	}

	public static Map<Class<? extends Event>,List<MatchEventMethod>> getMethods(ArenaListener ael) {
		if (Defaults.DEBUG_EVENTS) System.out.println("!!!! getEvent "+ael.getClass()+" contains=" + arenaMethods.containsKey(ael.getClass()));
		return arenaMethods.get(ael.getClass());
	}

	@SuppressWarnings("unchecked")
	public static void addMethods(Class<? extends ArenaListener> arenaListener, Method[] methodArray){
		HashMap<Class<? extends Event>,List<MatchEventMethod>> typeMap =
				new HashMap<Class<? extends Event>,List<MatchEventMethod>>();

		for (Method method : methodArray){
			MatchEventHandler meh = method.getAnnotation(MatchEventHandler.class);
			if (meh == null)
				continue;
			MatchState beginState = meh.begin(),endState = meh.end();
			boolean needsTeamOrPlayer = false;
			/// Make sure there is some sort of bukkit bukkitEvent here
			Class<?>[] classes = method.getParameterTypes();
			if (classes.length == 0 || !(Event.class.isAssignableFrom(classes[0]))){
				System.err.println("Bukkit Event was null for method " + method);
				continue;
			}
			Class<? extends Event> bukkitEvent = (Class<? extends Event>)classes[0];
			Method getPlayerMethod = null;
			Method getLivingMethod = null;
			Method getEntityMethod = null;

			if (meh.needsPlayer()){
				/// From our bukkit bukkitEvent. find any methods that return a Player, HumanEntity, or LivingEntity
				for (Method m : bukkitEvent.getMethods()){
					Type t = m.getReturnType();
					if (Player.class.isAssignableFrom((Class<?>) t) || HumanEntity.class.isAssignableFrom((Class<?>) t)){
						if (getPlayerMethod != null){
							System.err.println("Method "+method.getName() +" has multiple methods that return a player ");
							return;
						}
						getPlayerMethod = m;
					} else if (LivingEntity.class.isAssignableFrom((Class<?>) t) || Entity.class.isAssignableFrom((Class<?>) t)){
						getLivingMethod = m;
					}
				}
			}

			/// Go over the rest of the parameters to see if we should give a Team or Player
			for (int i =1;i< classes.length;i++){
				Class<?> c = classes[i];
				needsTeamOrPlayer = Player.class.isAssignableFrom(c) || Team.class.isAssignableFrom(c);
				if (!needsTeamOrPlayer){
					continue;
				}
				/// TODO Check for conflicting scope
				boolean noEntityMethod = getEntityMethod == null && getLivingMethod==null && getPlayerMethod==null;
				if (noEntityMethod){
					System.err.println("[BattleArena] "+arenaListener+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+"returns no player, and no entities. Class="+arenaListener);
					return;
				} else if (getLivingMethod != null && !meh.suppressCastWarnings()){
					Log.warn("[BattleArena] Warning. "+arenaListener+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+" returns only a living entity. Cast to Player will be attempted at runtime");
				} else if (getEntityMethod != null && !meh.suppressCastWarnings()){
					Log.warn("[BattleArena] Warning. "+arenaListener+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+" returns only an Entity. Cast to Player will be attempted at runtime");
				}
			}

			if (getPlayerMethod == null) getPlayerMethod = getLivingMethod; /// if playermethod is null maybe we have a living
			if (getPlayerMethod == null) getPlayerMethod = getEntityMethod;/// ok.. maybe at least an entity?

			List<MatchEventMethod> mths = typeMap.get(bukkitEvent);
			if (mths == null){
//				System.out.println("bukkitEvent !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + bukkitEvent + "  " + getPlayerMethod);
				mths = new ArrayList<MatchEventMethod>();
				typeMap.put(bukkitEvent, mths);
			}


			if (getPlayerMethod != null){
				if (beginState == MatchState.NONE) beginState = MatchState.ONENTER;
				if (endState == MatchState.NONE) endState = MatchState.ONLEAVE;

				mths.add(new MatchEventMethod(method, bukkitEvent,getPlayerMethod,beginState, endState, meh.priority()));
			} else {
				if (beginState == MatchState.NONE) beginState = MatchState.ONOPEN;
				if (endState == MatchState.NONE) endState = MatchState.ONFINISH;
				mths.add(new MatchEventMethod(method, bukkitEvent,beginState,endState,meh.priority()));
			}
			Collections.sort(mths);
		}
		arenaMethods.put(arenaListener, typeMap);
	}

}
