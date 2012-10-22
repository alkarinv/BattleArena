package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.FormingTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.objects.teams.TeamHandler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public enum TeamController  implements Listener {
	INSTANCE;

	static final boolean DEBUG = false;
	static Map<Team,CopyOnWriteArrayList<TeamHandler>> handlers = new ConcurrentHashMap<Team,CopyOnWriteArrayList<TeamHandler>>();
	HashSet<Team> selfFormedTeams = new HashSet<Team>();
	HashSet<FormingTeam> formingTeams = new HashSet<FormingTeam>();
	BattleArenaController bac;
	static TeamController teamController = null;

	private TeamController(){
		this.bac = BattleArena.getBAC();
	}

	public static Team getTeam(ArenaPlayer p) {
		Team t = handledTeams(p);
		return t == null ? INSTANCE.getSelfTeam(p) : t;
	}

	private static Team handledTeams(ArenaPlayer p) {
		synchronized(handlers){
			for (Team t: handlers.keySet()){
				if (t.hasMember(p))
					return t;
			}
		}
		return null;
	}

	public Team getSelfTeam(ArenaPlayer pl) {
		for (Team t: selfFormedTeams){
			if (t.hasMember(pl))
				return t;
		}
		return null;
	}

	public boolean removeSelfTeam(Team t) {
		return selfFormedTeams.remove(t);
	}

	public void addSelfTeam(Team t) {
		selfFormedTeams.add(t);
	}


	private void leaveSelfTeam(ArenaPlayer p) {
		Team t = getFormingTeam(p);
		if (t != null && formingTeams.remove(t)){
			t.sendMessage("&cYou're team has been disbanded as &6" + p.getDisplayName()+"&c has left minecraft");
			return;
		}
		t = getSelfTeam(p);
		if (t != null && selfFormedTeams.remove(t)){
			t.sendMessage("&cYou're team has been disbanded as &6" + p.getDisplayName()+"&c has left minecraft");
			return;
		}
	}

	private static void playerLeft(ArenaPlayer p) {
		Team t = handledTeams(p);
		if (t == null ){
			return;}
		List<TeamHandler> unused = new ArrayList<TeamHandler>();
		synchronized(handlers){
			List<TeamHandler> list = handlers.get(t);
			if (list == null){
				return;}
			synchronized(list){
				TeamHandler th;
				Iterator<TeamHandler> iter = list.iterator();
				while (iter.hasNext()){
					try{
						th = iter.next();
						if (th.leave(p)){ /// they are finished with the player, no longer need to keep them around
							unused.add(th);
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			list.retainAll(unused);
			if (list.isEmpty())
				handlers.remove(t);

		}
		//		logHandlerList("player left " + p.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		ArenaPlayer ap = PlayerController.toArenaPlayer(event.getPlayer());
		playerLeft(ap);
		leaveSelfTeam(ap);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		ArenaPlayer ap = PlayerController.toArenaPlayer(event.getPlayer());
		playerLeft(ap);
		leaveSelfTeam(ap);
	}

	public Map<Team,CopyOnWriteArrayList<TeamHandler>> getTeams() {
		return handlers;
	}

	public boolean inFormingTeam(ArenaPlayer p) {
		for (FormingTeam ft: formingTeams){
			if (ft.hasMember(p)){
				return true;}
		}
		return false;
	}

	public FormingTeam getFormingTeam(ArenaPlayer p) {
		for (FormingTeam ft: formingTeams){
			if (ft.hasMember(p)){
				return ft;}
		}
		return null;
	}

	public void addFormingTeam(FormingTeam ft) {
		formingTeams.add(ft);
	}

	public void removeFormingTeam(FormingTeam ft) {
		formingTeams.remove(ft);
	}

	public Map<TeamHandler,Team> getTeamMap(ArenaPlayer p){
		HashMap<TeamHandler,Team> map = new HashMap<TeamHandler,Team>();
		synchronized(handlers){
			for (Team t: handlers.keySet()){
				if (!t.hasMember(p)){
					continue;}
				List<TeamHandler> ths = handlers.get(t);
				synchronized(ths){
					for (TeamHandler th: ths){
						map.put(th,t);}				
				}
			}
		}
		return map;
	}
	//
	//	public static Team createTe3am(ArenaPlayer p, TeamHandler th) {
	//		if (DEBUG) System.out.println("------- createTeam " + p.getName() + ": " + th);
	//		Team t = TeamFactory.createTeam(p);
	//		List<TeamHandler> ths = new ArrayList<TeamHandler>();
	//		ths.add(th);
	//		handlers.put(t, ths);
	//		return t;
	//	}
	//
	//	public static Team createT3eam(Set<ArenaPlayer> players, TeamHandler th) {
	//		if (DEBUG) System.out.println("------- createTeam " + players.size() + ": " + th);
	//		Team t = TeamFactory.createTeam(players);
	//		List<TeamHandler> ths = new ArrayList<TeamHandler>();
	//		ths.add(th);
	//		handlers.put(t, ths);
	//		return t;
	//	}

	public static void addTeamHandler(Team t, TeamHandler th) {
		if (DEBUG) System.out.println("------- addTeamHandler " + t + ": " + th);
		CopyOnWriteArrayList<TeamHandler> ths = handlers.get(t);
		if (ths == null){
			//			ths = Collections.synchronizedList(new ArrayList<TeamHandler>());
			ths = new CopyOnWriteArrayList<TeamHandler>();
			//			ths = new ArrayList<TeamHandler>();
			synchronized(handlers){
				handlers.put(t, ths);
			}
		}
		if (ths != null){
			if (!ths.contains(th))
				ths.add(th);
		}

		//		logHandlerList("addTeamHandler " + t + "   " + th);
	}

	public static Team createTeam(ArenaPlayer p) {
		if (DEBUG) System.out.println("------- createTeam sans handler " + p.getName());
		return TeamFactory.createTeam(p);
	}

	public static void removeTeam(Team t, TeamHandler teamHandler) {
		if (DEBUG) System.out.println("------- removing team="+t+" and handler =" + teamHandler);
		List<TeamHandler> ths = handlers.get(t);
		if (ths != null){
			ths.remove(teamHandler);
			if (ths.isEmpty())
				handlers.remove(t);
		} else {
			handlers.remove(t);
		}
		//		logHandlerList("removeTeam " + t +"   " + th);
	}

	public static void removeTeams(Collection<Team> teams, TeamHandler teamHandler) {
		for (Team t: teams){
			removeTeam(t,teamHandler);
		}
	}

	public static CompositeTeam createCompositeTeam(Set<ArenaPlayer> players) {
		if (DEBUG) System.out.println("------- createCompositeTeam " + players.size());
		return TeamFactory.createCompositeTeam(players);
	}

	public static CompositeTeam createCompositeTeam(Team t, TeamHandler th) {
		CompositeTeam ct = new CompositeTeam();
		ct.addTeam(t);
		ct.finish();
		addTeamHandler(ct,th);
		if (DEBUG) System.out.println("------- createCompositeTeam " + ct);
		return ct;
	}
	public String toString(){
		return "[TeamController]";
	}

	public List<TeamHandler> getHandlers(ArenaPlayer p) {
		List<TeamHandler> hs = new ArrayList<TeamHandler>();
		synchronized(handlers){
			for (Team t: handlers.keySet()){
				if (t.hasMember(p)){
					hs.addAll(handlers.get(t));
				}
			}			
		}
		return hs;
	}

	/**
	 * Remember, just this is just 1 team leaving.. those players can still be in events on different teams
	 * as the events have composite teams that arent the same hashcode
	 * @param t
	 * @return
	 */
	public List<TeamHandler> getHandlers(Team t) {
		if (t == null) return null; /// null returns null
		return handlers.get(t);
	}



}
