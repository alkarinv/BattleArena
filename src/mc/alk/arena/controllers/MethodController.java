package mc.alk.arena.controllers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mc.alk.arena.Defaults;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BukkitEventListener;
import mc.alk.arena.listeners.RListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.events.MatchEventMethod;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.Log;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;


public class MethodController {

	/** Our Dynamic listeners, listening for bukkit events*/
	static HashMap<Type, BukkitEventListener> bukkitListeners = new HashMap<Type, BukkitEventListener>();

	/** Our registered bukkit events and the methods to call when they happen*/
	static HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends Event>,List<MatchEventMethod>>> bukkitEventMethods =
			new HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends Event>,List<MatchEventMethod>>>();

	/** Our registered arena events and the methods to call when they happen*/
	static HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends BAEvent>,List<MatchEventMethod>>> arenaEventMethods =
			new HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends BAEvent>,List<MatchEventMethod>>>();

	HashMap<Class<? extends Event>,List<RListener>> bukkitMethods = new HashMap<Class<? extends Event>,List<RListener>>();

	HashMap<Class<? extends BAEvent>,List<RListener>> arenaMethods = new HashMap<Class<? extends BAEvent>,List<RListener>>();

	public MethodController(){}

	public static HashMap<Type, BukkitEventListener> getEventListeners() {
		return bukkitListeners;
	}

	public void updateMatchBukkitEvents(MatchState matchState, List<String> players) {
		try {
//			Map<Class<? extends Event>,List<MatchEventMethod>> map = getBukkitMethods(arenaListener);
			Collection<Class<? extends Event>> keys = bukkitMethods.keySet();
			if (keys==null)
				return;
//			if (map == null){
//				return;}
			for (Class<? extends Event> event: keys){
				//				RListener rl = getRListener(arenaListener,event);
				updateEventListener(matchState, players, event);}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateAllEventListeners(MatchState matchState, ArenaPlayer player){
		List<String> players = new ArrayList<String>();
		players.add(player.getName());
		updateMatchBukkitEvents(matchState,players);
	}

	public void updateAllEventListeners(MatchState matchState, Collection<ArenaPlayer> players){
		List<String> strplayers = new ArrayList<String>();
		for (ArenaPlayer ap: players){
			strplayers.add(ap.getName());}
		updateMatchBukkitEvents(matchState,strplayers);
	}

	/**
	 *
	 * @param matchState
	 * @param player
	 * @param events
	 */
	public void updateEventListeners(MatchState matchState, ArenaPlayer player, Class<? extends Event>... events) {
		try {
			List<String> players = new ArrayList<String>();
			players.add(player.getName());

			for (Class<? extends Event> event: events){
				updateEventListener(matchState,players, event);}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 *
	 * @param matchState
	 * @param player
	 * @param events
	 */
	public void updateEventListeners(MatchState matchState, ArenaPlayer player, List<Class<? extends Event>> events) {
		try {
			List<String> players = new ArrayList<String>();
			players.add(player.getName());

			for (Class<? extends Event> event: events){
				updateEventListener(matchState,players, event);}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateEventListener(MatchState matchState, Collection<String> players, final Class<? extends Event> event) {
		final List<RListener> at = bukkitMethods.get(event);
		if (at == null || at.isEmpty()){
			//			System.err.println(arenaListener +" has no method that uses the bukkit event +"+event+", but is trying to register it");
			return;
		}
		if (Defaults.DEBUG_EVENTS) System.out.println("updateEventListener "+  event.getSimpleName() +"    " + matchState);

		for (RListener rl : at){
			MatchEventMethod mem = rl.getMethod();
			//			MatchEventMethod mem = arenaListener.getPlayerMethod();
			if (Defaults.DEBUG_EVENTS) System.out.println("  updateEventListener "+  event.getSimpleName() +"    " + matchState +":" +
					mem +",   " + players);
			if (mem.getBeginState() == matchState){
				BukkitEventListener bel = getCreate(event,mem);
				bel.addListener(rl,players);
			} else if (mem.getEndState() == matchState || mem.getCancelState() == matchState) {
				BukkitEventListener bel = bukkitListeners.get(event);
				if (bel != null){
					bel.removeListener(rl, players);
					if (!bel.hasListeners()){
						bukkitListeners.remove(event);
					}
				}
			}
		}
	}

	private static BukkitEventListener getCreate(Class<? extends Event> event, MatchEventMethod mem){
		BukkitEventListener gel = bukkitListeners.get(event);
		if (Defaults.DEBUG_EVENTS) System.out.println("***************************** checking for " + event);
		if (gel == null){
			if (Defaults.DEBUG_EVENTS) System.out.println("***************************** making new gel for type " + event);
			gel = new BukkitEventListener(event,mem.getPlayerMethod());
			bukkitListeners.put(event, gel);
		}
		return gel;
	}

	public static List<MatchEventMethod> getMethods(ArenaListener ael, Event event) {
		return getMethods(ael,event.getClass());
	}

	public static List<MatchEventMethod> getMethods(ArenaListener ael, Class<? extends Event> eventClass) {
		HashMap<Class<? extends Event>,List<MatchEventMethod>> typeMap = bukkitEventMethods.get(ael.getClass());
		if (Defaults.DEBUG_EVENTS) System.out.println("!! getEvent "+ael.getClass()+ " " +eventClass+"  methods="+
				(typeMap==null?"null" :typeMap.size() +":"+ (typeMap.get(eventClass) != null ? typeMap.get(eventClass).size() : 0) ) );
		if (typeMap == null)
			return null;
		return typeMap.get(eventClass);
	}

	public static Map<Class<? extends Event>,List<MatchEventMethod>> getBukkitMethods(ArenaListener ael) {
		if (Defaults.DEBUG_EVENTS) System.out.println("!!!! getEvent "+ael.getClass().getSimpleName()+" contains=" + bukkitEventMethods.containsKey(ael.getClass()));
		return bukkitEventMethods.get(ael.getClass());
	}

	public static Map<Class<? extends BAEvent>,List<MatchEventMethod>> getArenaMethods(ArenaListener ael) {
		return getArenaMethods(ael.getClass());
	}
	public static Map<Class<? extends BAEvent>,List<MatchEventMethod>> getArenaMethods(Class<? extends ArenaListener> alClass) {
		if (Defaults.DEBUG_EVENTS) System.out.println("!!!! getEvent "+alClass.getSimpleName()+" contains=" + arenaEventMethods.containsKey(alClass));
		return arenaEventMethods.get(alClass);
	}

	@SuppressWarnings("unchecked")
	public static void addBukkitMethods(Class<? extends ArenaListener> alClass){
		HashMap<Class<? extends Event>,List<MatchEventMethod>> typeMap =
				new HashMap<Class<? extends Event>,List<MatchEventMethod>>();
		Method[] methodArray = alClass.getMethods();
		for (Method method : methodArray){
			MatchEventHandler meh = method.getAnnotation(MatchEventHandler.class);
			if (meh == null)
				continue;
			/// Make sure there is some sort of bukkit bukkitEvent here
			Class<?>[] classes = method.getParameterTypes();
			if (classes.length == 0 || !(Event.class.isAssignableFrom(classes[0]))){
				System.err.println("Bukkit Event was null for method " + method);
				continue;
			}
			Class<? extends Event> bukkitEvent = (Class<? extends Event>)classes[0];
			/// MethodController only deals with Straight up BukkitEvents, not BAEvents which should be handled from the Match
			if (BAEvent.class.isAssignableFrom(bukkitEvent)){
				continue;
			}
			MatchState beginState = meh.begin(),endState = meh.end(), cancelState=MatchState.NONE;
			boolean needsTeamOrPlayer = false;
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
					System.err.println("[BattleArena] "+alClass+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+"returns no player, and no entities. Class="+alClass);
					return;
				} else if (getLivingMethod != null && !meh.suppressCastWarnings()){
					Log.warn("[BattleArena] Warning. "+alClass+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+" returns only a living entity. Cast to Player will be attempted at runtime");
				} else if (getEntityMethod != null && !meh.suppressCastWarnings()){
					Log.warn("[BattleArena] Warning. "+alClass+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
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
				if (cancelState == MatchState.NONE) cancelState = MatchState.ONCANCEL;
				mths.add(new MatchEventMethod(method, bukkitEvent,getPlayerMethod,beginState, endState,cancelState, meh.priority()));
			} else {
				if (beginState == MatchState.NONE) beginState = MatchState.ONOPEN;
				if (endState == MatchState.NONE) endState = MatchState.ONFINISH;
				if (cancelState == MatchState.NONE) cancelState = MatchState.ONCANCEL;
				mths.add(new MatchEventMethod(method, bukkitEvent,beginState, endState,cancelState, meh.priority()));
			}
			Collections.sort(mths);
		}
		bukkitEventMethods.put(alClass, typeMap);
	}


	@SuppressWarnings("unchecked")
	public static void addMatchListener(Class <? extends ArenaListener> alClass){
		HashMap<Class<? extends BAEvent>,List<MatchEventMethod>> typeMap =
				new HashMap<Class<? extends BAEvent>,List<MatchEventMethod>>();
		Method[] methodArray = alClass.getMethods();
		for (Method method : methodArray){
			MatchEventHandler teh = method.getAnnotation(MatchEventHandler.class);
			if (teh == null)
				continue;
			/// Make sure there is some sort of BAEvent here
			Class<?>[] classes = method.getParameterTypes();
			/// We only want BAEvents, not just plain BukkitEvents
			if (classes.length == 0 || !(BAEvent.class.isAssignableFrom(classes[0]))){
				continue;}
			Class<? extends BAEvent> baEvent = (Class<? extends BAEvent>)classes[0];

			List<MatchEventMethod> mths = typeMap.get(baEvent);
			if (mths == null){
				mths = new ArrayList<MatchEventMethod>();
				typeMap.put(baEvent, mths);
			}

			mths.add(new MatchEventMethod(method, baEvent,MatchState.NONE,MatchState.NONE,MatchState.NONE,teh.priority()));
			Collections.sort(mths);
		}
		arenaEventMethods.put(alClass, typeMap);
	}

	public boolean removeListener(ArenaListener listener) {
		synchronized(arenaMethods){
			for (List<RListener> rls : arenaMethods.values()){
				Iterator<RListener> iter = rls.iterator();
				while(iter.hasNext()){
					RListener rl = iter.next();
					if (rl.getListener() == listener){
						iter.remove();}
				}
			}
		}
		synchronized(bukkitMethods){
			for (List<RListener> rls: bukkitMethods.values()){
				Iterator<RListener> iter = rls.iterator();
				while(iter.hasNext()){
					RListener rl = iter.next();
					if (rl.getListener() == listener){
						iter.remove();
						BukkitEventListener bel = bukkitListeners.get(rl.getMethod().getBukkitEvent());
						if (bel != null){
							bel.removeAllListener(rl);
							if (!bel.hasListeners()){
								bukkitListeners.remove(rl.getMethod().getBukkitEvent());
							}
						}

					}
				}
			}
		}
		return true;
	}

	public void addListener(ArenaListener listener) {
		addBukkitMethods(listener);
		addMatchMethods(listener);
	}

	/**
	 * Add all of the bukkit events found in the listener
	 * @param listener
	 */
	public void addBukkitMethods(ArenaListener listener) {
		if (!bukkitEventMethods.containsKey(listener.getClass())){
			MethodController.addBukkitMethods(listener.getClass());
		}
		Map<Class<? extends Event>,List<MatchEventMethod>> map = getBukkitMethods(listener);
		for (Class<? extends Event> clazz : map.keySet()){
			addBukkitMethod(listener, map, clazz);

		}
	}

	/**
	 * Add a subset of the events found in the listener to the MethodController
	 * @param listener
	 * @param events
	 */
	public void addBukkitMethods(ArenaListener listener, List<Class<? extends Event>> events) {
		if (!bukkitEventMethods.containsKey(listener.getClass())){
			MethodController.addBukkitMethods(listener.getClass());
		}
		Map<Class<? extends Event>,List<MatchEventMethod>> map = getBukkitMethods(listener);
		for (Class<? extends Event> clazz : events){
			addBukkitMethod(listener, map, clazz);
		}
	}


	private void addBukkitMethod(ArenaListener listener, Map<Class<? extends Event>, List<MatchEventMethod>> map,
			Class<? extends Event> clazz) {
		List<MatchEventMethod> list = map.get(clazz);
		if (list == null || list.isEmpty())
			return;
		List<RListener> rls = new ArrayList<RListener>();
		for (MatchEventMethod mem: list){
			rls.add(new RListener(listener, mem));
		}
		bukkitMethods.put(clazz, rls);
	}

	public void addMatchMethods(ArenaListener listener) {
		/// If we have never seen this listener before add all the methods to our static list
		if (!arenaEventMethods.containsKey(listener.getClass())){
			MethodController.addMatchListener(listener.getClass());
		}

		Map<Class<? extends BAEvent>,List<MatchEventMethod>> map = getArenaMethods(listener);
		for (Class<? extends BAEvent> clazz : map.keySet()){
			List<MatchEventMethod> list = map.get(clazz);
			if (list == null || list.isEmpty())
				continue;
			List<RListener> rls = new ArrayList<RListener>();
			for (MatchEventMethod mem: list){
				rls.add(new RListener(listener, mem));
			}
			arenaMethods.put(clazz, rls);
		}
	}

	public void callListeners(BAEvent event){
		List<RListener> mtls = arenaMethods.get(event.getClass());
		if (mtls == null){
			return;}
		for (RListener tl: mtls){
			try {
				tl.getMethod().getMethod().invoke(tl.getListener(), event); /// Invoke the listening transitionlisteners method
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void deconstruct() {
		arenaMethods.clear();
		bukkitMethods.clear();
	}

}
