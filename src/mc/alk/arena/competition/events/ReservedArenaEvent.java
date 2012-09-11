package mc.alk.arena.competition.events;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.util.NeverWouldJoinException;
import mc.alk.arena.competition.events.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.Util;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.WLT;

public class ReservedArenaEvent extends Event {
	public ReservedArenaEvent(MatchParams params) {
		super(params);
	}

	Match arenaMatch;	
	/// Run continuously members
	boolean runContinuously = false;
	int secondsTillNext = Defaults.AUTO_EVENT_COUNTDOWN_TIME;
	int announcementInterval = Defaults.ANNOUNCE_EVENT_INTERVAL;

	public void openEvent(MatchParams mp, Arena arena) throws NeverWouldJoinException {
		super.openEvent(mp);
		rounds.clear();

		matchParams.setPrettyName(prefix);
		arenaMatch = new Match(arena, ac, mp);
		ac.openMatch(arenaMatch);
		arenaMatch.onJoin(teams);
	}

	public void autoEvent(MatchParams mp, Arena arena, int secondsTillStart, int announcementInterval) throws NeverWouldJoinException {
		openEvent(mp,arena);
		mc.sendCountdownTillEvent(secondsTillStart);
		/// Set a countdown to announce updates every minute
		timer = new Countdown(BattleArena.getSelf(),secondsTillStart, announcementInterval, this);	
	}


	public void runContinuously(MatchParams mp, Arena arena,int secondsTillNext, int announcementInterval) throws NeverWouldJoinException {
		autoEvent(mp,arena,secondsTillNext,announcementInterval);
		runContinuously = true;
		this.secondsTillNext = secondsTillNext; 
		this.announcementInterval = announcementInterval;
	}


	@Override
	public void startEvent() {
		super.startEvent();
		mc.sendEventStarting(teams);
		makeNextRound();
		startRound();
	}

	@Override
	public void matchCancelled(Match am){
		runContinuously = false;

		if (!isRunning()) /// redundant call? can this happen anymore?
			return ;
		if (arenaMatch != am)
			return;
		if (Defaults.DEBUG_TRACE) System.out.println("ReservedArenaEvent::matchCancelled " +am +"   isRunning()=" + isRunning());		
		endEvent();
	}

	@Override
	public void matchComplete(Match am) {
		if (Defaults.DEBUG_TRACE) System.out.println("ReservedArenaEvent::matchComplete " +am +"   isRunning()=" + isRunning());
		if (!isRunning()) /// redundant call? can this happen anymore?
			return ;
		Team victor = am.getVictor();
		Matchup m = getMatchup(victor);
		if (m == null){
			return;
		}
		m.setResult(am.getResult());
		//		if (BattleArena.bet != null) BattleEventTracker.addTeamWinner(victor.getDisplayName(), getName());

		TrackerInterface bti = BTInterface.getInterface(matchParams);
		if (bti != null){
			BTInterface.addRecord(bti, victor.getPlayers(), am.getLosers(), WLT.WIN);			
		}
		//		Integer elo = (int) ((bti != null) ? BTInterface.loadRecord(bti, victor).getRanking() : Defaults.DEFAULT_ELO);

		eventVictory(victor,m.getResult().getLosers());
		endEvent();
		if (runContinuously){
			Arena arena = ac.getArenaByMatchParams(matchParams);
			if (arena == null){
				Log.err("&cCouldnt find an arena matching the params &6"+matchParams.toPrettyString()+". Stopping continuous run");
				return;
			}

			try {
				autoEvent(matchParams,arena,secondsTillNext,announcementInterval);
			} catch (NeverWouldJoinException e) {
				Log.err("&cCouldn't restart event " +matchParams.toPrettyString() +". " + e.getMessage());
			}
		}
	}


	@Override
	public TeamJoinResult joining(Team t){
		TeamJoinResult tjr = super.joining(t);
		switch(tjr.getEventType()){
		case ADDED:
			/// The first time, add the entire team
			arenaMatch.onJoin(tjr.team);
			break;
		case ADDED_TO_EXISTING:
			if (arenaMatch.hasTeam(t)){
				for (ArenaPlayer p : t.getPlayers()){/// subsequent times, just the new players
					/// dont call arenaMatch.onJoin(Team), as part of the team might already be in arena
					arenaMatch.playerAddedToTeam(p,tjr.team);}				
			}
			String str = Util.playersToCommaDelimitedString(t.getPlayers());
			for (ArenaPlayer p : t.getPlayers()){
				tjr.team.sendToOtherMembers(p, str +" has joined the team!");
			}										

			break;
		default:
		}
		return tjr;
	}

	@Override
	public void cancelEvent() {
		if (arenaMatch!=null){
			ac.cancelMatch(arenaMatch); /// let ourself and other splisteners know this has been cancelled
		}

		super.cancelEvent();
	}

	private void makeNextRound() {
		Matchup m = new Matchup(matchParams,teams);
		Round tr = new Round(0);
		tr.addMatchup(m);
		rounds.add(tr);
	}

	public boolean startRound(){
		ac.startMatch(arenaMatch);
		return true;
	}

	public Matchup getMatchup(Team t){
		Round tr = rounds.get(0);
		if (tr == null)
			return null;
		for (Matchup m : tr.getMatchups()){
			for (Team team: m.getTeams()){
				if (team.getName().equals(t.getName()))
					return m;
			}
		}
		return null;
	}

	public Arena getArena() {
		return arenaMatch.getArena();
	}

	@Override
	public boolean canLeave(ArenaPlayer p) {
		return !isRunning();
	}

	@Override
	public boolean canJoin() {
		return super.canJoin() && isOpen();
	}

	@Override
	public boolean leave(ArenaPlayer p){
		Team t = getTeam(p);
		if (t==null) /// they arent in this Event
			return true;

		boolean canLeave = super.leave(p);
		if (canLeave){ 
			arenaMatch.onLeave(p);
		}
		return canLeave;
	}

}
