package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class BukkitScoreboard extends ArenaScoreboard{
	Scoreboard board;

	DisplaySlot sideSlot = DisplaySlot.SIDEBAR;
	DisplaySlot listSlot = DisplaySlot.PLAYER_LIST;

	Objective main;
	Objective secondary;

	public BukkitScoreboard(MatchParams params) {
		super(params);
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		if (solo){

		}
	}

	@Override
	public void clear(){
		super.clear();
		for (DisplaySlot ds: DisplaySlot.values()){
			board.clearSlot(ds);
		}
	}

	public static DisplaySlot getDisplaySlot(ArenaDisplaySlot slot){
		switch(slot){
		case BELOW_NAME:
			return DisplaySlot.BELOW_NAME;
		case PLAYER_LIST:
			return DisplaySlot.PLAYER_LIST;
		case SIDEBAR:
			return DisplaySlot.SIDEBAR;
		case NONE:
		default:
			return null;
		}
	}
	@Override
	public void addObjective(ArenaObjective objective) {
		super.addObjective(objective);
		secondary = main;
		if (secondary != null && objective.getDisplaySlot() != ArenaDisplaySlot.PLAYER_LIST)
			secondary.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		Objective o = getObjective(objective);
		main = o;
		DisplaySlot slot = getDisplaySlot(objective.getDisplaySlot());
		o.setDisplayName(objective.getDisplayName());

		if (slot != null)
			o.setDisplaySlot(slot);
	}

	public Objective getObjective(ArenaObjective objective){
		Objective o = board.getObjective(objective.getName());
		if (o == null){
			o = board.registerNewObjective(objective.getName(), objective.getCriteria());
		}
		return o;
	}

	public Scoreboard getScoreboard(){
		return board;
	}

	@Override
	public void addTeam(ArenaTeam team) {
		try{
			Team t = board.registerNewTeam(team.geIDString());
			t.setDisplayName(team.getDisplayName());
			for (Player p: team.getBukkitPlayers()){
				t.addPlayer(p);
				t.setPrefix(team.getTeamChatColor()+"");
				if (p.isOnline())
					p.setScoreboard(board);

				Objective o = getMainObjective();
				Score sc = o.getScore(p);
				sc.setScore(0);
				o.setDisplaySlot(DisplaySlot.SIDEBAR);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void addedToTeam(ArenaTeam team, ArenaPlayer player) {
		Team t = board.getTeam(team.geIDString());
		if (t!=null){
			t.addPlayer(player.getPlayer());
			t.setPrefix(team.getTeamChatColor()+""); /// need to set after every team????!!
			player.getPlayer().setScoreboard(board);
		}
	}

	@Override
	public void setDead(ArenaTeam team, ArenaPlayer p) {
		board.resetScores(p.getPlayer());
	}

	@Override
	public void leaving(ArenaTeam t, ArenaPlayer player){
		if (t != null){
			removedFromTeam(t,player);
		}
		board.resetScores(player.getPlayer());
		player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	private Objective getMainObjective() {
		return main;
	}


	@Override
	public void removeTeam(ArenaTeam team) {
		Team t = board.getTeam(team.geIDString());
		if (t!=null){
			Scoreboard mains = Bukkit.getScoreboardManager().getMainScoreboard();
			for (Player p: team.getBukkitPlayers()){
				if (p.isOnline()){
					p.setScoreboard(mains);
				}
				t.removePlayer(p);
			}
		}
	}

	@Override
	public void removedFromTeam(ArenaTeam team, ArenaPlayer player) {
		Team t = board.getTeam(team.geIDString());
		Scoreboard mains = Bukkit.getScoreboardManager().getMainScoreboard();
		if (t!=null){
			for (Player p: team.getBukkitPlayers()){
				if (p.isOnline()){
					p.setScoreboard(mains);
				}
				t.removePlayer(p);
			}
		}
	}



	@Override
	public void setPoints(ArenaObjective objective, ArenaTeam team, int points) {
		super.setPoints(objective, team, points);
		if (!solo){
			Objective o = getObjective(objective);
			Score sc = o.getScore(Bukkit.getOfflinePlayer(team.getScoreboardDisplayName()));
			sc.setScore(points);
		}
	}

	@Override
	public void setPoints(ArenaObjective objective, ArenaPlayer player, int points) {
		super.setPoints(objective, player, points);
		Objective o = getObjective(objective);
		Score sc = o.getScore(player.getPlayer());
		sc.setScore(points);
	}
}
