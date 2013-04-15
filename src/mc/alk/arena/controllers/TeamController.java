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
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.FormingTeam;
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
	final Map<ArenaTeam,CopyOnWriteArrayList<TeamHandler>> handlers = new ConcurrentHashMap<ArenaTeam,CopyOnWriteArrayList<TeamHandler>>();

	/** Teams that are created through players wanting to be teams up, or an admin command */
	final Set<ArenaTeam> selfFormedTeams = Collections.synchronizedSet(new HashSet<ArenaTeam>());

	/** Teams that are still being created, these aren't "real" teams yet */
	final Set<FormingTeam> formingTeams = Collections.synchronizedSet(new HashSet<FormingTeam>());

	/**
	 * A valid team should either be currently being "handled" or is selfFormed
	 * @param player
	 * @return Team
	 */
	public static ArenaTeam getTeam(ArenaPlayer player) {
		return INSTANCE.handledTeams(player);
	}

	public static ArenaTeam getTeamNotTeamController(ArenaPlayer player) {
		return INSTANCE.handledTeamsNotTeamController(player);
	}
	public static void removeAllHandlers() {
		INSTANCE.removeAllHandledTeams();
	}

	public static void removeTeamHandlers(ArenaTeam t) {
		INSTANCE.removeHandledTeam(t);
	}
	private void removeHandledTeam(ArenaTeam t) {
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
	private ArenaTeam handledTeams(ArenaPlayer p) {
		synchronized(handlers){
			for (ArenaTeam t: handlers.keySet()){
				if (t.hasMember(p))
					return t;
			}
		}
		return null;
	}

	private ArenaTeam handledTeamsNotTeamController(ArenaPlayer p) {
		synchronized(handlers){
			for (ArenaTeam t: handlers.keySet()){
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


	public ArenaTeam getSelfFormedTeam(ArenaPlayer pl) {
		for (ArenaTeam t: selfFormedTeams){
			if (t.hasMember(pl))
				return t;
		}
		if (HeroesController.hasHeroes)
			return HeroesController.getTeam(pl.getPlayer());
		return null;
	}

	public boolean removeSelfFormedTeam(ArenaTeam team) {
		if (selfFormedTeams.remove(team)){
			removeHandler(team,this);
			return true;
		}
		return false;
	}

	public void addSelfFormedTeam(ArenaTeam team) {
		selfFormedTeams.add(team);
		addHandler(team, this);
	}

	private void leaveSelfTeam(ArenaPlayer p) {
		ArenaTeam t = getFormingTeam(p);
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
		ArenaTeam t = handledTeams(p);
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
//		playerLeft(ap);
		leaveSelfTeam(ap);
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		ArenaPlayer ap = PlayerController.toArenaPlayer(event.getPlayer());
//		playerLeft(ap);
		leaveSelfTeam(ap);
	}

	public Map<ArenaTeam,CopyOnWriteArrayList<TeamHandler>> getTeams() {
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

	public Map<TeamHandler,ArenaTeam> getTeamMap(ArenaPlayer p){
		HashMap<TeamHandler,ArenaTeam> map = new HashMap<TeamHandler,ArenaTeam>();
		synchronized(handlers){
			for (ArenaTeam t: handlers.keySet()){
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

	public static void addTeamHandler(ArenaTeam t, TeamHandler th) {
		INSTANCE.addHandler(t, th);
	}
	private void addHandler(ArenaTeam t, TeamHandler th){
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

	public static ArenaTeam createTeam(ArenaPlayer p) {
		if (DEBUG) System.out.println("------- createTeam sans handler " + p.getName());
		return TeamFactory.createTeam(p);
	}


	public static boolean removeTeamHandler(ArenaTeam team, TeamHandler teamHandler) {
		return INSTANCE.removeHandler(team, teamHandler);
	}

	private boolean removeHandler(ArenaTeam team, TeamHandler teamHandler){
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

	public static void removeTeams(Collection<ArenaTeam> teams, TeamHandler teamHandler) {
		for (ArenaTeam t: teams){
			removeTeamHandler(t,teamHandler);
		}
	}

	public static CompositeTeam createCompositeTeam(ArenaTeam t, TeamHandler th) {
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
			for (ArenaTeam t: handlers.keySet()){
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
	public List<TeamHandler> getHandlers(ArenaTeam t) {
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
