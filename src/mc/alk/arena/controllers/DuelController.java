package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.Duel;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;

public class DuelController {
	static List<Duel> duels = new CopyOnWriteArrayList<Duel>();
	static HashMap<String, Long> rejectTimers = new HashMap<String,Long>();
	
	public static void addOutstandingDuel(Duel duel) {
		duels.add(duel);
	}


	public static Duel accept(ArenaPlayer player) {
		Duel d = getChallengedDuel(player);
		if (d != null){
			d.accept(player);
			if (d.isReady()){
				Team t = d.getChallengerTeam();
				Team t2 = d.makeChallengedTeam();
				List<Team> teams = new ArrayList<Team>();
				teams.add(t);
				teams.add(t2);
				Matchup m = new Matchup(d.getMatchParams(),teams);
				duels.remove(d);
				BattleArena.getBAC().addMatchup(m);
				
			}
		}
		return d;
	}

	public static Duel reject(ArenaPlayer player) {
		Duel d = getChallengedDuel(player);
		if (d != null){
			duels.remove(d);
			rejectTimers.put(player.getName(), System.currentTimeMillis());
		}
		return d;
	}

	public static boolean hasChallenger(ArenaPlayer player) {
		for (Duel d: duels){
			if (d.hasChallenger(player)){
				return true;}
		}
		return false;
	}
	
	public static Duel getDuel(ArenaPlayer player) {
		for (Duel d: duels){
			if (d.hasChallenger(player)){
				return d;}
		}
		return null;
	}

	public static Duel getChallengedDuel(ArenaPlayer player) {
		for (Duel d: duels){
			if (d.isChallenged(player)){
				return d;}
		}
		return null;
	}

	public static boolean isChallenged(ArenaPlayer ap) {
		for (Duel d: duels){
			if (d.isChallenged(ap))
				return true;
		}
		return false;
	}

	public static Duel rescind(ArenaPlayer player) {
		Duel d = getDuel(player);
		if (d != null){
			duels.remove(d);
		}
		return d;
	}


	public static Long getLastRejectTime(ArenaPlayer ap) {
		Long t = rejectTimers.get(ap.getName());
		if (t == null)
			return t;
		if (Defaults.DUEL_CHALLENGE_INTERVAL*1000 < System.currentTimeMillis() - t){
			rejectTimers.remove(ap.getName());}
		return t;
	}
	
}
