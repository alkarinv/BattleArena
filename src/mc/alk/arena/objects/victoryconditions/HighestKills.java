package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchFindNeededTeamsEvent;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.util.VictoryUtil;

public class HighestKills extends VictoryCondition{
	public HighestKills(Match match) {
		super(match);
	}

	@TransitionEventHandler
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		event.setCurrentLeader(VictoryUtil.highestKills(match));
	}

	@TransitionEventHandler
	public void onNeededTeams(MatchFindNeededTeamsEvent event) {
		event.setNeededTeams(2);
	}

}
