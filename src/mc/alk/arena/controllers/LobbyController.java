package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.controllers.containers.AbstractAreaContainer;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Location;

public enum LobbyController {
	INSTANCE;

	Map<ArenaType,LobbyContainer> lobbies = new HashMap<ArenaType,LobbyContainer>();
	Map<Arena,RoomContainer> waitrooms = new HashMap<Arena,RoomContainer>();

	public boolean joinLobby(ArenaType type, ArenaTeam team) {
		AbstractAreaContainer lobby = lobbies.get(type);
		if (lobby == null)
			return false;
		return lobby.teamJoining(team);
	}

	public boolean joinWaitroom(Arena arena, ArenaTeam team) {
		AbstractAreaContainer wr = getOrCreate(arena);
		return wr.teamJoining(team);
	}

	private RoomContainer getOrCreate(Arena arena) {
		RoomContainer lobby = waitrooms.get(arena);
		if (lobby == null){
			lobby = new RoomContainer(ParamController.getMatchParamCopy(arena.getArenaType()), LocationType.ARENA);
			waitrooms.put(arena, lobby);
			arena.setWaitRoom(lobby);
		}
		return lobby;
	}

	private RoomContainer getOrCreate(ArenaType type) {
		LobbyContainer lobby = lobbies.get(type);
		if (lobby == null){
			lobby = new LobbyContainer(ParamController.getMatchParamCopy(type), LocationType.LOBBY);
			lobbies.put(type, lobby);
		}
		return lobby;
	}

	public static void addLobby(ArenaType type, int index, Location location) {
		RoomContainer lobby = INSTANCE.getOrCreate(type);
		lobby.setSpawnLoc(index,location);
	}
	public static void addWaitRoom(Arena arena, int index, Location location) {
		RoomContainer lobby = INSTANCE.getOrCreate(arena);
		lobby.setSpawnLoc(index,location);
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
		RoomContainer lobby = lobbies.get(type);
		if (lobby == null)
			return null;
		return lobby.getSpawn(index, randomRespawn);
	}

	public static void setLobbyParams(MatchParams mp) {
		RoomContainer lobby = INSTANCE.getOrCreate(mp.getType());
		lobby.setParams(mp);
	}

	public static Collection<LobbyContainer> getLobbies() {
		return INSTANCE.lobbies.values();
	}

	public static void cancelAll() {
		synchronized(INSTANCE.lobbies){
			for (RoomContainer lc : INSTANCE.lobbies.values()){
				lc.cancel();
			}
		}
	}

	public static void leaveLobby(MatchParams params, ArenaPlayer p) {
		RoomContainer lobby = INSTANCE.lobbies.get(params.getType());
		if (lobby == null)
			return;
		lobby.playerLeaving(p);

	}



}
