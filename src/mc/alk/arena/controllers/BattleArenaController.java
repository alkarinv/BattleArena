package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.MatchListener;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.ParamTeamPair;
import mc.alk.arena.objects.QPosTeamPair;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;
import mc.alk.arena.objects.tournament.Matchup;

import org.bukkit.Bukkit;


public class BattleArenaController implements OnMatchComplete, Runnable, TeamHandler{

	boolean stop = false;

	private ArenaMatchQueue amq = new ArenaMatchQueue(this);
	
//	private HashMap<String,Event> openEvents = new HashMap<String,Event>();
	private Set<Match> running_matches = new HashSet<Match>();
	private List<MatchListener> matchListeners = new ArrayList<MatchListener>();
	private Map<String, Arena> allarenas = new ConcurrentHashMap<String, Arena>();
	long lastTimeCheck = 0;

	/// Run is Thread Safe as well as every method and object it uses
	public void run() {
		Match match = null;
		while (!stop){
			match = amq.getArenaMatch();
			if (match != null){
				openMatch(match);
				startMatch(match);
			}
		}		
	}

	public void openMatch(Match match){
		synchronized(running_matches){
			running_matches.add(match);
		}
		/// BattleArena controller only tracks the players while they are in the queue
		/// now that a match is starting the players are no longer our responsibility
		for (Team t : match.getTeams()){
			TeamController.removeTeam(t, this);}
		match.open();
	}


	public void startMatch(Match arenaMatch) {
		/// arenaMatch run calls.... broadcastMessage ( which unfortunately is not thread safe)
		/// So we have to schedule a sync task... again
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), arenaMatch);
	}
	
	public void matchComplete(Match am) {
//		if (Defaults.DEBUG ) System.out.println("BattleArenaController::matchComplete=" + am + ":" );
		removeMatch(am); /// handles removing match from the BArenaController
		List<MatchListener> mls = null;
		synchronized(matchListeners){
			 mls = new ArrayList<MatchListener>(matchListeners);			
		}
		for (Team t : am.getTeams()){ /// Do I need to really do this?
			TeamController.removeTeam(t, this);}
		/// Notify all Listeners that this match is finished
		for (MatchListener ml: mls){
			if (am.getMatchState() == MatchState.ONCANCEL){
				try { ml.matchCancelled(am);} catch(Exception e){};
			} else {
				try { ml.matchComplete(am);} catch(Exception e){};				
			}
		}		
		Arena arena = allarenas.get(am.getArena().getName());
		if (arena != null)
			amq.add(arena); /// add it back into the queue

	}

	public void updateArena(Arena arena) {
		allarenas.put(arena.getName(), arena);
		if (amq.removeArena(arena) != null){ /// if its not being used
			amq.add(arena);			
		}
	}

	public void addArena(Arena arena) {
		allarenas.put(arena.getName(), arena);
		amq.add(arena);
	}

	public void addMatchListener(MatchListener ml) {matchListeners.add(ml);}
	public void removeMatchListener(MatchListener ml) {matchListeners.remove(ml);}

	public Map<String, Arena> getArenas(){return allarenas;}
	public QPosTeamPair addToQue(Team t1, MatchParams mp) {
		QPosTeamPair qpp = amq.add(t1,mp);
		if (qpp != null && qpp.pos >=0){
			TeamController.addTeamHandler(t1,this);
		}
		return qpp;
	}
	public boolean isInQue(ArenaPlayer p) {return amq.isInQue(p);}
	public QPosTeamPair getCurrentQuePos(ArenaPlayer p) {return amq.getQuePos(p);}
	public ParamTeamPair removeFromQue(ArenaPlayer p) {
		Team t = TeamController.getTeam(p);
		TeamController.removeTeam(t, this);
		return amq.removeFromQue(p);
	}
	public ParamTeamPair removeFromQue(Team t) {
		TeamController.removeTeam(t, this);
		return amq.removeFromQue(t);
	}
	public void addMatchup(Matchup m) {amq.addMatchup(m);}
	public Arena reserveArena(Arena arena) {return amq.reserveArena(arena);}
	public Arena getArena(String arenaName) {return allarenas.get(arenaName);}
	
	public Arena removeArena(Arena arena) {
		Arena a = amq.removeArena(arena);
		if (a != null){
			allarenas.remove(arena.getName());
		}
		return a;
	}
	
	public Arena nextArenaByMatchParams(MatchParams mp){
		return amq.getNextArena(mp);
	}
	public Arena getArenaByMatchParams(MatchParams mp) {
		for (Arena a : allarenas.values()){
			if (a.valid() && a.matches(mp)){
				return a;}
		}
		return null;
	}

	/**
	 * We dont care if they leave queues
	 */
	public boolean canLeave(ArenaPlayer p) {
		return true;
	}
	public boolean hasArenaSize(int i) {return getArenaBySize(i) != null;}
	public Arena getArenaBySize(int i) {
		for (Arena a : allarenas.values()){
			if (a.getParameters().matchesTeamSize(i)){
				return a;}
		}
		return null;
	}

	private void removeMatch(Match am){
		synchronized(running_matches){
			running_matches.remove(am);
		}		
	}

	public synchronized void stop() {
		if (stop)
			return;
		stop = true;
		amq.stop();
		synchronized(running_matches){
			for (Match am: running_matches){
				cancelMatch(am);
				Arena a = am.getArena();
				if (a != null){
					Arena arena = allarenas.get(a.getName());
					if (arena == null)
						amq.add(arena);
				}
			}
			running_matches.clear();
		}
	}
	
	/**
	 * If they are in a queue, take them out
	 */
	public boolean leave(ArenaPlayer p) {
		ParamTeamPair ptp = removeFromQue(p);
		if (ptp != null){
			ptp.team.sendMessage("&cYour team has left the queue b/c &6"+p.getDisplayName()+"c has left");			
		}
		/// else they are in a match, but those will be dealt with by the match itself
		/// In all cases, we no longer have to worry about this player or their team
		return true;
	}

	public boolean cancelMatch(Arena arena) {
		for (Match am: running_matches){
			if (am.getArena().getName().equals(arena.getName())){
				cancelMatch(am);
				return true;
			}
		}
		return false;
	}
	
	public boolean cancelMatch(ArenaPlayer p) {
		Match am = getMatch(p);
		if (am==null)
			return false;
		cancelMatch(am);
		return true;
	}

	public void cancelMatch(Team team) {
		Set<ArenaPlayer> ps = team.getPlayers();
		for (ArenaPlayer p : ps){
			cancelMatch(p);
			return;
		}
	}

	public void cancelMatch(Match am){
		am.cancelMatch();
		for (Team t: am.getTeams()){
			t.sendMessage("&4!!!&e This arena match has been cancelled");
		}
	}

	public Match getArenaMatch(Arena a) {
		synchronized(running_matches){
			for (Match am: running_matches){
				if (am.getArena().getName().equals(a.getName())){
					return am;
				}
			}
		}		
		return null;
	}

	public Match getMatch(ArenaPlayer p) {
		synchronized(running_matches){
			for (Match am: running_matches){
				if (am.hasPlayer(p)){
					return am;
				}
			}
		}		
		return null;
	}

	public String toString(){
		return "[BAC]";
	}

	public String toDetailedString(){
		StringBuilder sb = new StringBuilder();
		sb.append(amq);
		sb.append("------ arenas -------\n");
		for (Arena a : allarenas.values()){
			sb.append(a +"\n");
		}
		sb.append("------ running  matches -------\n");
		synchronized(running_matches){
			for (Match am : running_matches){
				sb.append(am + "\n");
			}
		}

		return sb.toString();
	}


	public void removeAllArenas() {
		synchronized(running_matches){
			for (Match am: running_matches){
				am.cancelMatch();
			}			
		}

		amq.stop();
		amq.removeAllArenas();
		synchronized(allarenas){
			allarenas.clear();
		}
		amq.resume();
		
	}
	public void removeAllArenas(ArenaType arenaType) {
		synchronized(running_matches){
			for (Match am: running_matches){
				if (am.getArena().getArenaType() == arenaType)
					am.cancelMatch();
			}
		}

		amq.stop();
		amq.removeAllArenas(arenaType);
		synchronized(allarenas){
			for (String aName: allarenas.keySet()){
				Arena a = allarenas.get(aName);
				if (a.getArenaType() == arenaType)
					allarenas.remove(aName);
			}
		}
		amq.resume();
		
	}

	public void cancelAllArenas() {
		synchronized(running_matches){
			for (Match am: running_matches){
				am.cancelMatch();
			}			
		}
	}

	public void purgeQueue() {
		// TODO Auto-generated method stub
		
	}
	
}
