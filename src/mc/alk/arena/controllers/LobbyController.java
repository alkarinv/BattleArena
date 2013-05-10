package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Location;

public enum LobbyController {
	INSTANCE;

	Map<ArenaType,LobbyContainer> lobbies = new HashMap<ArenaType,LobbyContainer>();

	public boolean joinLobby(ArenaType type, ArenaTeam team) {
		LobbyContainer lobby = lobbies.get(type);
		if (lobby == null)
			return false;
		return lobby.teamJoining(team);
	}

	private LobbyContainer getOrCreate(ArenaType type) {
		LobbyContainer lobby = lobbies.get(type);
		if (lobby == null){
			lobby = new LobbyContainer(ParamController.getMatchParamCopy(type.getName()));
			lobbies.put(type, lobby);
		}
		return lobby;
	}

	public static void addLobby(ArenaType type, int index, Location loc) {
		LobbyContainer lobby = INSTANCE.getOrCreate(type);
		lobby.addSpawn(index,loc);
	}

	public static boolean hasLobby(ArenaType type) {
		return INSTANCE.lobbies.containsKey(type);
	}

	public static LobbyContainer getLobby(ArenaType type) {
		return INSTANCE.lobbies.get(type);
	}

	public static Location getLobbySpawn(int index, ArenaType type, boolean randomRespawn) {
		return INSTANCE.getSpawn(index,type, randomRespawn);
	}

	private Location getSpawn(int index, ArenaType type, boolean randomRespawn) {
		LobbyContainer lobby = lobbies.get(type);
		if (lobby == null)
			return null;
		return lobby.getSpawn(index, LocationType.LOBBY, randomRespawn);
	}

	public static void setLobbyParams(MatchParams mp) {
		LobbyContainer lobby = INSTANCE.getOrCreate(mp.getType());
		lobby.setParams(mp);
	}

	public static Collection<LobbyContainer> getLobbies() {
		return INSTANCE.lobbies.values();
	}

	public static void cancelAll() {
		synchronized(INSTANCE.lobbies){
			for (LobbyContainer lc : INSTANCE.lobbies.values()){
				lc.cancel();
			}
		}
	}

	public static void leaveLobby(MatchParams params, ArenaPlayer p) {
		LobbyContainer lobby = INSTANCE.lobbies.get(params.getType());
		if (lobby == null)
			return;
		lobby.playerLeaving(p);

	}
}
