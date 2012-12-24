package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.VictoryUtil;

public class HighestKills extends PvPCount{
	public HighestKills(Match match) {
		super(match);
	}

	@MatchEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		Collection<Team> leaders = VictoryUtil.highestKills(match);
		if (leaders.size() > 1){
			event.setCurrentDrawers(leaders);
		} else {
			event.setCurrentLeaders(leaders);
		}
	}
}
