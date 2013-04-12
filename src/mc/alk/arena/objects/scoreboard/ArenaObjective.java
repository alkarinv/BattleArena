package mc.alk.arena.objects.scoreboard;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.ScoreMap;

import org.bukkit.entity.Player;

public class ArenaObjective implements ScoreTracker{
	ScoreMap<ArenaTeam> teamPoints = new ScoreMap<ArenaTeam>();
	ScoreMap<ArenaPlayer> playerPoints = new ScoreMap<ArenaPlayer>();
	ArenaScoreboard scoreboard =null;

	final String name;
	String criteria;
	String displayName;

	ArenaDisplaySlot slot = ArenaDisplaySlot.SIDEBAR;
	boolean displayPlayers = true;
	boolean displayTeams = true;

	public ArenaObjective(String name, String criteria) {
		this.name = name;
		this.criteria = criteria;
	}

	public String getName(){
		return name;
	}

	public String getCriteria() {
		return criteria;
	}

	public String getDisplayName(){
		return displayName == null ? name : displayName;
	}

	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}

	public Integer getPoints(ArenaTeam team) {
		return teamPoints.containsKey(team) ? teamPoints.get(team) : 0;
	}

	public Integer addPoints(ArenaTeam team, int points) {
		Integer totalpoints = teamPoints.addPoints(team, points);
		if (displayTeams && scoreboard != null)
			scoreboard.setPoints(this,team,totalpoints);

		return totalpoints;
	}

	public Integer subtractPoints(ArenaTeam team, int points) {
		Integer totalpoints = addPoints(team,-points);
		if (displayTeams && scoreboard != null)
			scoreboard.setPoints(this,team,totalpoints);

		return totalpoints;
	}

	public Integer setPoints(ArenaTeam team, int points) {
		Integer oldpoints = teamPoints.setPoints(team,points);
		if (displayTeams && scoreboard != null)
			scoreboard.setPoints(this,team,points);

		return oldpoints;
	}

	public Integer getPoints(ArenaPlayer player) {
		return playerPoints.containsKey(player) ? playerPoints.get(player) : 0;
	}

	public Integer addPoints(Player player, int points) {
		return playerPoints.addPoints(BattleArena.toArenaPlayer(player),points);
	}

	public Integer setPoints(Player player, int points) {
		return playerPoints.setPoints(BattleArena.toArenaPlayer(player),points);
	}

	public Integer addPoints(ArenaPlayer player, int points) {
		Integer totalpoints = playerPoints.addPoints(player,points);
		if (displayPlayers && scoreboard != null)
			scoreboard.setPoints(this,player,totalpoints);
		return totalpoints;
	}

	public Integer subtractPoints(Player player, int points) {
		return playerPoints.addPoints(BattleArena.toArenaPlayer(player),-points);
	}

	public Integer subtractPoints(ArenaPlayer player, int points) {
		Integer totalpoints = playerPoints.addPoints(player,-points);
		if (displayPlayers && scoreboard != null)
			scoreboard.setPoints(this,player,totalpoints);
		return totalpoints;
	}

	public Integer setPoints(ArenaPlayer player, int points) {
		playerPoints.setPoints(player,points);
		if (displayPlayers && scoreboard != null)
			scoreboard.setPoints(this,player,points);

		return points;
	}

	public Map<ArenaTeam,Integer> getTeamPoints(){
		return teamPoints;
	}

	public Map<ArenaPlayer,Integer> getPlayerPoints(){
		return playerPoints;
	}

	public boolean containsKey(ArenaTeam team) {
		return teamPoints.containsKey(team);
	}

	public boolean containsKey(ArenaPlayer player) {
		return playerPoints.containsKey(player);
	}

	public boolean containsKey(Player player) {
		return playerPoints.containsKey(BattleArena.toArenaPlayer(player));
	}

	@Override
	public void setScoreBoard(ArenaScoreboard scoreboard) {
		this.scoreboard = scoreboard;
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

	@Override
	public List<ArenaTeam> getLeaders() {
		return teamPoints.getLeaders();
	}

	@Override
	public TreeMap<?, Collection<ArenaTeam>> getRanks() {
		return teamPoints.getRankings();
	}

	public MatchResult getMatchResult(Match match){
		TreeMap<Integer,Collection<ArenaTeam>> ranks = this.getTeamRanks();
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
		return result;
	}

	public void setAllPoints(int points) {
		for (ArenaTeam t: teamPoints.keySet()){
			this.setPoints(t, points);
		}
		for (ArenaPlayer p: playerPoints.keySet()){
			this.setPoints(p, points);
		}
	}
	public void setAllPoints(Match match, int points){
		for (ArenaTeam t: match.getTeams()){
			setPoints(t, points);
			for (ArenaPlayer p : t.getPlayers()){
				setPoints(p, points);}
		}
	}
	public void setDisplaySlot(ArenaDisplaySlot slot) {
		this.slot = slot;
	}

	public ArenaDisplaySlot getDisplaySlot() {
		return this.slot;
	}

	public void setDisplayPlayers(boolean b) {
		this.displayPlayers = b;
	}

	public void setDisplayTeams(boolean b) {
		this.displayTeams = b;
	}
}
