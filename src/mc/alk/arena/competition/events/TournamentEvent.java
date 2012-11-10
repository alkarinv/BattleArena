package mc.alk.arena.competition.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.match.PerformTransition;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TournamentEvent extends Event implements Listener{
	public long timeBetweenRounds;

	int round = -1;
	int nrounds = -1;
	boolean preliminary_round = false;
	ArrayList<Team> aliveTeams = new ArrayList<Team>();
	ArrayList<Team> competingTeams = new ArrayList<Team>();
	final EventParams oParms ; /// Our original default tourney params from the config
	Random rand = new Random();

	public TournamentEvent(EventParams params) {
		super(params);
		oParms = params;
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
	}

	@Override
	public void openEvent(EventParams mp) throws NeverWouldJoinException {
		aliveTeams.clear();
		competingTeams.clear();
		rounds.clear();
		round = -1;
		nrounds = -1;
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
		server.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&e The " + eventParams.toPrettyString() +
				oParms.getName() + " tournament is starting!"));

		preliminary_round = teams.size()!=nteams;
		if (preliminary_round) nrounds++;

		TreeMap<Double,Team> sortTeams = new TreeMap<Double,Team>(Collections.reverseOrder());
		BTInterface bti = new BTInterface(eventParams);
		for (Team t: teams){
			Double elo = Defaults.DEFAULT_ELO;
			if (bti.isValid()){
				elo = (double) bti.getElo(t);}
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
		server.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&6 " + teams.size() + " &e" +MessageUtil.getTeamsOrPlayers(teams.size())+
				" will compete in a &6"+nrounds+"&e round tournament"));
		if (preliminary_round){
			makePreliminaryRound();
		} else{
			makeNextRound();
		}
		startRound();
	}


	@TransitionEventHandler
	public void matchCancelled(MatchCancelledEvent event){
		Match am = event.getMatch();
		Matchup m = getMatchup(am.getTeams().get(0),round);
		System.out.println("victor ===  am= " +am + " losers=" + am.getLosers() +"   m = " + m +"   am.result="+am.getResult());
		if (m == null){ /// This match wasnt in our tournament
			return;}
		eventCancelled();
	}
	@Override
	public void endEvent(){
		super.endEvent();
		aliveTeams.clear();
		competingTeams.clear();
	}
	@TransitionEventHandler
	public void matchCompleted(MatchCompletedEvent event){
		Match am = event.getMatch();
		Team victor = am.getVictor();
		Matchup m;
		if (victor == null)
			m = getMatchup(am.getResult().getLosers().iterator().next(),round);
		else
			 m = getMatchup(victor,round);
		System.out.println("victor ===" + victor + "  am= " +am + " losers=" + am.getLosers() +"   m = " + m +"   am.result="+am.getResult());
		if (m == null){ /// This match wasnt in our tournament
			return;}
		if (am.getState() == MatchState.ONCANCEL){
			endEvent();
			return;}
		MatchResult r = am.getResult();
		if (victor == null){ /// match was a draw, pick a random lucky winner
			List<Team> ls = new ArrayList<Team>(am.getResult().getLosers());
			if (ls.isEmpty()){
				Log.err("[BattleArena] Tournament found a match with no players, cancelling tournament");
				this.cancelEvent();
				return;
			}
			victor = ls.get(rand.nextInt(ls.size()));
			victor.sendMessage("&2You drew your match but have been randomly selected as the winner!");
			r.setVictor(victor);
			Set<Team> losers = new HashSet<Team>(ls);
			losers.remove(victor);
			r.setLosers(losers);
			for (Team l: losers){
				l.sendMessage("&cYou drew your match but someone else has been randomly selected as the winner!");
			}
		}
		m.setResult(r);
		for (Team t: r.getLosers()){
			super.removeTeam(t);
		}
		aliveTeams.removeAll(r.getLosers());

		if (roundFinished()){
			TimeUtil.testClock();
			if (Defaults.DEBUG) System.out.println("ROUND FINISHED !!!!!   " + aliveTeams);

			if (round+1 == nrounds || isFinished()){
				Server server = Bukkit.getServer();
				Team t = aliveTeams.get(0);
				server.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&e Congratulations to &6" + t.getDisplayName() + "&e for winning!"));
				HashSet<Team> losers = new HashSet<Team>(competingTeams);
				losers.remove(victor);
				eventVictory(victor,losers);
				PerformTransition.transition(am, MatchState.FIRSTPLACE, t,false);
				PerformTransition.transition(am, MatchState.PARTICIPANTS, losers,false);
				eventCompleted();
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
			m = new Matchup(eventParams,t1,t2);
			m.addTransitionListener(this);
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
			m = new Matchup(eventParams,t1,t2);
			m.addTransitionListener(this);
			tr.addMatchup(m);
		}
	}

	private boolean roundFinished() {
		Round tr = rounds.get(round);
		for (Matchup m : tr.getMatchups()){
			if (!m.result.isFinished())
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
		BTInterface bti = new BTInterface(eventParams);
		final String prefix = eventParams.getPrefix();
		for (Matchup m: tr.getMatchups()){
			if (m.getTeams().size() == 2){
				Team t1 = m.getTeam(0);
				Team t2 = m.getTeam(1);

				Double elo1 = (double) bti.getElo(t1);
				Double elo2 = (double) bti.getElo(t2);
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
			Bukkit.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&6" + size +" "+MessageUtil.getTeamsOrPlayers(teams.size())+
					"&e have joined, Current tournament will have &6" + nrounds+"&e rounds"));
		}
	}


	public int getNRounds(int size){
		return (int) Math.floor(Math.log(size)/Math.log(2));
	}

	@Override
	public boolean canLeave(ArenaPlayer p) {
		Team t = getTeam(p);
		return isOpen() || (t != null && !aliveTeams.contains(t));
	}
}
