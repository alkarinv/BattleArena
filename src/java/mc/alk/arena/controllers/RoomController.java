package mc.alk.arena.controllers;

import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.spawns.SpawnLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RoomController {
    INSTANCE;

    final Map<ArenaType,LobbyContainer> lobbies = new HashMap<ArenaType,LobbyContainer>();
    final Map<String,RoomContainer> waitrooms = new HashMap<String,RoomContainer>();
    final Map<String,RoomContainer> spectate = new HashMap<String,RoomContainer>();

    private RoomContainer _getOrCreateSpectate(Arena arena) {
        RoomContainer room = spectate.get(arena.getName());
        if (room == null){
            if (arena.getSpectatorRoom() == null){
                MatchParams mp = new MatchParams(arena.getArenaType());
                mp.setParent(arena.getParams());
                room = new RoomContainer("s_"+arena.getName()+"",mp, LocationType.SPECTATE);
            } else {
                room = arena.getSpectatorRoom();
            }
            spectate.put(arena.getName(), room);
        }
        return room;
    }

    private RoomContainer getOrCreate(Arena arena) {
        RoomContainer room = waitrooms.get(arena.getName());
        if (room == null){
            if (arena.getWaitroom() == null) {
                MatchParams mp = new MatchParams(arena.getArenaType());
                mp.setParent(arena.getParams());
                room = new RoomContainer("wr_" + arena.getName() + "", mp, LocationType.WAITROOM);
            } else {
                room = arena.getWaitroom();
            }
            waitrooms.put(arena.getName(), room);
        }
        return room;
    }

    private RoomContainer getOrCreate(ArenaType type) {
        LobbyContainer lobby = lobbies.get(type);
        if (lobby == null) {
            MatchParams mp = new MatchParams(type);
            mp.setParent(ParamController.getMatchParams(type));
            lobby = new LobbyContainer("lb_" + type.getName(),
                    mp, LocationType.LOBBY);
            lobbies.put(type, lobby);
        }
        return lobby;
    }

    public static void addLobby(ArenaType type, int index, int spawnIndex, SpawnLocation location) {
        RoomContainer room = INSTANCE.getOrCreate(type);
        room.setSpawnLoc(index,spawnIndex, location);
    }

    public static void addWaitRoom(Arena arena, int index, int spawnIndex, SpawnLocation location) {
        RoomContainer room = INSTANCE.getOrCreate(arena);
        if (arena.getWaitroom() == null)
            arena.setWaitRoom(room);
        room.setSpawnLoc(index,spawnIndex, location);
    }

    public static void addSpectate(Arena arena, int index, int spawnIndex, SpawnLocation location) {
        RoomContainer room = INSTANCE._getOrCreateSpectate(arena);
        if (arena.getSpectatorRoom() == null)
            arena.setSpectate(room);
        room.setSpawnLoc(index,spawnIndex, location);
    }

    public static boolean hasLobby(ArenaType type) {
        return INSTANCE.lobbies.containsKey(type);
    }

    public static boolean hasWaitroom(Arena arena) {
        return INSTANCE.waitrooms.containsKey(arena.getName());
    }

    public static LobbyContainer getLobby(ArenaType type) {
        return INSTANCE.lobbies.get(type);
    }

    public static RoomContainer getWaitroom(Arena arena) {
        return INSTANCE.waitrooms.get(arena.getName());
    }

    public static RoomContainer getSpectate(Arena arena) {
        return INSTANCE.spectate.get(arena.getName());
    }

    public static RoomContainer getOrCreateWaitroom(Arena arena) {
        return INSTANCE.getOrCreate(arena);
    }

    public static RoomContainer getOrCreateSpectate(Arena arena) {
        return INSTANCE._getOrCreateSpectate(arena);
    }

    public static SpawnLocation getLobbySpawn(int index, ArenaType type, boolean randomRespawn) {
        return INSTANCE.getSpawn(index,type, randomRespawn);
    }

    private SpawnLocation getSpawn(int index, ArenaType type, boolean randomRespawn) {
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

    public static void updateRoomParams(MatchParams matchParams) {
        synchronized(INSTANCE.lobbies) {
            LobbyContainer lc = INSTANCE.lobbies.get(matchParams.getType());
            if (lc != null) {
                lc.getParams().setParent(matchParams);
            }
        }
    }

    public static void updateArenaParams(Arena arena) {
        MatchParams arenaParams = arena.getParams();
        List<RoomContainer> rcs = new ArrayList<RoomContainer>();
        rcs.add(arena.getWaitroom());
        rcs.add(arena.getSpectatorRoom());
        for (RoomContainer rc : rcs){
            if (rc == null)
                continue;
            rc.getParams().setParent(arenaParams);
        }
    }
}
