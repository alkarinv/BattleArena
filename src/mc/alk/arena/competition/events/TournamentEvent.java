package mc.alk.arena.competition.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.match.PerformTransition;
import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.listeners.custom.MatchCreationCallback;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionResult;
import mc.alk.arena.objects.CompetitionSize;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.EventState;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;

public class TournamentEvent extends Event implements Listener, MatchCreationCallback{
	public long timeBetweenRounds;

	int round = -1;
	int nrounds = -1;
	boolean preliminary_round = false;
	ArrayList<ArenaTeam> aliveTeams = new ArrayList<ArenaTeam>();
	ArrayList<ArenaTeam> competingTeams = new ArrayList<ArenaTeam>();
	final EventParams oParms ; /// Our original default tourney params from the config
	Random rand = new Random();
	Integer curTimer = null;
	Map<Match, Matchup> matchups = Collections.synchronizedMap(new HashMap<Match,Matchup>());

	public TournamentEvent(EventParams params) {
		super(params);
		oParms = params;
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());

	}

	@Override
	public void matchCreated(Match match, Matchup matchup) {
		matchups.put(match, matchup);
	}

	@Override
	public void openEvent(EventParams mp) throws NeverWouldJoinException {
		aliveTeams.clear();
		competingTeams.clear();
		rounds.clear();
		round = -1;
		nrounds = -1;
		timeBetweenRounds = oParms.getTimeBetweenRounds();
		ChatColor color = MessageUtil.getFirstColor(mp.getPrefix());
		mp.setTransitionOptions(mp.getTransitionOptions());
		mp.setPrefix(color+"["+mp.getName() +" " + oParms.getName()+"]");
		mp.setCommand(oParms.getCommand());
		mp.setName(mp.getName()+" " + oParms.getName());
		mp.setTimeBetweenRounds(oParms.getTimeBetweenRounds());
		mp.setSecondsTillMatch(oParms.getSecondsTillMatch());
		mp.setSecondsToLoot(oParms.getSecondsToLoot());
		TimeUtil.testClock();
		super.openEvent(mp);
		EventParams copy = new EventParams(mp);
		copy.setMaxTeams(CompetitionSize.MAX);
		this.setTeamJoinHandler(TeamJoinFactory.createTeamJoinHandler(copy, this));
		for (ArenaTeam t: teams){
			TeamController.removeTeamHandler(t, this);
		}
		joinHandler.removeImproperTeams();
	}

	@Override
	public void startEvent() {
		super.startEvent();
		Server server = Bukkit.getServer();
		int osize = teams.size();
		nrounds = getNRounds(osize);
		final int minTeams = eventParams.getMinTeams();
		int roundteams = (int) Math.pow(minTeams, nrounds);
		server.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&e The " + eventParams.toPrettyString() +
				oParms.getName() + " tournament is starting!"));

		TreeMap<Double,ArenaTeam> sortTeams = new TreeMap<Double,ArenaTeam>(Collections.reverseOrder());
		StatController sc = new StatController(eventParams);

		for (ArenaTeam t: teams){
			Double elo = Defaults.DEFAULT_ELO;
			ArenaStat stat = sc.loadRecord(t);
			elo = (double) stat.getRating();
			while (sortTeams.containsKey(elo)){elo+= 0.0001;}
			sortTeams.put(elo, t);
		}
		teams.clear();
		aliveTeams.clear();
		ArrayList<ArenaTeam> ts = new ArrayList<ArenaTeam>(sortTeams.values());
		for (ArenaTeam t: ts){
			teams.add(t);
			aliveTeams.add(t);
			competingTeams.add(t);
		}
		removeExtraneous();
		preliminary_round = teams.size()!=roundteams;
		if (preliminary_round) nrounds++;
		server.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&6 " + teams.size() + " &e" +MessageUtil.getTeamsOrPlayers(teams.size())+
				" will compete in a &6"+nrounds+"&e round tournament"));
		if (preliminary_round){
			makePreliminaryRound();
		} else{
			makeNextRound();
		}
		startRound();
	}


	@MatchEventHandler
	public void matchCancelled(MatchCancelledEvent event){
		Match am = event.getMatch();
		if (am.getState() == MatchState.ONCANCEL){
			endEvent();
			return;}
		matchEnded(am, am.getResult());
	}

	@Override
	public void endEvent(){
		super.endEvent();
		aliveTeams.clear();
		competingTeams.clear();
		matchups.clear();
		if (curTimer != null){
			Bukkit.getScheduler().cancelTask(curTimer);
			curTimer = null;
		}
	}

	@MatchEventHandler
	public void matchCompleted(MatchCompletedEvent event){
		Match am = event.getMatch();

		if (am.getState() == MatchState.ONCANCEL){
			endEvent();
			return;}

		matchEnded(am, am.getResult());
	}

	private void matchEnded(Match am, MatchResult r) {
		Matchup m = matchups.get(am);
		if (m==null){
			eventCancelled();
			Log.err("[BA Error] match completed but not found in tournament");
			return;
		}
		ArenaTeam victor = null;
		if (r.isDraw() || r.isUnknown()){ /// match was a draw, pick a random lucky winner
			victor = pickRandomWinner(r, r.getDrawers());
		} else if (r.hasVictor() && r.getVictors().size() != 1){
			victor = pickRandomWinner(r, r.getVictors());
		} else if (r.hasVictor()){
			victor = r.getVictors().iterator().next(); /// single winner
		}
		m.setResult(r);
		for (ArenaTeam t: r.getLosers()){
			super.removeTeam(t);
		}
		aliveTeams.removeAll(r.getLosers());

		if (roundFinished()){
			TimeUtil.testClock();
			if (Defaults.DEBUG) System.out.println("ROUND FINISHED !!!!!   " + aliveTeams);

			if (round+1 == nrounds || isFinished()){
				ArenaTeam t = aliveTeams.get(0);
				HashSet<ArenaTeam> losers = new HashSet<ArenaTeam>(competingTeams);
				losers.remove(victor);
				Set<ArenaTeam> victors = new HashSet<ArenaTeam>(Arrays.asList(victor));
				CompetitionResult result = new CompetitionResult();
				result.setVictors(victors);
				setEventResult(result);
				PerformTransition.transition(am, MatchState.FIRSTPLACE, t,false);
				PerformTransition.transition(am, MatchState.PARTICIPANTS, losers,false);
				eventCompleted();
			} else {
				makeNextRound();
				startRound();
			}
		}
	}

	private ArenaTeam pickRandomWinner(MatchResult r, Collection<ArenaTeam> randos) {
		ArenaTeam victor;
		List<ArenaTeam> ls = new ArrayList<ArenaTeam>(randos);
		if (ls.isEmpty()){
			Log.err("[BattleArena] Tournament found a match with no players, cancelling tournament");
			this.cancelEvent();
			return null;
		}
		victor = ls.get(rand.nextInt(ls.size()));
		victor.sendMessage("&2You drew your match but have been randomly selected as the winner!");
		r.setVictor(victor);
		Set<ArenaTeam> losers = new HashSet<ArenaTeam>(ls);
		losers.remove(victor);
		r.addLosers(losers);
		for (ArenaTeam l: losers){
			l.sendMessage("&cYou drew your match but someone else has been randomly selected as the winner!");
		}
		return victor;
	}

	private void removeExtraneous(){
		/// remaining teams
		int minTeams = eventParams.getMinTeams();
		final int needed_size = (int) Math.pow(minTeams, nrounds);
		final int nprelims = (teams.size() - needed_size) / (minTeams-1);

		int remaining = teams.size() - (needed_size + nprelims*(minTeams-1));
		if (remaining > 0){
			List<ArenaTeam> newTeams = new ArrayList<ArenaTeam>();
			for (int i=0;i<remaining;i++){
				ArenaTeam t = teams.get(needed_size + i);
				newTeams.add(t);
				t.sendMessage("&c[Tourney] There weren't enough players for you to compete in this tourney");
			}
			teams.removeAll(newTeams);
			aliveTeams.removeAll(newTeams);
		}
	}

	private void makePreliminaryRound() {
		Matchup m;
		round++;
		Round tr = new Round(round);
		rounds.add(tr);
		int nrounds = getNRounds(teams.size()) + 1;
		int minTeams = eventParams.getMinTeams();
		final int needed_size = (int) Math.pow(minTeams, nrounds-1);
		final int nprelims = (aliveTeams.size() - needed_size) / (minTeams-1);

		int loffset = needed_size -1;
		int hoffset = needed_size;

		for (int i = 0;i< nprelims;i++){
			List<ArenaTeam> newTeams = new ArrayList<ArenaTeam>();
			for (int j=0;j<minTeams/2;j++){
				newTeams.add(aliveTeams.get(loffset));
				newTeams.add(aliveTeams.get(hoffset));
				loffset--;
				hoffset++;
			}
			m = new Matchup(eventParams,newTeams);
			m.addArenaListener(this);
			m.addMatchCreationListener(this);
			tr.addMatchup(m);
		}
	}

	private void makeNextRound() {
		Matchup m;
		round++;
		Round tr = new Round(round);
		rounds.add(tr);
		int minTeams = eventParams.getMinTeams();
		int size = aliveTeams.size();
		final int nMatches = size/minTeams;
		for (int i = 0;i< nMatches;i++){
			List<ArenaTeam> newTeams = new ArrayList<ArenaTeam>();
			for (int j=0;j<minTeams/2;j++){
				int index = i + j*nMatches;
				newTeams.add(aliveTeams.get(index));
				newTeams.add(aliveTeams.get(size-1-index));
			}
			m = new Matchup(eventParams,newTeams);
			m.addArenaListener(this);
			m.addMatchCreationListener(this);
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
		/// trying to start when we havent created anything yet
		/// or event was canceled/closed
		if (round <0 || state == EventState.CLOSED){
			return false;}
		announceRound();
		Plugin plugin = BattleArena.getSelf();
		/// Section to start the match
		curTimer = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
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

		String strround = preliminary_round && round==0 ? "PreliminaryRound" : ("Round "+(round+1));
		if (round+1 == nrounds){
			strround = "Final Round";}
		if (preliminary_round){
			preliminary_round = false;
			int nprelims = tr.getMatchups().size()*eventParams.getMinTeams();
			for (int i=0;i< aliveTeams.size()-nprelims;i++){
				ArenaTeam t = aliveTeams.get(i);
				t.sendMessage("&4["+strround+"]&e You have a &5bye&e this round");
			}
		}
		StatController sc = new StatController(eventParams);
		final String prefix = eventParams.getPrefix();
		if (tr.getMatchups().size() <= 8){
			for (Matchup m: tr.getMatchups()){
				List<String> names = new ArrayList<String>();
				for (ArenaTeam t: m.getTeams()){
					ArenaStat st = sc.loadRecord(t);
					names.add("&8"+t.getDisplayName()+"&6["+st.getRating()+"]");
				}
				String msg = "&e"+ strround +": " + StringUtils.join(names, " vs ");
				if (ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH > msg.length() + prefix.length()){
					broadcastAlive(prefix+" "+msg);
				} else {
					broadcastAlive(msg);
				}
			}
		} else {
			broadcastAlive(prefix + "&e Round " + strround +" has " + tr.getMatchups().size()+ " "+
					MessageUtil.teamsOrPlayers(eventParams.getMinTeamSize())+" competing. &6/tourney status:&e for updates");
		}
		if (round != nrounds)
			broadcast(prefix+"&e "+strround+" will start in &4" + timeBetweenRounds +" &eseconds!");
		else
			broadcast(prefix +"&e The "+strround+" will start in &4" + timeBetweenRounds +" &eseconds!");
	}

	@Override
	public void broadcast(String msg){for (ArenaTeam t : competingTeams){t.sendMessage(msg);}}

	public void broadcastAlive(String msg){for (ArenaTeam t : aliveTeams){t.sendMessage(msg);}}

	@Override
	public boolean addedToTeam(ArenaTeam team, ArenaPlayer ap){
		if (super.addedToTeam(team, ap)){
			if (team.size() == 1){ /// it's finally a valid team
				announceTourneySize();}
			return true;
		}
		return false;
	}

	@Override
	public boolean addTeam(ArenaTeam team){
		if (super.addTeam(team)){
			announceTourneySize();
			return true;
		}
		return false;
	}

	private void announceTourneySize() {
		int size = 0;
		for (ArenaTeam t: teams){
			if (t.size() > 0)
				size++;
		}
		int nrounds = getNRounds(size);
		int idealteam = (int) Math.pow(eventParams.getMinTeams(), nrounds);
		if (nrounds > 1 && size % idealteam == 0){
			Bukkit.broadcastMessage(Log.colorChat(eventParams.getPrefix()+"&6" + size +" "+MessageUtil.getTeamsOrPlayers(teams.size())+
					"&e have joined, Current tournament will have &6" + nrounds+"&e rounds"));
		}
	}

	public int getNRounds(int size){
		return (int) Math.floor(Math.log(size)/Math.log(eventParams.getMinTeams()));
	}

	@Override
	public boolean canLeave(ArenaPlayer p) {
		ArenaTeam t = getTeam(p);
		return isOpen() || (t != null && !aliveTeams.contains(t));
	}

	@Override
	public String getStatus() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getStatus());
		if (round <0){
			return sb.toString();}
		sb.append("&e Alive Teams=&6 " + aliveTeams.size()+"\n");
		Round tr = rounds.get(round);
		int ncomplete = tr.getCompleteMatchups().size();
		final int total = tr.getMatchups().size();
		sb.append(preliminary_round && round==0 ? "&ePreliminaryRound" : "&eRound");
		sb.append("&4 " + (round+1)+" &eComplete Matches: &6 " + ncomplete +"/" +total);
		return sb.toString();
	}


}
