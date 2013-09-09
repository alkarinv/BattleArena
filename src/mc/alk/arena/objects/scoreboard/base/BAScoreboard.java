package mc.alk.arena.objects.scoreboard.base;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;
import mc.alk.scoreboardapi.scoreboard.SAPITeam;

public class BAScoreboard extends ArenaScoreboard {
//	final boolean solo;
//
	Match match;

	public BAScoreboard(Match match, MatchParams params) {
		super(match,params);
//		solo = params.getMaxTeamSize() == 1;
		this.match = match;
	}

	@Override
	public ArenaObjective createObjective(String id, String criteria, String displayName) {
		return createObjective(id,criteria,displayName,SAPIDisplaySlot.SIDEBAR);
	}

	@Override
	public ArenaObjective createObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot) {
		return createObjective(id,criteria,displayName,SAPIDisplaySlot.SIDEBAR, 50);
	}

	@Override
	public ArenaObjective createObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot, int priority) {
		ArenaObjective o = new ArenaObjective(id,criteria,displayName,slot,priority);
		addObjective(o);
		return o;
	}

	@Override
	public SAPITeam addTeam(ArenaTeam team) { return null;}

	@Override
	public void addedToTeam(ArenaTeam team, ArenaPlayer player) {/* do nothing */}

	@Override
	public void removeTeam(ArenaTeam team) {/* do nothing */}

	@Override
	public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {/* do nothing */}

	@Override
	public void setDead(ArenaTeam t, ArenaPlayer p) {/* do nothing */}

	@Override
	public void leaving(ArenaTeam t, ArenaPlayer player) {/* do nothing */}

	@Override
	public void addObjective(ArenaObjective scores) {
		this.registerNewObjective(scores);
	}

	@Override
	public List<STeam> getTeams() {
		return null;
	}

}
