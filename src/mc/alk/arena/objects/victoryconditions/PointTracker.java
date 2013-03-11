package mc.alk.arena.objects.victoryconditions;

import java.util.List;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesLeaderRanking;
import mc.alk.arena.util.ScoreMap;

import org.bukkit.entity.Player;

public class PointTracker extends VictoryCondition implements DefinesLeaderRanking{
	ScoreMap<Team> teamPoints = new ScoreMap<Team>();
	ScoreMap<ArenaPlayer> playerPoints = new ScoreMap<ArenaPlayer>();

	public PointTracker(Match match) {
		super(match);
		for (Team t: match.getTeams()){
			teamPoints.put(t, 0);}
	}

	@Override
	public List<Team> getLeaders() {
		return teamPoints.getLeaders();
	}

	@Override
	public List<Team> getRankings() {
		return teamPoints.getRankings();
	}

	public List<ArenaPlayer> getPlayerLeaders() {
		return playerPoints.getLeaders();
	}

	public List<ArenaPlayer> getPlayerRankings() {
		return playerPoints.getRankings();
	}

	public Integer getPoints(Team team) {
		return teamPoints.containsKey(team) ? teamPoints.get(team) : 0;
	}

	public Integer addPoints(Team team, int points) {
		return teamPoints.addPoints(team, points);
	}

	public Integer subtractPoints(Team team, int points) {
		return addPoints(team,-points);
	}

	public Integer getPoints(ArenaPlayer player) {
		return playerPoints.containsKey(player) ? playerPoints.get(player) : 0;
	}

	public Integer addPoints(Player player, int points) {
		return playerPoints.addPoints(BattleArena.toArenaPlayer(player),points);
	}

	public Integer addPoints(ArenaPlayer player, int points) {
		return playerPoints.addPoints(player,points);
	}

	public Integer subtractPoints(ArenaPlayer player, int points) {
		return playerPoints.addPoints(player,-points);
	}

	public Integer subtractPoints(Player player, int points) {
		return playerPoints.addPoints(BattleArena.toArenaPlayer(player),-points);
	}

	public Map<Team,Integer> getTeamPoints(){
		return teamPoints;
	}

	public Map<ArenaPlayer,Integer> getPlayerPoints(){
		return playerPoints;
	}

	public boolean containsKey(Team team) {
		return teamPoints.containsKey(team);
	}

	public boolean containsKey(ArenaPlayer player) {
		return playerPoints.containsKey(player);
	}

	public boolean containsKey(Player player) {
		return playerPoints.containsKey(BattleArena.toArenaPlayer(player));
	}
}
