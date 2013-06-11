package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.controllers.containers.LobbyWRContainer;
import mc.alk.arena.controllers.containers.PlayerContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;

import org.bukkit.Location;

public enum LobbyController {
	INSTANCE;

	Map<ArenaType,LobbyWRContainer> lobbies = new HashMap<ArenaType,LobbyWRContainer>();
	Map<Arena,LobbyWRContainer> waitrooms = new HashMap<Arena,LobbyWRContainer>();

	public boolean joinLobby(ArenaType type, ArenaTeam team) {
		PlayerContainer lobby = lobbies.get(type);
		Log.debug(type+ "  Joining lobby = " + lobby +"   team = " + team);
		if (lobby == null)
			return false;
		return lobby.teamJoining(team);
	}

	public boolean joinWaitroom(Arena arena, ArenaTeam team) {
		PlayerContainer wr = getOrCreate(arena);
		Log.debug("Joining waitroom  arnea=" + arena.getName() +"   t=" + team +"   wr="+ wr);
		return wr.teamJoining(team);
	}

	private LobbyWRContainer getOrCreate(Arena arena) {
		LobbyWRContainer lobby = waitrooms.get(arena);
		if (lobby == null){
			lobby = new LobbyWRContainer(ParamController.getMatchParamCopy(arena.getArenaType()));
			waitrooms.put(arena, lobby);
			arena.setWaitRoom(lobby);
		}
		return lobby;
	}

	private LobbyWRContainer getOrCreate(ArenaType type) {
		LobbyWRContainer lobby = lobbies.get(type);
		if (lobby == null){
			lobby = new LobbyWRContainer(ParamController.getMatchParamCopy(type));
			lobbies.put(type, lobby);
		}
		return lobby;
	}

	public static void addLobby(ArenaType type, int index, Location location) {
		LobbyWRContainer lobby = INSTANCE.getOrCreate(type);
		lobby.addSpawn(index,location);
	}
	public static void addWaitRoom(Arena arena, int index, Location location) {
		LobbyWRContainer lobby = INSTANCE.getOrCreate(arena);
		lobby.addSpawn(index,location);
	}
	public static boolean hasLobby(ArenaType type) {
		return INSTANCE.lobbies.containsKey(type);
	}

	public static LobbyWRContainer getLobby(ArenaType type) {
		return INSTANCE.lobbies.get(type);
	}

	public static Location getLobbySpawn(int index, ArenaType type, boolean randomRespawn) {
		return INSTANCE.getSpawn(index,type, randomRespawn);
	}

	private Location getSpawn(int index, ArenaType type, boolean randomRespawn) {
		LobbyWRContainer lobby = lobbies.get(type);
		if (lobby == null)
			return null;
		return lobby.getSpawn(index, randomRespawn);
	}

	public static void setLobbyParams(MatchParams mp) {
		LobbyWRContainer lobby = INSTANCE.getOrCreate(mp.getType());
		lobby.setParams(mp);
	}

	public static Collection<LobbyWRContainer> getLobbies() {
		return INSTANCE.lobbies.values();
	}

	public static void cancelAll() {
		synchronized(INSTANCE.lobbies){
			for (LobbyWRContainer lc : INSTANCE.lobbies.values()){
				lc.cancel();
			}
		}
	}

	public static void leaveLobby(MatchParams params, ArenaPlayer p) {
		LobbyWRContainer lobby = INSTANCE.lobbies.get(params.getType());
		if (lobby == null)
			return;
		lobby.playerLeaving(p);

	}



}
