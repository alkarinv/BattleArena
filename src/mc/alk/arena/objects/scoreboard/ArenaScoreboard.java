package mc.alk.arena.objects.scoreboard;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaScoreboard {
	final boolean solo;

	Map<String, ArenaObjective> objectives = new HashMap<String,ArenaObjective>();

	public ArenaScoreboard(MatchParams params) {
		solo = params.getMaxTeamSize() == 1;
	}

	public void clear(){
		objectives.clear();
	}

	public void addObjective(ArenaObjective objective) {
		objectives.put(objective.getName(), objective);
		objective.setScoreBoard(this);
	}

	public void addTeam(ArenaTeam team) {
		/* do nothing */
	}

	public void addedToTeam(ArenaTeam team, ArenaPlayer player) {
		/* do nothing */
	}

	public void removeTeam(ArenaTeam team) {
		/* do nothing */
	}

	public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
		/* do nothing */
	}

	public void setPoints(ArenaObjective objective, ArenaTeam team, int points) {
		/* do nothing */
	}

	public void setPoints(ArenaObjective objective, ArenaPlayer player, int points) {
		/* do nothing */
	}

	public void setDead(ArenaTeam t, ArenaPlayer p) {
		/* do nothing */
	}

	public void leaving(ArenaTeam t, ArenaPlayer player) {
		/* do nothing */
	}
}
