package mc.alk.arena.controllers;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mc.alk.arena.Defaults;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.listeners.custom.BukkitEventHandler;
import mc.alk.arena.listeners.custom.RListener;
import mc.alk.arena.listeners.custom.RListener.RListenerPriorityComparator;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.ArenaEventMethod;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;


@SuppressWarnings("deprecation")
public class MethodController {

	/** Our Dynamic listeners, listening for bukkit events*/
	static EnumMap<EventPriority, HashMap<Type, BukkitEventHandler>> bukkitListeners =
			new EnumMap<EventPriority,HashMap<Type, BukkitEventHandler>>(EventPriority.class);

	/** Our registered bukkit events and the methods to call when they happen*/
	static HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends Event>,List<ArenaEventMethod>>> bukkitEventMethods =
			new HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends Event>,List<ArenaEventMethod>>>();

	/** Our registered bukkit events and the methods to call when they happen*/
	static HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends BAEvent>,List<ArenaEventMethod>>> matchEventMethods =
			new HashMap<Class<? extends ArenaListener>,HashMap<Class<? extends BAEvent>,List<ArenaEventMethod>>>();

	HashMap<Class<? extends Event>,List<RListener>> bukkitMethods = new HashMap<Class<? extends Event>,List<RListener>>();

	HashMap<Class<? extends BAEvent>,List<RListener>> matchMethods = new HashMap<Class<? extends BAEvent>,List<RListener>>();

	public MethodController(){}

	public static EnumMap<EventPriority, HashMap<Type, BukkitEventHandler>> getEventListeners() {
		return bukkitListeners;
	}

	public void updateEvents(MatchState matchState, ArenaPlayer player){
		List<String> players = new ArrayList<String>();
		players.add(player.getName());
		updateEvents(matchState,players);
	}

	public void updateEvents(MatchState matchState, Collection<ArenaPlayer> players){
		List<String> strplayers = new ArrayList<String>();
		for (ArenaPlayer ap: players){
			strplayers.add(ap.getName());}
		updateEvents(matchState,strplayers);
	}

	public void updateEvents(MatchState matchState, List<String> players) {
		try {
			Collection<Class<? extends Event>> keys = bukkitMethods.keySet();
			if (keys==null)
				return;
			for (Class<? extends Event> event: keys){
				updateEvent(matchState, players, event);}
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
	public void updateSpecificEvents(MatchState matchState, ArenaPlayer player, Class<? extends Event>... events) {
		try {
			List<String> players = new ArrayList<String>();
			players.add(player.getName());

			for (Class<? extends Event> event: events){
				updateEvent(matchState,players, event);}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateEvent(MatchState matchState, Collection<String> players, final Class<? extends Event> event) {
		final List<RListener> rls = bukkitMethods.get(event);
		if (rls == null || rls.isEmpty()){
			return;}
		if (Defaults.DEBUG_EVENTS) System.out.println("updateEventListener "+  event.getSimpleName() +"    " + matchState);

		for (RListener rl : rls){
			ArenaEventMethod mem = rl.getMethod();
			if (Defaults.DEBUG_EVENTS) System.out.println("  updateEventListener "+  event.getSimpleName() +"    " + matchState +":" +
					rl +",   " + players);
			if (mem.getBeginState() == matchState){
				BukkitEventHandler bel = getCreate(event,mem);
				bel.addListener(rl,players);
			} else if (mem.getEndState() == matchState || mem.getCancelState() == matchState) {
				for (HashMap<Type,BukkitEventHandler> ls : bukkitListeners.values()){
					BukkitEventHandler bel = ls.get(event);
					if (bel != null){
						bel.removeListener(rl, players);
					}
				}
			}
		}
	}

	private static BukkitEventHandler getCreate(Class<? extends Event> event, ArenaEventMethod mem){
		HashMap<Type,BukkitEventHandler> gels = bukkitListeners.get(mem.getBukkitPriority());
		if (gels == null){
			gels = new HashMap<Type,BukkitEventHandler>();
			bukkitListeners.put(mem.getBukkitPriority(), gels);
		}
		BukkitEventHandler gel = gels.get(event);
		if (Defaults.DEBUG_EVENTS) System.out.println("***************************** checking for " + event);

		if (gel == null){
			if (Defaults.DEBUG_EVENTS) System.out.println("***************************** making new gel for type " + event);
			gel = new BukkitEventHandler(event,mem.getBukkitPriority(), mem.getPlayerMethod());
			gels.put(event, gel);
		}
		return gel;
	}

	public static List<ArenaEventMethod> getMethods(ArenaListener ael, Event event) {
		return getMethods(ael,event.getClass());
	}

	private static List<ArenaEventMethod> getMethods(ArenaListener ael, Class<? extends Event> eventClass) {
		HashMap<Class<? extends Event>,List<ArenaEventMethod>> typeMap = bukkitEventMethods.get(ael.getClass());
		if (Defaults.DEBUG_EVENTS) System.out.println("!! getEvent "+ael.getClass()+ " " +eventClass+"  methods="+
				(typeMap==null?"null" :typeMap.size() +":"+ (typeMap.get(eventClass) != null ? typeMap.get(eventClass).size() : 0) ) );
		if (typeMap == null)
			return null;
		return typeMap.get(eventClass);
	}

	private static Map<Class<? extends Event>,List<ArenaEventMethod>> getBukkitMethods(ArenaListener ael) {
		if (Defaults.DEBUG_EVENTS) System.out.println("!!!! getEvent "+ael.getClass().getSimpleName()+" contains=" + bukkitEventMethods.containsKey(ael.getClass()));
		return bukkitEventMethods.get(ael.getClass());
	}

	private static Map<Class<? extends BAEvent>,List<ArenaEventMethod>> getMatchMethods(ArenaListener ael) {
		if (Defaults.DEBUG_EVENTS) System.out.println("!!!! getEvent "+ael.getClass().getSimpleName()+" contains=" + bukkitEventMethods.containsKey(ael.getClass()));
		return matchEventMethods.get(ael.getClass());
	}


	@SuppressWarnings({ "unchecked" })
	private static void addMethods(Class<? extends ArenaListener> alClass){
		HashMap<Class<? extends Event>,List<ArenaEventMethod>> bukkitTypeMap =
				new HashMap<Class<? extends Event>,List<ArenaEventMethod>>();
		HashMap<Class<? extends BAEvent>, List<ArenaEventMethod>> matchTypeMap =
				new HashMap<Class<? extends BAEvent>,List<ArenaEventMethod>>();

		Method[] methodArray = alClass.getMethods();

		for (Method method : methodArray){
			MatchState beginState,endState, cancelState;
			boolean needsPlayer;
			final String entityMethod;
			boolean supressCastWarnings;
			mc.alk.arena.objects.events.EventPriority priority;
			org.bukkit.event.EventPriority bukkitPriority;

			ArenaEventHandler aeh = method.getAnnotation(ArenaEventHandler.class);
			if (aeh == null){
				/// Support for the old style MatchEventHandler
				MatchEventHandler meh = method.getAnnotation(MatchEventHandler.class);
				if (meh == null){
					continue;}
				beginState = meh.begin();
				endState = meh.end();
				cancelState=MatchState.NONE;
				needsPlayer = meh.needsPlayer();
				entityMethod = meh.entityMethod();
				supressCastWarnings = meh.suppressCastWarnings();
				bukkitPriority = meh.bukkitPriority();
				priority = meh.priority();
			} else {
				beginState = aeh.begin();
				endState = aeh.end();
				cancelState=MatchState.NONE;
				needsPlayer = aeh.needsPlayer();
				entityMethod = aeh.entityMethod();
				supressCastWarnings = aeh.suppressCastWarnings();
				bukkitPriority = aeh.bukkitPriority();
				priority = aeh.priority();
			}
			/// Make sure there is some sort of bukkit bukkitEvent here
			Class<?>[] classes = method.getParameterTypes();
			if (classes.length == 0 || !(Event.class.isAssignableFrom(classes[0]))){
				System.err.println("Bukkit Event was null for method " + method);
				continue;
			}

			Class<? extends Event> bukkitEvent = (Class<? extends Event>)classes[0];
			boolean baEvent = BAEvent.class.isAssignableFrom(bukkitEvent);
			//			MatchState beginState = meh.begin(),endState = meh.end(), cancelState=MatchState.NONE;
			boolean needsTeamOrPlayer = false;
			Method getPlayerMethod = null;
			Method getLivingMethod = null;
			Method getEntityMethod = null;

			if (needsPlayer){
				List<Method> playerMethods = new ArrayList<Method>();
				List<Method> entityMethods = new ArrayList<Method>();

				/// From our bukkit bukkitEvent. find any methods that return a Player, HumanEntity, or LivingEntity
				for (Method m : bukkitEvent.getMethods()){
					/// Check first for a specified method
					if (!entityMethod.isEmpty() && m.getName().equals(entityMethod)){
						getPlayerMethod = m;
						break;
					}
					Type t = m.getReturnType();
					if (Player.class.isAssignableFrom((Class<?>) t) || HumanEntity.class.isAssignableFrom((Class<?>) t)){
						playerMethods.add(m);
						getPlayerMethod = m;
					} else if (Entity.class.isAssignableFrom((Class<?>) t)){
						entityMethods.add(m);
						getLivingMethod = m;
					}
				}
				/// If we haven't already found the specified player method.. try and get it from our lists
				if (getPlayerMethod == null){
					if (!playerMethods.isEmpty()){
						if (playerMethods.size() > 1){
							System.err.println(alClass+". Method "+method.getName() +" has multiple methods that return a player");
							System.err.println(alClass+". Use @MatchEventHandler(entityMethod=\"methodWhichYouWantToUse\")");
							return;
						}
						getPlayerMethod = playerMethods.get(0);
					} else if (!entityMethods.isEmpty()){
						if (bukkitEvent == EntityDeathEvent.class){
							try {
								getEntityMethod = EntityDeathEvent.class.getMethod("getEntity", new Class<?>[]{});
							} catch (Exception e) {
								e.printStackTrace();
								continue;
							}
						} else if (entityMethods.size() > 1 && !EntityDamageEvent.class.isAssignableFrom(bukkitEvent)){
							System.err.println(alClass+". Method "+method.getName() +" has multiple methods that return an entity");
							System.err.println(alClass+". Use @MatchEventHandler(entityMethod=\"methodWhichYouWantToUse\")");
							return;
						} else {
							getEntityMethod = entityMethods.get(0);
						}
					}
				}

			}

			/// Go over the rest of the parameters to see if we should give a Team or Player
			for (int i =1;i< classes.length;i++){
				Class<?> c = classes[i];
				needsTeamOrPlayer = Player.class.isAssignableFrom(c) || ArenaTeam.class.isAssignableFrom(c);
				if (!needsTeamOrPlayer){
					continue;
				}

				boolean noEntityMethod = getEntityMethod == null && getLivingMethod==null && getPlayerMethod==null;
				if (noEntityMethod){
					System.err.println("[BattleArena] "+alClass+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+"returns no player, and no entities. Class="+alClass);
					return;
				} else if (getLivingMethod != null && !supressCastWarnings){
					Log.warn("[BattleArena] Warning. "+alClass+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+" returns only a living entity. Cast to Player will be attempted at runtime");
				} else if (getEntityMethod != null && !supressCastWarnings){
					Log.warn("[BattleArena] Warning. "+alClass+". Method "+method.getName() +" needs a player or team, but the bukkitEvent "+
							bukkitEvent.getCanonicalName()+" returns only an Entity. Cast to Player will be attempted at runtime");
				}
			}

			if (getPlayerMethod == null) getPlayerMethod = getLivingMethod; /// if playermethod is null maybe we have a living
			if (getPlayerMethod == null) getPlayerMethod = getEntityMethod;/// ok.. maybe at least an entity?

			List<ArenaEventMethod> mths = baEvent ? matchTypeMap.get(bukkitEvent) : bukkitTypeMap.get(bukkitEvent);
			if (mths == null){
				//				System.out.println("bukkitEvent !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + bukkitEvent + "  " + getPlayerMethod);
				mths = new ArrayList<ArenaEventMethod>();
				if (baEvent){
					matchTypeMap.put((Class<? extends BAEvent>) bukkitEvent, mths);
				} else {
					bukkitTypeMap.put(bukkitEvent, mths);
				}
			}

			if (getPlayerMethod != null){
				if (beginState == MatchState.NONE) beginState = MatchState.ONENTER;
				if (endState == MatchState.NONE) endState = MatchState.ONLEAVE;
				if (cancelState == MatchState.NONE) cancelState = MatchState.ONCANCEL;
				mths.add(new ArenaEventMethod(method, bukkitEvent,getPlayerMethod,
						beginState, endState,cancelState, priority, bukkitPriority ));
			} else {
				if (beginState == MatchState.NONE) beginState = MatchState.ONOPEN;
				if (endState == MatchState.NONE) endState = MatchState.ONCOMPLETE;
				if (cancelState == MatchState.NONE) cancelState = MatchState.ONCANCEL;
				mths.add(new ArenaEventMethod(method, bukkitEvent,beginState,
						endState,cancelState, priority, bukkitPriority));
			}
			Collections.sort(mths, new Comparator<ArenaEventMethod>(){
				@Override
				public int compare(ArenaEventMethod o1, ArenaEventMethod o2) {
					return o1.getPriority().compareTo(o2.getPriority());
				}
			});
		}
		bukkitEventMethods.put(alClass, bukkitTypeMap);
		matchEventMethods.put(alClass, matchTypeMap);
	}

	private boolean removeListener(ArenaListener listener, HashMap<?,List<RListener>> methods){
		synchronized(bukkitMethods){
			for (List<RListener> rls: bukkitMethods.values()){
				Iterator<RListener> iter = rls.iterator();
				while(iter.hasNext()){
					RListener rl = iter.next();
					if (rl.getListener() == listener){
						iter.remove();
						for (HashMap<Type,BukkitEventHandler> ls : bukkitListeners.values()){
							BukkitEventHandler bel = ls.get(rl.getMethod().getBukkitEvent());
							if (bel != null){
								bel.removeAllListener(rl);
							}
						}
					}
				}
			}
			return true;
		}
	}

	public boolean removeListener(ArenaListener listener) {
		removeListener(listener, bukkitMethods);
		removeListener(listener, matchMethods);
		return true;
	}


	public void addListener(ArenaListener listener) {
		addAllEvents(listener);
	}

	/**
	 * Add all of the bukkit events found in the listener
	 * @param listener
	 */
	public void addAllEvents(ArenaListener listener) {
		if (!bukkitEventMethods.containsKey(listener.getClass()) && !matchEventMethods.containsKey(listener.getClass())){
			MethodController.addMethods(listener.getClass());
		}
		Map<Class<? extends Event>,List<ArenaEventMethod>> map = getBukkitMethods(listener);
		if (map != null){
			for (Class<? extends Event> clazz : map.keySet()){
				addEventMethod(listener, map, clazz);
			}
		}
		Map<Class<? extends BAEvent>,List<ArenaEventMethod>> map2 = getMatchMethods(listener);
		if (map2 != null){
			for (Class<? extends BAEvent> clazz : map2.keySet()){
				addBAEventMethod(listener, map2, clazz);
			}
		}
	}

	/**
	 * Add a subset of the events found in the listener to the MethodController
	 * @param listener
	 * @param events
	 */
	public void addSpecificEvents(ArenaListener listener, List<Class<? extends Event>> events) {
		if (!bukkitEventMethods.containsKey(listener.getClass()) && !matchEventMethods.containsKey(listener.getClass())){
			MethodController.addMethods(listener.getClass());
		}
		Map<Class<? extends Event>,List<ArenaEventMethod>> map = getBukkitMethods(listener);
		for (Class<? extends Event> clazz : events){
			addEventMethod(listener, map, clazz);
		}
		Map<Class<? extends BAEvent>,List<ArenaEventMethod>> map2 = getMatchMethods(listener);
		for (Class<? extends BAEvent> clazz : map2.keySet()){
			addBAEventMethod(listener, map2, clazz);
		}
	}


	private void addEventMethod(ArenaListener listener, Map<Class<? extends Event>, List<ArenaEventMethod>> map,
			Class<? extends Event> clazz) {
		List<ArenaEventMethod> list = map.get(clazz);
		if (list == null || list.isEmpty())
			return;

		List<RListener> rls = bukkitMethods.get(clazz);
		if (rls == null){
			rls = new ArrayList<RListener>();
			bukkitMethods.put(clazz, rls);
		}
		for (ArenaEventMethod mem: list){
			rls.add(new RListener(listener, mem));
		}
		Collections.sort(rls, new RListenerPriorityComparator());
	}

	private void addBAEventMethod(ArenaListener listener, Map<Class<? extends BAEvent>, List<ArenaEventMethod>> map,
			Class<? extends BAEvent> clazz) {
		List<ArenaEventMethod> list = map.get(clazz);
		if (list == null || list.isEmpty())
			return;

		List<RListener> rls = matchMethods.get(clazz);
		if (rls == null){
			rls = new ArrayList<RListener>();
			matchMethods.put(clazz, rls);
		}
		for (ArenaEventMethod mem: list){
			rls.add(new RListener(listener, mem));
		}
		Collections.sort(rls, new RListenerPriorityComparator());
	}

	public void deconstruct() {
		bukkitMethods.clear();
	}

	public void callEvent(BAEvent event) {
		callEvent(event.getClass(), event);
	}

	@SuppressWarnings("unchecked")
	private void callEvent(Class<? extends BAEvent> clazz, BAEvent event) {
		List<RListener> rls = matchMethods.get(clazz);
		if (rls != null){
			for (RListener rl : rls){
				try {
					rl.getMethod().getMethod().invoke(rl.getListener(), event); /// Invoke the listening arenalisteners method
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != BAEvent.class && BAEvent.class.isAssignableFrom(superClass)){
			callEvent((Class<? extends BAEvent>) superClass,event);
		}
	}

}
