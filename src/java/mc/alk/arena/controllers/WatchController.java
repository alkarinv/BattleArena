package mc.alk.arena.controllers;

import mc.alk.arena.controllers.containers.AreaContainer;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.PlayerSave;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.SpawnLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author alkarin
 */
public class WatchController{
    Map<UUID, PlayerSave> watchers = new HashMap<UUID, PlayerSave>();

    public boolean watch(ArenaPlayer player, Arena arena) {
        AreaContainer rc = arena.getVisitorRoom();
        if (rc == null || rc.getSpawns() == null || rc.getSpawn(0,0) == null)
            return false;
        SpawnLocation l = rc.getSpawn(0, 0);
        PlayerSave ps = watchers.get(player.getID());
        if (ps == null) {
            ps = new PlayerSave(player);
            watchers.put(player.getID(), ps);
            ps.setLocation(player.getLocation());
        }
        TeleportController.teleport(player, l.getLocation());
        return true;
    }

    public boolean hasWatcher(ArenaPlayer player) {
        return watchers.containsKey(player.getID());
    }

    public void leave(ArenaPlayer player) {
        PlayerSave ps = watchers.remove(player.getID());
        if (ps != null && ps.getLocation() != null) {
            TeleportController.teleport(player, ps.getLocation());
        }
    }
}
