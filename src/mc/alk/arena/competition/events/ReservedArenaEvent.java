package mc.alk.arena.competition.events;

import java.util.Collection;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.competition.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchPlayersReadyEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.queues.TeamQObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.MessageUtil;

public class ReservedArenaEvent extends Event {
	public ReservedArenaEvent(EventParams params) {
		super(params);
	}

	Match arenaMatch;

	public void openEvent(EventParams mp, Arena arena) throws NeverWouldJoinException {
		arenaMatch = new ArenaMatch(arena, mp);
		arenaMatch.setTeamJoinHandler(null); /// we are taking care of this
		openEvent(mp);
	}

	public void autoEvent(EventParams mp, Arena arena) throws NeverWouldJoinException {
		openEvent(mp,arena);
		mc.sendCountdownTillEvent(mp.getSecondsTillStart());
		/// Set a countdown to announce updates every minute
		timer = new Countdown(BattleArena.getSelf(),mp.getSecondsTillStart(), mp.getAnnouncementInterval(), this);
	}

	@Override
	public void openEvent(EventParams mp) throws NeverWouldJoinException{
		super.openEvent(mp);
		rounds.clear();
		arenaMatch.addArenaListener(this);
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

	@MatchEventHandler
	public void allPlayersReady(MatchPlayersReadyEvent event){
		if (joinHandler != null && joinHandler.hasEnough(true)){
			startEvent();
		}
	}

	@MatchEventHandler
	public void matchCompleted(MatchCompletedEvent event){
		if (Defaults.DEBUG_TRACE) System.out.println("ReservedArenaEvent::matchComplete " +arenaMatch +"   isRunning()=" + isRunning());
		Collection<Team> victors = event.getMatch().getResult().getVictors();

		MatchResult r = event.getMatch().getResult();
		Matchup m = null;
		if (r.getVictors() != null && !r.getVictors().isEmpty()){
			m = getMatchup(r.getVictors().iterator().next(),0);
		}else if (r.getLosers() != null && !r.getLosers().isEmpty()){
			m = getMatchup(r.getLosers().iterator().next(),0);
		} else if (r.getDrawers() != null && !r.getDrawers().isEmpty()){
			m = getMatchup(r.getDrawers().iterator().next(),0);}
		if (m == null){ /// This match wasnt in our tournament
			return;}

		m.setResult(arenaMatch.getResult());
		eventVictory(victors,m.getResult().getLosers());
	}

	@MatchEventHandler
	public void matchFinished(MatchFinishedEvent event){
		eventCompleted();
	}

	@Override
	public TeamJoinResult joining(TeamQObject tqo){
		TeamJoinResult tjr = super.joining(tqo);
		switch(tjr.getEventType()){
		case ADDED:
			/// The first time, add the entire team
			arenaMatch.onJoin(tjr.team);
			break;
		case ADDED_TO_EXISTING:
			Team t = tqo.getTeam();
			if (arenaMatch.hasTeam(tjr.team)){
				for (ArenaPlayer p : t.getPlayers()){/// subsequent times, just the new players
					/// dont call arenaMatch.onJoin(Team), as part of the team might already be in arena
					arenaMatch.addedToTeam(tjr.team,p);
				}
			}
			String str = MessageUtil.joinPlayers(t.getPlayers(), ", ");
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
		m.addArenaListener(this);
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

		arenaMatch.onLeave(p);
		boolean canLeave = super.leave(p);
		return canLeave;
	}
	public Match getMatch(){
		return arenaMatch;
	}
}
