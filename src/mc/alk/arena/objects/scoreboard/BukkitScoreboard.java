package mc.alk.arena.objects.scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.ServerUtil;

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

	final boolean colorPlayerNames;
	HashMap<DisplaySlot,ArenaObjective> slots = new HashMap<DisplaySlot,ArenaObjective>();
	final boolean solo;
	public BukkitScoreboard(Match match, MatchParams params) {
		super(match, params);
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		this.colorPlayerNames = Defaults.USE_COLORNAMES;
		this.solo = params.getMaxTeamSize() == 1;
	}

	@Override
	public void clear(){
		super.clear();
		for (DisplaySlot ds: DisplaySlot.values()){
			board.clearSlot(ds);
		}
	}

	public static DisplaySlot convertDisplaySlot(ArenaDisplaySlot slot){
		switch (slot){
		case BELOW_NAME:
			return DisplaySlot.BELOW_NAME;
		case PLAYER_LIST:
			return DisplaySlot.PLAYER_LIST;
		case SIDEBAR:
			return DisplaySlot.SIDEBAR;
		case NONE:
			return null;
		default:
			return null;
		}
	}
	public static ArenaDisplaySlot convertToArenaDisplaySlot(DisplaySlot slot){
		switch (slot){
		case BELOW_NAME:
			return ArenaDisplaySlot.BELOW_NAME;
		case PLAYER_LIST:
			return ArenaDisplaySlot.PLAYER_LIST;
		case SIDEBAR:
			return ArenaDisplaySlot.SIDEBAR;
		default:
			return ArenaDisplaySlot.NONE;
		}
	}

	@Override
	public void addObjective(ArenaObjective objective) {
		super.addObjective(objective);
		DisplaySlot oldSlot = convertDisplaySlot(objective.getDisplaySlot());
//		Log.debug("-----  adding objective " + objective.getName() +
//				"    slot =  " + objective.getDisplaySlot() +"   --"+objective.getPriority()+"-- " + slots.size());

		Collection<ArenaPlayer> players = match.getPlayers();
		Collection<Player> ps = new ArrayList<Player>(players.size());
		for (ArenaPlayer s : players){
			Player p = ServerUtil.findPlayerExact(s.getName());
			if (p!=null && p.isOnline()){
				ps.add(p);}
		}
		if (slots.isEmpty()){
			for (Player p : ps){
				if (p!=null && p.isOnline()){
					p.setScoreboard(board);}}
		}

		boolean created = false;
		if (slots.containsKey(oldSlot)){
			ArenaObjective movingObjective;
			if (slots.get(oldSlot).getPriority() <= objective.getPriority()){
				movingObjective = slots.get(oldSlot);
				makeBukkitObjective(objective);
				slots.put(oldSlot, objective);
				created = true;
			} else {
				movingObjective = objective;
			}
			DisplaySlot moveToDS = convertDisplaySlot(movingObjective.getDisplaySlot().swap());
//			Log.debug("  ### MovingObjective = " + movingObjective.getName() +"   " + movingObjective.getPriority());
			if (moveToDS != null){
				ArenaObjective inPlaceObjective = slots.get(moveToDS);
				if (inPlaceObjective == null || inPlaceObjective.getPriority() < movingObjective.getPriority()){
					if (objective == movingObjective){
						created = true;
						objective.setDisplaySlot(movingObjective.getDisplaySlot().swap());
						makeBukkitObjective(movingObjective);
						slots.put(moveToDS, movingObjective);
//						Log.debug(" 2@@@@ movingObjective has " + oldSlot+"  - "  +movingObjective.getPriority()+" " + movingObjective.getName());
//						if (inPlaceObjective != null)
//							Log.debug(" 2@@@@ already in has " + moveToDS +"  - " + inPlaceObjective.getPriority()+" " + inPlaceObjective.getName());
					}
				}
			}
		} else {
			Objective o = makeBukkitObjective(objective);
			slots.put(o.getDisplaySlot(), objective);
			created = true;
		}

		if (created){
			Objective o = makeBukkitObjective(objective);
			slots.put(o.getDisplaySlot(), objective);
			for (Player p : ps){
				Score sc = o.getScore(p);
				sc.setScore(1);
				sc.setScore(0);
			}
		}
	}

	public Objective makeBukkitObjective(ArenaObjective objective){
		Objective o = board.getObjective(objective.getName());
		if (o == null){
			o = board.registerNewObjective(objective.getName(), objective.getCriteria());
		}
		o.setDisplayName(objective.getDisplayName());
		o.setDisplaySlot(convertDisplaySlot(objective.getDisplaySlot()));
		return o;
	}


	public Scoreboard getScoreboard(){
		return board;
	}

	@Override
	public void addTeam(ArenaTeam team) {
		try{
			Team t = board.registerNewTeam(team.geIDString());
//			Log.debug(" addTeam " + team.getName() +"   " + t.getDisplayName());
			t.setDisplayName(getTeamName(team));
			for (Player p: team.getBukkitPlayers()){
				if (!p.isOnline())
					continue;
				t.addPlayer(p);
				if (colorPlayerNames)
					t.setPrefix(MessageUtil.colorChat(team.getTeamChatColor()+""));
				if (p.isOnline())
					p.setScoreboard(board);
				synchronized(slots){
					for (Entry<DisplaySlot,ArenaObjective> entry: slots.entrySet()){
						Objective o = board.getObjective(entry.getKey());
						Score sc = o.getScore(p);
						sc.setScore(0);
						o.setDisplaySlot(entry.getKey());
					}
				}
			}
		} catch(Exception e){
			Log.printStackTrace(e);
		}
	}
	public String getTeamName(ArenaTeam team){
		String name = team.size() > 1 || team.size()==0 ? team.getName() : team.getScoreboardDisplayName();
		if (name.length() > 16)
			name = name.substring(0, 16);
		name = MessageUtil.colorChat(name);
		return name;
	}

	@Override
	public void addedToTeam(ArenaTeam team, ArenaPlayer player) {
		Team t = board.getTeam(team.geIDString());
//		Log.debug(" addedToTeam " + team.getName() +"   " + t.getDisplayName() +"   " + player.getName());

		if (t!=null){
			t.setDisplayName(getTeamName(team));
			t.addPlayer(player.getPlayer());
			if (colorPlayerNames)
				t.setPrefix(MessageUtil.colorChat(team.getTeamChatColor()+"")); /// need to set after every player added to team????!!
			/// Joining through commandSigns will throw an error here as the Player from the event is
			/// not an abstract player.  So reget the player from bukkit
			Player p = ServerUtil.findPlayerExact(player.getName());
			if (p != null && p.isOnline()){
				p.setScoreboard(board);
				player.setPlayer(p);
				setAllPoints(player,1);
				setAllPoints(player,0);
			}
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
		Player p = Bukkit.getPlayerExact(player.getName());
		if (p!=null){
			board.resetScores(p);
			p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		} else {
			board.resetScores(Bukkit.getOfflinePlayer(player.getName()));
		}
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


	private void setAllPoints(ArenaPlayer p, int points) {
		synchronized(slots){
			for (Entry<DisplaySlot,ArenaObjective> entry: slots.entrySet()){
				setPoints(entry.getValue(), p, points);
			}
		}
	}

	@Override
	public void setPoints(ArenaObjective objective, ArenaTeam team, int points) {
		super.setPoints(objective, team, points);
		if (!solo){
			Objective o = board.getObjective(objective.getName());
			if (o == null)
				return;
			Score sc = o.getScore(Bukkit.getOfflinePlayer(MessageUtil.colorChat(team.getScoreboardDisplayName())));
			sc.setScore(points);
		}
	}

	@Override
	public void setPoints(ArenaObjective objective, ArenaPlayer player, int points) {
		super.setPoints(objective, player, points);
		Objective o = board.getObjective(objective.getName());
		if (o == null)
			return;
		Score sc = o.getScore(player.getPlayer());
		sc.setScore(points);
	}

}
