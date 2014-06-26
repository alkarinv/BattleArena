package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.ScoreMap;
import mc.alk.scoreboardapi.api.SAPIFactory;
import mc.alk.scoreboardapi.api.SEntry;
import mc.alk.scoreboardapi.api.SObjective;
import mc.alk.scoreboardapi.api.SScoreboard;
import mc.alk.scoreboardapi.api.STeam;
import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;
import mc.alk.scoreboardapi.scoreboard.bukkit.BObjective;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;


public class ArenaObjective implements SObjective, ScoreTracker{

    final protected ScoreMap<ArenaTeam> teamPoints = new ScoreMap<ArenaTeam>();
    final protected ScoreMap<ArenaPlayer> playerPoints = new ScoreMap<ArenaPlayer>();
    final protected SObjective o;

	public ArenaObjective(String name, String criteria) {
		this(name,criteria,name, SAPIDisplaySlot.SIDEBAR,50);
	}

	/**
	 *
	 * @param name Objective name
	 * @param criteria Objective criteria
	 * @param priority: lower priority means it has precedence
	 */
	public ArenaObjective(String name, String criteria, int priority) {
		this(name,criteria,name,SAPIDisplaySlot.SIDEBAR,priority);
	}

	public ArenaObjective(String name, String criteria, String displayName, SAPIDisplaySlot slot) {
		this(name,criteria,displayName,slot, 50);
	}

	public ArenaObjective(String name, String criteria, String displayName, SAPIDisplaySlot slot, int priority) {
		this(name,criteria,displayName,slot, priority,0);
	}

	public ArenaObjective(String id, String criteria, String displayName,
			SAPIDisplaySlot slot, int priority, int points) {

		o = (Defaults.TESTSERVER || !Defaults.USE_SCOREBOARD) ?
                SAPIFactory.createSAPIObjective(id, displayName, criteria, slot, priority) :
                SAPIFactory.createObjective(id,displayName, criteria,slot, priority);
		if (displayName != null){
			setDisplayName(displayName);}
	}

	public void setDisplaySlot(ArenaDisplaySlot sidebar) {
		o.setDisplaySlot(sidebar.toSAPI());
	}

	public Integer getPoints(ArenaTeam t) {
		return teamPoints.get(t);
	}

	public void setAllPoints(int points) {
		for (ArenaTeam t: teamPoints.keySet()){
			setPoints(t, points);
		}
		for (ArenaPlayer p: playerPoints.keySet()){
			setPoints(p, points);
		}
	}

	public void setAllPoints(Match match, int points){
		for (ArenaTeam t: match.getTeams()){
			if (o.isDisplayTeams()){
				setPoints(t, points);
			}
			if (o.isDisplayPlayers()){
				for (ArenaPlayer p : t.getPlayers()){
					setPoints(p, points);}
			}
		}
	}


	public Integer addPoints(ArenaTeam team, int points) {
		int oldPoints = teamPoints.getPoints(team);
		setPoints(team,points+oldPoints);
		return points+oldPoints;
	}

	public Integer addPoints(ArenaPlayer ap, int points) {
		int oldPoints = playerPoints.getPoints(ap);
		setPoints(ap,points+oldPoints);
		return points+oldPoints;
	}

	public Integer subtractPoints(ArenaTeam team, int points) {
		int oldPoints = teamPoints.getPoints(team);
		setPoints(team,oldPoints-points);
		return oldPoints-points;
	}

	public int subtractPoints(ArenaPlayer ap, int points) {
		int oldPoints = playerPoints.getPoints(ap);
		setPoints(ap,oldPoints-points);
		return oldPoints-points;
	}

	public List<ArenaTeam> getTeamLeaders() {
		return teamPoints.getLeaders();
	}

	public TreeMap<Integer, Collection<ArenaTeam>> getTeamRanks() {
		return teamPoints.getRankings();
	}

	public List<ArenaPlayer> getPlayerLeaders() {
		return playerPoints.getLeaders();
	}

	public TreeMap<Integer, Collection<ArenaPlayer>> getPlayerRanks() {
		return playerPoints.getRankings();
	}

	public MatchResult getMatchResult(Match match){
		TreeMap<Integer,Collection<ArenaTeam>> ranks = this.getTeamRanks();
		/// Deal with teams that haven't scored and possibly aren't inside the ranks
		HashSet<ArenaTeam> unfoundTeams = new HashSet<ArenaTeam>(match.getAliveTeams());
		for (Collection<ArenaTeam> t : ranks.values()){
			unfoundTeams.removeAll(t);
		}
		Collection<ArenaTeam> zeroes = ranks.get(0);
		if (zeroes != null){
			zeroes.addAll(unfoundTeams);
		} else {
			ranks.put(0, unfoundTeams);
		}

		MatchResult result = new MatchResult();
		if (ranks == null || ranks.isEmpty())
			return result;
		if (ranks.size() == 1){ /// everyone tied obviously
			for (Collection<ArenaTeam> col : ranks.values()){
				result.setDrawers(col);}
		} else {
			boolean first = true;
			for (Integer key : ranks.keySet()){
				Collection<ArenaTeam> col = ranks.get(key);
				if (first){
					result.setVictors(col);
					first = false;
				} else {
					result.addLosers(col);
				}
			}
		}
        result.setRanking(ranks);
        return result;
	}

	public Integer setPoints(ArenaPlayer p, int points) {
		o.setPoints(p.getName(), points);
		return playerPoints.setPoints(p, points);
	}

	public Integer setPoints(ArenaTeam t, int points) {
		o.setPoints(t.getIDString(), points);
		return teamPoints.setPoints(t, points);
	}

	@Override
	public List<ArenaTeam> getLeaders() {
		return getTeamLeaders();
	}

	@Override
	public TreeMap<?, Collection<ArenaTeam>> getRanks() {
		return getTeamRanks();
	}

	@Override
	public void setScoreBoard(ArenaScoreboard scoreboard) {
		scoreboard.setObjectiveScoreboard(this);
	}

	@Override
	public void setDisplayName(String displayName) {
		o.setDisplayName(displayName);
	}

    @Override
    public String getDisplayNameSuffix() {
        return o.getDisplayNameSuffix();
    }

    @Override
	public void setDisplayNameSuffix(String suffix) {
		o.setDisplayNameSuffix(suffix);
	}

    @Override
    public String getDisplayNamePrefix() {
        return o.getDisplayNamePrefix();
    }

    @Override
    public void setDisplayNamePrefix(String prefix) {
        o.setDisplayNamePrefix(prefix);
    }

    @Override
	public boolean setPoints(SEntry entry, int points) {
		return o.setPoints(entry, points);
	}

	@Override
	public SAPIDisplaySlot getDisplaySlot() {
		return o.getDisplaySlot();
	}

	@Override
	public int getPriority() {
		return o.getPriority();
	}

	@Override
	public void setDisplaySlot(SAPIDisplaySlot slot) {
		o.setDisplaySlot(slot);
	}

	@Override
	public String getID() {
		return o.getID();
	}

	@Override
	public String getDisplayName() {
		return o.getDisplayName();
	}

    @Override
    public String getBaseDisplayName() {
        return o.getBaseDisplayName();
    }

    @Override
	public boolean setTeamPoints(STeam t, int points) {
		return o.setTeamPoints(t, points);
	}

	@Override
	public boolean setPoints(String id, int points) {
		return o.setPoints(id, points);
	}

	@Override
	public SEntry addEntry(String id, int points) {
		return o.addEntry(id, points);
	}

	@Override
	public void setDisplayPlayers(boolean b) {
		o.setDisplayPlayers(b);
	}

	@Override
	public void setDisplayTeams(boolean display) {
		o.setDisplayTeams(display);
	}

	@Override
	public void setScoreBoard(SScoreboard scoreboard) {
		o.setScoreBoard(scoreboard);
		scoreboard.registerNewObjective(this);
	}

    @Override
    public int getPoints(String id) {
        return o.getPoints(id);
    }

    @Override
    public int getPoints(SEntry e) {
        return o.getPoints(e);
    }

    @Override
	public String toString(){
		return o.toString();
	}

	@Override
	public boolean isDisplayTeams() {
		return o.isDisplayTeams();
	}

	@Override
	public boolean isDisplayPlayers() {
		return o.isDisplayPlayers();
	}

	@Override
	public SScoreboard getScoreboard() {
		return o.getScoreboard();
	}

	@Override
	public SEntry addEntry(OfflinePlayer player, int points) {
		return o.addEntry(player, points);
	}

	@Override
	public SEntry removeEntry(OfflinePlayer player) {
		return o.removeEntry(player);
	}

	@Override
	public SEntry removeEntry(String id) {
		return o.removeEntry(id);
	}

	@Override
	public boolean addEntry(SEntry entry, int defaultPoints) {
		return o.addEntry(entry, defaultPoints);
	}

	@Override
	public SEntry removeEntry(SEntry entry) {
		return o.removeEntry(entry);
	}

	@Override
	public boolean contains(SEntry e) {
		return o.contains(e);
	}

	@Override
	public STeam addTeam(String id, int points) {
		return o.addTeam(id, points);
	}

	@Override
	public boolean addTeam(STeam entry, int points) {
		return o.addTeam(entry, points);
	}

    public void setDisplayName(String displayNamePrefix, String displayName, String displayNameSuffix, STeam team){
        if (o instanceof BObjective) {
            ((BObjective) o).setDisplayName(displayNamePrefix, displayName,displayNameSuffix, team);
        }
    }

    @Override
    public void initPoints(List<SEntry> entries, List<Integer> points) {
        o.initPoints(entries, points);
    }
}
