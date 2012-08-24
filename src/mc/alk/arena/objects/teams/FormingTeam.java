package mc.alk.arena.objects.teams;

import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.objects.ArenaPlayer;



public class FormingTeam extends Team{
	Set<ArenaPlayer> joined_players = new HashSet<ArenaPlayer>();

	public FormingTeam(ArenaPlayer p, Set<ArenaPlayer> teammates) {
		super(p,teammates);
		joined_players.add(p);
	}

	public void joinTeam(ArenaPlayer p){joined_players.add(p);}
	public Set<ArenaPlayer> getJoinedPlayers() {return joined_players;}

	public boolean hasJoined(ArenaPlayer p) {return joined_players.contains(p);}
	public boolean isJoining(ArenaPlayer p) {return !hasJoined(p);}

	public Set<ArenaPlayer> getUnjoinedPlayers() {
		if (hasAllPlayers()) return null;
		Set<ArenaPlayer> ps = new HashSet<ArenaPlayer>(players);
		ps.removeAll(joined_players);
		return ps;
	}
	
	public void sendJoinedPlayersMessage(String message) {
		for (ArenaPlayer p: joined_players){
			MessageController.sendMessage(p, message);
		}
	}
	public void sendUnjoinedPlayersMessage(String message) {
		Set<ArenaPlayer> unjoined = getUnjoinedPlayers();
		if (unjoined == null)
			return;
		for (ArenaPlayer p: unjoined){
			MessageController.sendMessage(p, message);
		}
	}
	
	public boolean hasAllPlayers() {return joined_players.size() == players.size();}

}

