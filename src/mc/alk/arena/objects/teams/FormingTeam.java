package mc.alk.arena.objects.teams;

import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.controllers.MessageController;

import org.bukkit.entity.Player;



public class FormingTeam extends Team{
	Set<Player> joined_players = new HashSet<Player>();

	public FormingTeam(Player p, Set<Player> teammates) {
		super(p,teammates);
		joined_players.add(p);
	}

	public void joinTeam(Player p){joined_players.add(p);}
	public Set<Player> getJoinedPlayers() {return joined_players;}

	public boolean hasJoined(Player p) {return joined_players.contains(p);}
	public boolean isJoining(Player p) {return !hasJoined(p);}

	public Set<Player> getUnjoinedPlayers() {
		if (hasAllPlayers()) return null;
		Set<Player> ps = new HashSet<Player>(players);
		ps.removeAll(joined_players);
		return ps;
	}
	
	public void sendJoinedPlayersMessage(String message) {
		for (Player p: joined_players){
			MessageController.sendMessage(p, message);
		}
	}
	public void sendUnjoinedPlayersMessage(String message) {
		Set<Player> unjoined = getUnjoinedPlayers();
		if (unjoined == null)
			return;
		for (Player p: unjoined){
			MessageController.sendMessage(p, message);
		}
	}
	
	public boolean hasAllPlayers() {return joined_players.size() == players.size();}

}

