package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;

public class NoTeamsLeft extends NTeamsNeeded{
	public NoTeamsLeft(Match match) {
		super(match,1);
	}
}
