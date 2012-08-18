package mc.alk.arena.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.events.util.NeverWouldJoinException;
import mc.alk.arena.match.Match;
import mc.alk.arena.match.PerformTransition;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TimeUtil;
import mc.alk.arena.util.Util;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.Stat;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.alk.battleEventTracker.BattleEventTracker;

public class TournamentEvent extends Event {
	public long timeBetweenRounds;

	int round = -1;
	int nrounds = -1;
	boolean preliminary_round = false;
	ArrayList<Team> aliveTeams = new ArrayList<Team>();
	ArrayList<Team> competingTeams = new ArrayList<Team>();
	final MatchParams oParms ; /// Our original default params
	
	public TournamentEvent(MatchParams params) {
		super(params);
		oParms = params;
	}

	@Override
	public void openEvent(MatchParams mp) throws NeverWouldJoinException {
		aliveTeams.clear();
		competingTeams.clear();
		rounds.clear();
		round = -1;
		nrounds = -1;
		matchParams.setPrettyName(prefix);
		timeBetweenRounds = oParms.getTimeBetweenRounds();
		String color = Util.getColor(mp.getPrefix());
		mp.setPrefix(color+"["+mp.getName() +" " + oParms.getName()+"]");
		mp.setCommand(oParms.getCommand());
		mp.setName(mp.getName()+" " + oParms.getName());
		mp.setTimeBetweenRounds(oParms.getTimeBetweenRounds());
		mp.setSecondsTillMatch(oParms.getSecondsTillMatch());
		mp.setSecondsToLoot(oParms.getSecondsToLoot());
		
		TimeUtil.testClock();
		super.openEvent(mp);
	}

	@Override
	public void startEvent() {
		super.startEvent();
		Server server = Bukkit.getServer();

		int osize = teams.size();
		nrounds = getNRounds(osize);
		int nteams = (int) Math.pow(2, nrounds);
		if (!silent)
			server.broadcastMessage(Log.colorChat(prefix+"&e The " + matchParams.toPrettyString() +
				oParms.getName() + " tournament is starting!"));

		preliminary_round = teams.size()!=nteams;
		if (preliminary_round) nrounds++;

		TreeMap<Double,Team> sortTeams = new TreeMap<Double,Team>(Collections.reverseOrder());
		TrackerInterface bti = BTInterface.getInterface(matchParams);
		//		System.out.println("startEvent:: bti=" + bti);
		for (Team t: teams){
			Double elo = Defaults.DEFAULT_ELO;
			if (bti != null){
				Stat s = BTInterface.loadRecord(bti, t);
				if (s!= null) elo= (double) s.getElo();
			}
			while (sortTeams.containsKey(elo)){elo+= 0.0001;}
			sortTeams.put(elo, t);
		}
		competingTeams.addAll(teams);
		teams.clear();
		aliveTeams.clear();
		ArrayList<Team> ts = new ArrayList<Team>(sortTeams.values());
		for (Team t: ts){
			teams.add(t);
			aliveTeams.add(t);
		}
		if (!silent)
			server.broadcastMessage(Log.colorChat(prefix+"&6 " + teams.size() + " &e" +MessageController.getTeamsOrPlayers(teams.size())+
				" will compete in a &6"+nrounds+"&e round tournament"));
		if (preliminary_round){			
			makePreliminaryRound();			
		} else{
			makeNextRound();			
		}
		startRound();
	}

	@Override
	public void matchCancelled(Match am){
		Matchup m = getMatchup(am.getTeams().get(0),round);
//		System.out.println("victor ===" + victor + "  am= " +am + " losers=" + am.getLosers() +"   m = " + m +"   am.result="+am.getResult());	
		if (m == null){ /// This match wasnt in our tournament
			return;}
		cancelEvent();
	}
	
	@Override
	public void matchComplete(Match am) {
		if (!isRunning())
			return ;
		Team victor = am.getVictor();
		Matchup m = getMatchup(victor,round);
//		System.out.println("victor ===" + victor + "  am= " +am + " losers=" + am.getLosers() +"   m = " + m +"   am.result="+am.getResult());	
		if (m == null){ /// This match wasnt in our tournament
			return;}
		if (am.getMatchState() == MatchState.ONCANCEL){
			cancelEvent();
			return;}

		m.setResult(am.getResult());
		for (Team t: am.getResult().getLosers()){
			super.removeTeam(t);
		}
		aliveTeams.removeAll(am.getResult().getLosers());

		if (roundFinished()){
			TimeUtil.testClock();
			if (Defaults.DEBUG) System.out.println("ROUND FINISHED !!!!!   " + aliveTeams);	

			if (round+1 == nrounds || isFinished()){
				matchParams.setPrettyName("&4[Tournament]");
				Server server = Bukkit.getServer();
				Team t = aliveTeams.get(0);
				if (!silent) server.broadcastMessage(Log.colorChat(prefix+"&e Congratulations to &6" + t.getDisplayName() + "&e for winning!"));
				PerformTransition.transition(am, MatchState.FIRSTPLACE, t,false);
				if (BattleArena.bet != null) BattleEventTracker.addTeamWinner(t.getDisplayName(), getName());
				endEvent();
			} else {
				makeNextRound();
				startRound();
			}
		}
	}


	private void makePreliminaryRound() {
		Matchup m;
		round++;
		Round tr = new Round(round);
		rounds.add(tr);
		final int needed_size = (int) Math.pow(2, nrounds-1);
		final int nprelims = aliveTeams.size() - needed_size;

		final int loffset = needed_size -1;
		final int hoffset = needed_size;

		for (int i = 0;i< nprelims;i++){
			Team t1 = aliveTeams.get(loffset-i);
			Team t2 = aliveTeams.get(hoffset+i);
			m = new Matchup(matchParams,t1,t2);
			tr.addMatchup(m);
		}
	}

	private void makeNextRound() {
		Matchup m;
		round++;
		Round tr = new Round(round);
		rounds.add(tr);
		int j = aliveTeams.size()-1;
		for (int i = 0;i< aliveTeams.size()/2;i++){
			Team t1 = aliveTeams.get(i);
			Team t2 = aliveTeams.get(j-i);
			m = new Matchup(matchParams,t1,t2);
			tr.addMatchup(m);
		}
	}

	private boolean roundFinished() {
		Round tr = rounds.get(round);
		for (Matchup m : tr.getMatchups()){
			if (!m.result.matchComplete())
				return false;
		}
		return true;
	}

	public boolean startRound(){
		if (round <0){ /// trying to start when we havent created anything yet
			return false;}
		announceRound();
		Plugin plugin = BattleArena.getSelf();
		/// Section to start the match
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Round tr = rounds.get(round);
				for (Matchup m: tr.getMatchups()){
					ac.addMatchup(m);
				}
			}
		}, (long) (timeBetweenRounds * 20L * Defaults.TICK_MULT));
		return true;
	}

	private void announceRound() {
		Round tr = rounds.get(round);
		String strround = "Round " + (round +1);
		if (round+1 == nrounds){
			strround = "Final Round";
		}
		if (preliminary_round){
			preliminary_round = false;
			int nprelims = tr.getMatchups().size()*2;
			for (int i=0;i< aliveTeams.size()-nprelims;i++){
				Team t = aliveTeams.get(i);
				t.sendMessage("&4["+strround+"]&e You have a &5bye&e this round");
			}
		}
		matchParams.setPrettyName("&4[" + strround +"]");
		TrackerInterface bti = BTInterface.getInterface(matchParams);
		for (Matchup m: tr.getMatchups()){
			if (m.getTeams().size() == 2){
				Team t1 = m.getTeam(0);
				Team t2 = m.getTeam(1);
				Double elo1 = (bti != null) ? BTInterface.loadRecord(bti, t1).getElo() : Defaults.DEFAULT_ELO;
				Double elo2 = (bti != null) ? BTInterface.loadRecord(bti, t2).getElo() : Defaults.DEFAULT_ELO;
				//				System.out.println("team1 stat= " + bti.loadRecord(t1.getPlayers()));
				broadcast(prefix + "&e "+ strround +": &8"+t1.getDisplayName() +"&6["+ elo1+"]" +
						"&e vs &8" +t2.getDisplayName() +"&6["+ elo2+"]");							
			}
		}
		if (round != nrounds)
			broadcast(prefix+"&e "+strround+" will start in &4" + timeBetweenRounds +" &eseconds!");			
		else 
			broadcast(prefix +"&e The "+strround+" will start in &4" + timeBetweenRounds +" &eseconds!");
	}
	
	@Override
	public void broadcast(String msg){for (Team t : competingTeams){t.sendMessage(msg);}}

	@Override
	public void addTeam(Team t){
		super.addTeam(t);
		int size = teams.size();
		int nrounds = getNRounds(size);
		int idealteam = (int) Math.pow(2, nrounds);
		if (size > 2 && size % idealteam == 0){
			if (!silent) Bukkit.broadcastMessage(Log.colorChat(prefix+"&6" + size +" "+MessageController.getTeamsOrPlayers(teams.size())+
					"&e have joined, Current tournament will have &6" + nrounds+"&e rounds"));
		}			
	}


	public int getNRounds(int size){
		return (int) Math.floor(Math.log(size)/Math.log(2));
	}

	@Override
	public boolean canLeave(Player p) {
		Team t = getTeam(p);
		return isOpen() || (t != null && !aliveTeams.contains(t));
	}
}
