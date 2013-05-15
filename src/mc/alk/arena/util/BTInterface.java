package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.objects.stats.TrackerArenaStat;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.tracker.Tracker;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.StatType;
import mc.alk.tracker.objects.WLT;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class BTInterface {
	public static Tracker battleTracker = null;
	public static TrackerInterface aBTI = null;
	static private HashMap<String, TrackerInterface> btis = new HashMap<String, TrackerInterface>();
	static private HashMap<String, TrackerInterface> currentInterfaces = new HashMap<String, TrackerInterface>();
	final TrackerInterface ti;
	boolean valid = false;

	public BTInterface(MatchParams mp){
		ti = getInterface(mp);
		valid = battleTracker != null && ti != null;
	}
	public boolean isValid(){
		return valid;
	}
	public Stat getRecord(TrackerInterface bti, ArenaTeam t){
		try{return bti.getRecord(t.getBukkitPlayers());} catch(Exception e){e.printStackTrace();return null;}
	}
	public Stat loadRecord(TrackerInterface bti, ArenaTeam t){
		try{return bti.loadRecord(t.getBukkitPlayers());} catch(Exception e){e.printStackTrace();return null;}
	}
	public static TrackerInterface getInterface(MatchParams sq){
		if (sq == null)
			return null;
		final String db = sq.getDBName();
		return db == null ? null : btis.get(db);
	}
	public static boolean hasInterface(MatchParams mp){
		if (mp == null)
			return false;
		final String db = mp.getDBName();
		return db == null ? false : btis.containsKey(db);
	}

	public static void addRecord(TrackerInterface bti, Set<ArenaTeam> victors,Set<ArenaTeam> losers, Set<ArenaTeam> drawers, WinLossDraw wld) {
		if (victors != null){
			Set<ArenaPlayer> winningPlayers = new HashSet<ArenaPlayer>();
			for (ArenaTeam w : victors){
				winningPlayers.addAll(w.getPlayers());
			}
			WLT wlt = null;
			switch(wld){
			case WIN: wlt = WLT.WIN; break;
			case LOSS: wlt = WLT.LOSS; break;
			case DRAW: wlt = WLT.TIE; break;
			default: wlt = WLT.WIN; break;
			}
			addRecord(bti,winningPlayers, losers,wlt);
		}
	}
	public static void addRecord(TrackerInterface bti, Set<ArenaPlayer> players, Collection<ArenaTeam> losers, WLT win) {
		if (bti == null)
			return;
		try{
			Set<Player> winningPlayers = PlayerController.toPlayerSet(players);
			if (losers.size() == 1){
				Set<Player> losingPlayers = new HashSet<Player>();
				for (ArenaTeam t: losers){losingPlayers.addAll(t.getBukkitPlayers());}
				if (Defaults.DEBUG_TRACKING) System.out.println("BA Debug: addRecord ");
				for (Player p: winningPlayers){
					if (Defaults.DEBUG_TRACKING) System.out.println("BA Debug: winner = "+p.getName());}
				for (Player p: losingPlayers){
					if (Defaults.DEBUG_TRACKING) System.out.println("BA Debug: loser = "+p.getName());}
				bti.addTeamRecord(winningPlayers, losingPlayers, WLT.WIN);
			} else {
				Collection<Collection<Player>> plosers = new ArrayList<Collection<Player>>();
				for (ArenaTeam t: losers){
					plosers.add(t.getBukkitPlayers());
				}
				bti.addRecordGroup(winningPlayers, plosers, WLT.WIN);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static boolean addBTI(MatchParams pi) {
		if (battleTracker == null)
			return false;
		final String dbName = pi.getDBName();
		if (Defaults.DEBUG) System.out.println("adding BTI for " + pi +"  " + dbName);
		TrackerInterface bti = btis.get(dbName);
		if (bti == null){
			/// Try to first the interface from our existing ones
			bti = currentInterfaces.get(dbName);
			if (bti==null){ /// no current interface, do we even have the BattleTracker plugin?
				battleTracker = (Tracker) Bukkit.getPluginManager().getPlugin("BattleTracker");
				if (battleTracker == null) {
					/// Well BattleTracker obviously isnt enabled.. not much we can do about that
					return false;
				}
				/// yay, we have it, now get our interface
				bti = Tracker.getInterface(dbName);
				currentInterfaces.put(dbName, bti);
				if (aBTI == null)
					aBTI = bti;
			}
			btis.put(dbName, bti);
		}
		return true;
	}

	public static void resumeTracking(ArenaPlayer p) {
		if (aBTI != null)
			aBTI.resumeTracking(p.getName());
	}
	public static void stopTracking(ArenaPlayer p) {
		if (aBTI != null)
			aBTI.stopTracking(p.getName());
	}
	public static void resumeTracking(Set<Player> players) {
		if (aBTI != null)
			aBTI.resumeTracking(players);
	}
	public static void stopTracking(Set<Player> players) {
		if (aBTI != null)
			aBTI.stopTracking(players);
	}

	@SuppressWarnings("deprecation")
	public Integer getElo(ArenaTeam t) {
		if (!isValid())
			return new Integer((int) Defaults.DEFAULT_ELO);
		Stat s = getRecord(ti,t);
		return (int) (s == null ? Defaults.DEFAULT_ELO : s.getRanking());
	}
	public Stat loadRecord(ArenaTeam team) {
		if (!isValid()) return null;
		return loadRecord(ti, team);
	}
	public Stat loadRecord(OfflinePlayer player){
		if (!isValid()) return null;
		try{return ti.loadRecord(player);} catch(Exception e){e.printStackTrace();return null;}
	}

	public static ArenaStat loadRecord(String dbName, ArenaTeam t) {
		TrackerInterface ti = btis.get(dbName);
		if (ti == null)
			return StatController.BLANK_STAT;
		Stat st = null;
		try{st = ti.loadRecord(t.getBukkitPlayers());}catch(Exception e){e.printStackTrace();}
		return st == null ? StatController.BLANK_STAT : new TrackerArenaStat(st);
	}


	@SuppressWarnings("deprecation")
	public String getRankMessage(OfflinePlayer player) {
		Stat stat = loadRecord(player);
		if (stat == null){
			return "&eCouldn't find stats for player " + player.getName();}
		Integer rank = ti.getRank(player.getName());
		if (rank == null)
			rank = -1;
		return "&eRank:&6"+rank+"&e (&4"+stat.getWins()+"&e:&8"+stat.getLosses()+"&e)&6["+stat.getRanking()+"]&e" +
				". Highest &6["+ stat.getMaxRating()+"]&e Longest Streak &b"+stat.getMaxStreak();
	}
	public boolean setRating(OfflinePlayer player, Integer elo) {
		return ti.setRating(player, elo);
	}
	public void resetStats() {
		ti.resetStats();
	}
	public void printTopX(CommandSender sender, int x, int minTeamSize, String headerMsg, String bodyMsg) {
		ti.printTopX(sender, StatType.RANKING, x, minTeamSize,headerMsg,bodyMsg);
	}
	public static void setTrackerPlugin(Plugin plugin) {
		battleTracker = (Tracker) plugin;
	}
	public static void addRecord(MatchParams mp, Set<ArenaTeam> victors,
			Set<ArenaTeam> losers, Set<ArenaTeam> drawers, WinLossDraw wld) {
		TrackerInterface bti = BTInterface.getInterface(mp);
		if (bti != null ){
			try{BTInterface.addRecord(bti,victors,losers,drawers,wld);}catch(Exception e){e.printStackTrace();}
		}
	}

}
