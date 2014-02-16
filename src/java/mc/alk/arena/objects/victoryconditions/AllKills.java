package mc.alk.arena.objects.victoryconditions;

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
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/// TODO refactor with MobKills and PlayerKills.yml
public class AllKills extends VictoryCondition implements ScoreTracker {
	final ArenaObjective kills;
	final StatController sc;

	public AllKills(Match match) {
		super(match);
		kills = new ArenaObjective("allkills",  "All Kills", MessageUtil.colorChat("&4All Kills"),
                SAPIDisplaySlot.SIDEBAR, 60);
		boolean isRated = match.getParams().isRated();
		boolean soloRating = !match.getParams().isTeamRating();
		sc = (isRated && soloRating) ? new StatController(match.getParams()): null;
	}

	@ArenaEventHandler(priority=EventPriority.LOW)
	public void playerKillEvent(ArenaPlayerKillEvent event) {
		kills.addPoints(event.getPlayer(), event.getPoints());
		kills.addPoints(event.getTeam(), event.getPoints());
		if (sc != null)
			sc.addRecord(event.getPlayer(),event.getTarget(),WinLossDraw.WIN);
	}

	@ArenaEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		Collection<ArenaTeam> leaders = kills.getLeaders();
		if (leaders.size() > 1){
			event.setCurrentDrawers(leaders);
		} else {
			event.setCurrentLeaders(leaders);
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

	@Override
	public void setDisplayTeams(boolean display) {
		kills.setDisplayTeams(display);
	}
}
