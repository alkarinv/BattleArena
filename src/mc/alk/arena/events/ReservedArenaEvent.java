package mc.alk.arena.events;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.events.util.NeverWouldJoinException;
import mc.alk.arena.events.util.TeamJoinHandler.TeamJoinResult;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.objects.tournament.Round;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Countdown;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.WLT;

import org.bukkit.entity.Player;

import com.alk.battleEventTracker.BattleEventTracker;

public class ReservedArenaEvent extends Event {
	public ReservedArenaEvent(MatchParams params) {
		super(params);
	}

	Match arenaMatch;	

	public void autoEvent(MatchParams mp, Arena arena, int seconds) throws NeverWouldJoinException {
		openEvent(mp,arena);
		if (!silent)
			mc.sendCountdownTillEvent(seconds);
		/// Set a countdown to announce updates every minute
		timer = new Countdown(BattleArena.getSelf(),seconds, Defaults.ANNOUNCE_EVENT_INTERVAL, this);
		
	}

	public void openEvent(MatchParams mp, Arena arena) throws NeverWouldJoinException {
		super.openEvent(mp);
		rounds.clear();

		matchParams.setPrettyName(prefix);
		arenaMatch = new Match(arena, ac, mp);
		ac.openMatch(arenaMatch);
		arenaMatch.onJoin(teams);
	}

	@Override
	public void startEvent() {
		super.startEvent();

		//		server.broadcastMessage(Log.colorChat(prefix+"&e The " + matchParams.toPrettyString() +" is starting!"));
		mc.sendEventStarting(teams);

		makeNextRound();
		startRound();
	}
	@Override
	public void matchCancelled(Match am){
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
		if (BattleArena.bet != null) BattleEventTracker.addTeamWinner(victor.getDisplayName(), getName());

		TrackerInterface bti = BTInterface.getInterface(matchParams);
		if (bti != null){
			BTInterface.addRecord(bti, victor.getPlayers(), am.getLosers(), WLT.WIN);			
		}
		Integer elo = (int) ((bti != null) ? BTInterface.loadRecord(bti, victor).getElo() : Defaults.DEFAULT_ELO);

		mc.sendEventWon(victor, elo);
		endEvent();
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
			for (Player p : t.getPlayers()){/// subsequent times, just the new players
				/// dont call arenaMatch.onJoin(Team), as part of the team might already be in arena
				arenaMatch.playerAddedToTeam(p,tjr.team);}
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
	public boolean canLeave(Player p) {
		return !isRunning();
	}

	@Override
	public boolean canJoin() {
		return super.canJoin() && isOpen();
	}

	@Override
	public boolean leave(Player p){
		Team t = getTeam(p);
		if (t==null) /// they arent in this bukkitEvent
			return true;

		boolean canLeave = super.leave(p);
		if (canLeave){ 
			arenaMatch.onLeave(p);
		}
		return canLeave;
	}

}
