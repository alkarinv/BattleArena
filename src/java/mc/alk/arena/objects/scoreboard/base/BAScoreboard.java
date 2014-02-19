package mc.alk.arena.objects.scoreboard.base;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.scoreboard.ArenaObjective;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

import java.util.List;

public class BAScoreboard extends ArenaScoreboard {
	Match match;

	public BAScoreboard(Match match) {
		super(match.getName());
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
	public STeam addTeam(ArenaTeam team) { return null;}

	@Override
	public STeam addedToTeam(ArenaTeam team, ArenaPlayer player) {return null;}

	@Override
	public STeam removeTeam(ArenaTeam team) {return null;}

	@Override
	public STeam removedFromTeam(ArenaTeam team, ArenaPlayer player) {return null;}

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
