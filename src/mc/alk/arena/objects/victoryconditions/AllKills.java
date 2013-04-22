package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.players.ArenaPlayerKillEvent;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.MessageUtil;

/// TODO refactor with MobKills and PlayerKills
public class AllKills extends VictoryCondition implements ScoreTracker {
	final ArenaObjective kills;

	public AllKills(Match match) {
		super(match);
		kills = new ArenaObjective("allkills","All Kills");
		kills.setDisplayName(MessageUtil.colorChat("&4All Kills"));
	}

	@MatchEventHandler(priority=EventPriority.LOW)
	public void playerKillEvent(ArenaPlayerKillEvent event) {
		kills.addPoints(event.getPlayer(), event.getPoints());
		kills.addPoints(event.getTeam(), event.getPoints());
	}

	@MatchEventHandler(priority = EventPriority.LOW)
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
}
