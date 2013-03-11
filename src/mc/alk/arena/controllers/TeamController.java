package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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


public enum TeamController implements Listener, TeamHandler {
	INSTANCE;

	static final boolean DEBUG = false;

	/** A map of teams to which classes are currently "handling" those teams */
	final Map<Team,CopyOnWriteArrayList<TeamHandler>> handlers = new ConcurrentHashMap<Team,CopyOnWriteArrayList<TeamHandler>>();

	/** Teams that are created through players wanting to be teams up, or an admin command */
	final Set<Team> selfFormedTeams = Collections.synchronizedSet(new HashSet<Team>());

	/** Teams that are still being created, these aren't "real" teams yet */
	final Set<FormingTeam> formingTeams = Collections.synchronizedSet(new HashSet<FormingTeam>());

	/**
	 * A valid team should either be currently being "handled" or is selfFormed
	 * @param player
	 * @return Team
	 */
	public static Team getTeam(ArenaPlayer player) {
		return INSTANCE.handledTeams(player);
	}

	public static Team getTeamNotTeamController(ArenaPlayer player) {
		return INSTANCE.handledTeamsNotTeamController(player);
	}
	public static void removeAllHandlers() {
		INSTANCE.removeAllHandledTeams();
	}

	public static void removeTeamHandlers(Team t) {
		INSTANCE.removeHandledTeam(t);
	}
	private void removeHandledTeam(Team t) {
		synchronized(handlers){
			List<TeamHandler> hs = handlers.remove(t);
			if (hs != null){
				for (TeamHandler th: hs){
					for (ArenaPlayer ap: t.getPlayers()){
						th.leave(ap);}
				}
			}
		}
	}
	private void removeAllHandledTeams() {
		selfFormedTeams.clear();
		formingTeams.clear();
		synchronized(handlers){
			handlers.clear();
		}
	}
	private Team handledTeams(ArenaPlayer p) {
		synchronized(handlers){
			for (Team t: handlers.keySet()){
				if (t.hasMember(p))
					return t;
			}
		}
		return null;
	}

	private Team handledTeamsNotTeamController(ArenaPlayer p) {
		synchronized(handlers){
			for (Team t: handlers.keySet()){
				if (t.hasMember(p)){
					List<TeamHandler> list = handlers.get(t);
					for (TeamHandler th: list){
						if (th != INSTANCE)
							return t;
					}
				}
			}
		}
		return null;
	}


	public Team getSelfFormedTeam(ArenaPlayer pl) {
		for (Team t: selfFormedTeams){
			if (t.hasMember(pl))
				return t;
		}
		if (HeroesController.hasHeroes)
			return HeroesController.getTeam(pl.getPlayer());
		return null;
	}

	public boolean removeSelfFormedTeam(Team team) {
		if (selfFormedTeams.remove(team)){
			removeHandler(team,this);
			return true;
		}
		return false;
	}

	public void addSelfFormedTeam(Team team) {
		selfFormedTeams.add(team);
		addHandler(team, this);
	}

	private void leaveSelfTeam(ArenaPlayer p) {
		Team t = getFormingTeam(p);
		if (t != null && formingTeams.remove(t)){
			t.sendMessage("&cYou're team has been disbanded as &6" + p.getDisplayName()+"&c has left minecraft");
			return;
		}
		t = getSelfFormedTeam(p);
		if (t != null && selfFormedTeams.remove(t)){
			t.sendMessage("&cYou're team has been disbanded as &6" + p.getDisplayName()+"&c has left minecraft");
			return;
		}
	}

	private void playerLeft(ArenaPlayer p) {
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

	public boolean removeFormingTeam(FormingTeam ft) {
		return formingTeams.remove(ft);
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

	public static void addTeamHandler(Team t, TeamHandler th) {
		INSTANCE.addHandler(t, th);
	}
	private void addHandler(Team t, TeamHandler th){
		if (DEBUG) System.out.println("------- addTeamHandler " + t + ": " + th);
		CopyOnWriteArrayList<TeamHandler> ths = handlers.get(t);
		if (ths == null){
			ths = new CopyOnWriteArrayList<TeamHandler>();
			synchronized(handlers){
				handlers.put(t, ths);
			}
		}
		if (ths != null){
			if (!ths.contains(th))
				ths.add(th);
		}
	}

	public static Team createTeam(ArenaPlayer p) {
		if (DEBUG) System.out.println("------- createTeam sans handler " + p.getName());
		return TeamFactory.createTeam(p);
	}


	public static boolean removeTeamHandler(Team team, TeamHandler teamHandler) {
		return INSTANCE.removeHandler(team, teamHandler);
	}

	private boolean removeHandler(Team team, TeamHandler teamHandler){
		if (DEBUG) System.out.println("------- removing team="+team+" and handler =" + teamHandler);
		List<TeamHandler> ths = handlers.get(team);
		if (ths != null){
			ths.remove(teamHandler);
			if (ths.isEmpty())
				handlers.remove(team);
			return true;
		} else {
			return handlers.remove(team) != null;
		}
		//		logHandlerList("removeTeam " + t +"   " + th);
	}

	public static void removeTeams(Collection<Team> teams, TeamHandler teamHandler) {
		for (Team t: teams){
			removeTeamHandler(t,teamHandler);
		}
	}

	public static CompositeTeam createCompositeTeam(Team t, TeamHandler th) {
		CompositeTeam ct = TeamFactory.createCompositeTeam(t);
		addTeamHandler(ct,th);
		if (DEBUG) System.out.println("------- createCompositeTeam " + ct);
		return ct;
	}

	@Override
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

	@Override
	public boolean canLeave(ArenaPlayer p) {
		return true;
	}

	@Override
	public boolean leave(ArenaPlayer p) {
		return true;
	}

}
