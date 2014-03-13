package mc.alk.arena.objects;

import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.Collection;
import java.util.Set;


/**
 * Represents the result of a competition.
 * Modifying this object will modify the outcome of the match.
 */
public interface CompetitionResult{

    /**
	 * Changes the outcome type of this match to the given type.
	 * Example, adding winners to this match will not change the outcome,
	 * unless this match is set to a WinLossDraw.WIN
	 * @param wld The WinLossDraw type.
	 */
	public void setResult(WinLossDraw wld);

    public void setVictor(ArenaTeam vic);

    public void setVictors(Collection<ArenaTeam> victors);

    public void setDrawers(Collection<ArenaTeam> drawers);

    public void setLosers(Collection<ArenaTeam> losers);

    public void addLosers(Collection<ArenaTeam> losers);

    public void addLoser(ArenaTeam loser);

    public Set<ArenaTeam> getVictors();

    public Set<ArenaTeam> getLosers();

    public void removeLosers(Collection<ArenaTeam> teams);

    public void removeDrawers(Collection<ArenaTeam> teams);

    public void removeVictors(Collection<ArenaTeam> teams);

    public Set<ArenaTeam> getDrawers();

    public String toPrettyString();

    public boolean isUnknown();

    public boolean isDraw();

    public boolean isWon();

    public boolean isLost();

    public boolean isFinished();

    public boolean hasVictor();

    public WinLossDraw getResult();
}
