package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.FormingTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.objects.teams.TeamHandler;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;


/// TODO this now needs to register generically or something
public class TeamController  implements Listener {
	static final boolean DEBUG = false;
	static Map<Team,List<TeamHandler>> inEvent = new ConcurrentHashMap<Team,List<TeamHandler>>();
	HashSet<Team> selfFormedTeams = new HashSet<Team>();
	HashSet<FormingTeam> formingTeams = new HashSet<FormingTeam>();
	BattleArenaController bac;

	public TeamController(BattleArenaController bac) {
		this.bac = bac;
	}

	public static Team getTeam(OfflinePlayer p) {
		return inEvent(p);
	}


	public static Team inEvent(OfflinePlayer p) {
		synchronized(inEvent){
			for (Team t: inEvent.keySet()){
				if (t.hasMember(p))
					return t;
			}
		}
		return null;
	}

	public Team getSelfTeam(OfflinePlayer pl) {
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


	private void leaveSelfTeam(Player p) {
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

	private static void playerLeft(Player p) {
		Team t = inEvent(p);
		if (t == null ){
			return;}

		synchronized(inEvent){
			List<TeamHandler> list = inEvent.get(t);
			if (list == null){
				return;}
			TeamHandler th;
			Iterator<TeamHandler> iter = list.iterator();
			while (iter.hasNext()){
				th = iter.next();
				if (DEBUG) System.out.println("  checking " + p.getName() +"    " +t +" th=" + th);
				if (th.leave(p)){ /// they are finished with the player, no longer need to keep them around
					iter.remove();}
			}				
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		playerLeft(event.getPlayer());
		leaveSelfTeam(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		playerLeft(event.getPlayer());
		leaveSelfTeam(event.getPlayer());
	}

	public Map<Team,List<TeamHandler>> getTeams() {
		return inEvent;
	}

	public boolean inFormingTeam(OfflinePlayer p) {
		for (FormingTeam ft: formingTeams){
			if (ft.hasMember(p)){
				return true;}
		}
		return false;
	}

	public FormingTeam getFormingTeam(OfflinePlayer p) {
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

	public Map<TeamHandler,Team> getTeamMap(OfflinePlayer p){
		HashMap<TeamHandler,Team> map = new HashMap<TeamHandler,Team>();
		synchronized(inEvent){
			for (Team t: inEvent.keySet()){
				if (!t.hasMember(p)){
					continue;}
				for (TeamHandler th: inEvent.get(t)){
					map.put(th,t);}
			}
		}
		return map;
	}

	public static Team createTeam(Player p, TeamHandler th) {
		if (DEBUG) System.out.println("------- createTeam " + p.getName() + ": " + th);
		Team t = TeamFactory.createTeam(p);
		List<TeamHandler> ths = new ArrayList<TeamHandler>();
		ths.add(th);
		inEvent.put(t, ths);
		return t;
	}

	public static Team createTeam(Set<Player> players, TeamHandler th) {
		if (DEBUG) System.out.println("------- createTeam " + players.size() + ": " + th);
		Team t = TeamFactory.createTeam(players);
		List<TeamHandler> ths = new ArrayList<TeamHandler>();
		ths.add(th);
		inEvent.put(t, ths);
		return t;
	}

	public static void addTeamHandler(Team t, TeamHandler th) {
		if (DEBUG) System.out.println("------- addTeamHandler " + t + ": " + th);
		List<TeamHandler> ths = inEvent.get(t);
		if (ths == null){
			ths = new ArrayList<TeamHandler>();
			inEvent.put(t, ths);
		}
		if (ths != null){
			if (!ths.contains(th))
				ths.add(th);
		}
	}

	public static Team createTeam(Player p) {
		if (DEBUG) System.out.println("------- createTeam sans handler " + p.getName());
		return TeamFactory.createTeam(p);
	}

	public static void removeTeam(Team t, TeamHandler th) {
		if (DEBUG) System.out.println("------- removing team="+t+" and handler =" + th);
		List<TeamHandler> ths = inEvent.get(t);
		if (ths != null){
			ths.remove(th);
			if (ths.isEmpty())
				inEvent.remove(t);
		} else {
			inEvent.remove(t);
		}
	}

	public static CompositeTeam createCompositeTeam(Set<Player> players) {
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

	public List<TeamHandler> getHandlers(Player p) {
		List<TeamHandler> handlers = new ArrayList<TeamHandler>();
		synchronized(inEvent){
			for (Team t: inEvent.keySet()){
				if (t.hasMember(p)){
					handlers.addAll(inEvent.get(t));
				}
			}			
		}
		return handlers;
	}

	/**
	 * Remember, just this is just 1 team leaving.. those players can still be in events on different teams
	 * as the events have composite teams that arent the same hashcode
	 * @param t
	 * @return
	 */
	public List<TeamHandler> getHandlers(Team t) {
		if (t == null) return null; /// null returns null
		return inEvent.get(t);
	}



}
