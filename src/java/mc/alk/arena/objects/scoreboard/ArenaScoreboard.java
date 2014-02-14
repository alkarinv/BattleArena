package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.scoreboardapi.ScoreboardAPI;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.SScoreboard;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class ArenaScoreboard implements SScoreboard {
	final Match match;
	final protected SScoreboard board;

    @SuppressWarnings({"unused"})
	public ArenaScoreboard(Match match, MatchParams params) {
		this.match = match;
        this.board = Defaults.TESTSERVER ? ScoreboardAPI.createSAPIScoreboard(match.getName()) :
                ScoreboardAPI.createScoreboard(match.getName());
	}

	public ArenaObjective createObjective(String id, String criteria, String displayName) {
		return createObjective(id,criteria,displayName,SAPIDisplaySlot.SIDEBAR);
	}

	public ArenaObjective createObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot) {
		return createObjective(id,criteria,displayName,SAPIDisplaySlot.SIDEBAR, 50);
	}

	public ArenaObjective createObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot, int priority) {
		ArenaObjective o = new ArenaObjective(id,criteria,displayName,slot,priority);
		addObjective(o);
		return o;
	}

	public STeam addTeam(ArenaTeam team) { return null;}

	public void addedToTeam(ArenaTeam team, ArenaPlayer player) {/* do nothing */}

	public void removeTeam(ArenaTeam team) {/* do nothing */}

	public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {/* do nothing */}

	public void setDead(ArenaTeam t, ArenaPlayer p) {/* do nothing */}

	public void leaving(ArenaTeam t, ArenaPlayer player) {/* do nothing */}

	public void addObjective(ArenaObjective scores) {
		this.registerNewObjective(scores);
	}

	public List<STeam> getTeams() {
		return null;
	}

	@Override
	public SObjective registerNewObjective(String objectiveName,
			String criteria, String displayName, SAPIDisplaySlot slot) {
		return createObjective(objectiveName,criteria, displayName,slot);
	}

	@Override
	public void setDisplaySlot(SAPIDisplaySlot slot, SObjective objective) {
		board.setDisplaySlot(slot, objective);
	}

	@Override
	public void setDisplaySlot(SAPIDisplaySlot slot, SObjective objective,boolean fromObjective) {
		board.setDisplaySlot(slot, objective, fromObjective);
	}

	@Override
	public SObjective getObjective(SAPIDisplaySlot slot) {
		return board.getObjective(slot);
	}

	@Override
	public SObjective getObjective(String id) {
		return board.getObjective(id);
	}

	@Override
	public List<SObjective> getObjectives() {
		return board.getObjectives();
	}

	@Override
	public String getPrintString() {
		return board.getPrintString();
	}

	@Override
	public void createEntry(OfflinePlayer p) {
		board.createEntry(p);
	}

	@Override
	public SEntry createEntry(OfflinePlayer p, String displayName) {
		return board.createEntry(p,displayName);
	}

	@Override
	public SEntry createEntry(String id, String displayName) {
		return board.createEntry(id, displayName);
	}

	@Override
	public STeam createTeamEntry(String id, String displayName) {
		return board.createTeamEntry(id, displayName);
	}

	@Override
	public SEntry removeEntry(OfflinePlayer p) {
		return board.removeEntry(p);
	}

	@Override
	public SEntry removeEntry(SEntry e) {
		return board.removeEntry(e);
	}

	@Override
	public boolean setEntryName(String id, String name) {
		return board.setEntryName(id, name);
	}

	@Override
	public void setEntryName(SEntry e, String name) {
		board.setEntryName(e, name);
	}

    @Override
    public boolean setEntryNameSuffix(String id, String name) {
        return board.setEntryNameSuffix(id, name);
    }

    @Override
    public boolean setEntryNameSuffix(SEntry e, String name) {
        return board.setEntryNameSuffix(e, name);
    }

    @Override
	public String getName() {
		return board.getName();
	}

	@Override
	public SEntry getEntry(String id) {
		return board.getEntry(id);
	}

	@Override
	public SEntry getEntry(OfflinePlayer player) {
		return board.getEntry(player);
	}

	@Override
	public STeam getTeam(String id) {
		return board.getTeam(id);
	}

	@Override
	public void clear() {
		board.clear();
	}

	@Override
	public SObjective registerNewObjective(SObjective objective) {
		return board.registerNewObjective(objective);
	}

	@Override
	public SEntry getOrCreateEntry(OfflinePlayer p) {
		return board.getOrCreateEntry(p);
	}

	@Override
	public Collection<SEntry> getEntries() {
		return board.getEntries();
	}

    @Override
    public void removeScoreboard(Player player) {
        board.removeScoreboard(player);
    }

    @Override
    public void setScoreboard(Player player) {
        board.setScoreboard(player);
    }

    public void setObjectiveScoreboard(ArenaObjective arenaObjective) {
		arenaObjective.setScoreBoard(board);
	}

	public void setPoints(ArenaObjective objective, ArenaTeam team, int points) {
		objective.setPoints(team, points);
	}

	public void setPoints(ArenaObjective objective, ArenaPlayer player, int points) {
		objective.setPoints(player, points);
	}
}
