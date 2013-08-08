package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.players.ArenaPlayerKillEvent;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.MessageUtil;

public class KillLimit extends VictoryCondition implements ScoreTracker{
	final ArenaObjective kills;
	final StatController sc;

	public KillLimit(Match match) {
		super(match);
		kills = new ArenaObjective("playerkills","Player Kills",5);
		kills.setDisplayName(MessageUtil.colorChat("&4First to 50"));
		boolean isRated = match.getParams().isRated();
		boolean soloRating = !match.getParams().isTeamRating();
		sc = (isRated && soloRating) ? new StatController(match.getParams()): null;
	}

	@ArenaEventHandler(priority=EventPriority.LOW)
	public void playerKillEvent(ArenaPlayerKillEvent event) {
		kills.addPoints(event.getPlayer(), event.getPoints());
		Integer points = kills.addPoints(event.getTeam(), event.getPoints());
		if (sc != null)
			sc.addRecord(event.getPlayer(),event.getTarget(),WinLossDraw.WIN);
		if (points >= 50){
			this.match.setVictor(event.getTeam());
		}

	}

	@ArenaEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		if (event.isMatchEnding()){
			event.setResult(kills.getMatchResult(match));
		} else {
			Collection<ArenaTeam> leaders = kills.getLeaders();
			if (leaders.size() > 1){
				event.setCurrentDrawers(leaders);
			} else {
				event.setCurrentLeaders(leaders);
			}
		}
	}

	@Override
	public List<ArenaTeam> getLeaders() {
		return kills.getTeamLeaders();
	}

	@Override
	public TreeMap<Integer,Collection<ArenaTeam>> getRanks() {
		return kills.getTeamRanks();
	}

	@Override
	public void setScoreBoard(ArenaScoreboard scoreboard) {
		this.kills.setScoreBoard(scoreboard);
		scoreboard.addObjective(kills);
	}
}
