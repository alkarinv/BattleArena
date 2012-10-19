package mc.alk.arena.competition.events;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.Exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Util;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.WLT;

public class ReservedArenaEvent extends Event {
	public ReservedArenaEvent(EventParams params) {
		super(params);
	}

	Match arenaMatch;	

	public void openEvent(EventParams mp, Arena arena) throws NeverWouldJoinException {
		arenaMatch = new ArenaMatch(arena, mp);
		openEvent(mp);
	}

	public void autoEvent(EventParams mp, Arena arena, int secondsTillStart, int announcementInterval) throws NeverWouldJoinException {
		openEvent(mp,arena);
		mc.sendCountdownTillEvent(secondsTillStart);
		/// Set a countdown to announce updates every minute
		timer = new Countdown(BattleArena.getSelf(),secondsTillStart, announcementInterval, this);	
	}

	public void openAllPlayersEvent(EventParams mp, Arena arena) throws NeverWouldJoinException {
		arenaMatch = new ArenaMatch(arena, mp);
		super.openAllPlayersEvent(mp);
	}

	@Override
	public void openEvent(EventParams mp) throws NeverWouldJoinException{
		super.openEvent(mp);
		rounds.clear();
		eventParams.setPrettyName(mp.getCommand());
		arenaMatch.addTransitionListener(this);
		ac.openMatch(arenaMatch);
		arenaMatch.onJoin(teams);
	}
	
	@Override
	public void startEvent() {
		super.startEvent();
		mc.sendEventStarting(teams);
		makeNextRound();
		startRound();
	}

	@TransitionEventHandler
	public void matchCompleted(MatchCompletedEvent event){
		if (Defaults.DEBUG_TRACE) System.out.println("ReservedArenaEvent::matchComplete " +arenaMatch +"   isRunning()=" + isRunning());
		Team victor = event.getMatch().getResult().getVictor();
		
		Matchup m;
		if (victor == null)
			m = getMatchup(event.getMatch().getResult().getLosers().iterator().next());
		else 
			 m = getMatchup(victor);
		if (m == null){
			return;
		}
		m.setResult(arenaMatch.getResult());

		TrackerInterface bti = BTInterface.getInterface(eventParams);
		if (bti != null && victor != null){
			BTInterface.addRecord(bti, victor.getPlayers(), arenaMatch.getLosers(), WLT.WIN);			
		}

		eventVictory(victor,m.getResult().getLosers());
		eventCompleted();
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
			if (arenaMatch.hasTeam(tjr.team)){
				for (ArenaPlayer p : t.getPlayers()){/// subsequent times, just the new players
					/// dont call arenaMatch.onJoin(Team), as part of the team might already be in arena
					arenaMatch.playerAddedToTeam(p,tjr.team);
				}				
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
		Matchup m = new Matchup(eventParams,teams);
		Round tr = new Round(0);
		tr.addMatchup(m);
		rounds.add(tr);
	}

	public boolean startRound(){
		ac.startMatch(arenaMatch);
		return true;
	}

	public Matchup getMatchup(Team t){
		if (rounds == null || rounds.isEmpty())
			return null;
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
