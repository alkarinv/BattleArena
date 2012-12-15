package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.util.VictoryUtil;

public class HighestKills extends PvPCount{
	public HighestKills(Match match) {
		super(match);
	}

	@MatchEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		event.setCurrentLeaders(VictoryUtil.highestKills(match));
	}
}
