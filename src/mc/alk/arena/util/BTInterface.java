package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;
import mc.alk.tracker.Tracker;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.WLT;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;


public class BTInterface {
	public static Tracker battleTracker = null;
	public static TrackerInterface aBTI = null;
	static private HashMap<String, TrackerInterface> btis = new HashMap<String, TrackerInterface>();
	static private HashMap<String, TrackerInterface> currentInterfaces = new HashMap<String, TrackerInterface>();
	TrackerInterface ti;
	boolean valid;
	public BTInterface(MatchParams mp){
		ti = getInterface(mp);
		valid = ti != null;
	}
	public boolean isValid(){
		return valid;
	}
	public static Stat getRecord(TrackerInterface bti, Team t){
		return bti.getRecord(t.getPlayers());
	}
	public static Stat loadRecord(TrackerInterface bti, Team t){
		return bti.loadRecord(t.getPlayers());
	}
	public static TrackerInterface getInterface(MatchParams sq){
		if (sq == null)
			return null;
		final String db = sq.getDBName();
		return db == null ? null : btis.get(db);
	}

	public static void addRecord(TrackerInterface bti, Set<Player> players, Collection<Team> losers, WLT win) {
		if (bti == null)
			return;
		try{
			if (losers.size() == 1){
				Set<Player> losingPlayers = new HashSet<Player>();
				for (Team t: losers){losingPlayers.addAll(t.getPlayers());}
				bti.addTeamRecord(players, losingPlayers, WLT.WIN);						
			} else {
				Collection<Collection<Player>> plosers = new ArrayList<Collection<Player>>();
				for (Team t: losers){
					plosers.add(t.getPlayers());
				}
				bti.addRecordGroup(players, plosers, WLT.WIN);			
			} 
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void addBTI(MatchParams pi) {
		final String dbName = pi.getDBName();
		TrackerInterface bti = btis.get(dbName);
		if (bti == null){
			/// Try to first the interface from our existing ones
			bti = currentInterfaces.get(dbName);
			if (bti==null){ /// no current interface, do we even have the BattleTracker plugin?
				battleTracker = (Tracker) Bukkit.getPluginManager().getPlugin("BattleTracker");
				if (battleTracker == null) {
					/// Well BattleTracker obviously isnt enabled.. not much we can do about that
					return;
				}
				/// yay, we have it, now get our interface
				bti = Tracker.getInterface(dbName);
				PluginDescriptionFile pdf = BattleArena.getSelf().getDescription();
				Log.info(pdf.getName()+" is now using BattleTracker interface '" + dbName+"'");
				currentInterfaces.put(dbName, bti);
				if (aBTI == null)
					aBTI = bti;
			}
			if (Defaults.DEBUG) System.out.println("adding BTI for " + pi +"  " + dbName);
			btis.put(dbName, bti);
		}
	}

	public static void resumeTracking(Player p) {
		if (aBTI != null)
			aBTI.resumeTracking(p);		
	}
	public static void stopTracking(Player p) {
		if (aBTI != null)
			aBTI.stopTracking(p);		
	}
	public static void resumeTracking(Set<Player> players) {
		if (aBTI != null)
			aBTI.resumeTracking(players);
	}
	public static void stopTracking(Set<Player> players) {
		if (aBTI != null)
			aBTI.stopTracking(players);
	}
	public static boolean hasInterface(MatchParams pi) {
		return btis.containsKey(pi.getName());
	}
	public Integer getElo(Team t) {
		Stat s = getRecord(ti,t);
		return (int) (s == null ? Defaults.DEFAULT_ELO : s.getElo());
	}
	public Stat loadRecord(Team team) {
		return BTInterface.loadRecord(ti, team);
	}

}
