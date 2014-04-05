package mc.alk.arena.controllers.containers;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.events.players.ArenaPlayerTeleportEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.Util;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class AreaContainer extends AbstractAreaContainer{
    Map<UUID, Integer> respawnTimer = null;
    final LocationType type;

    public AreaContainer(String name, LocationType type){
        super(name);
        this.type = type;
    }

    public AreaContainer(String name, MatchParams params, LocationType type){
        super(name);
        setParams(params);
        this.type = type;
    }

    @Override
    public LocationType getLocationType() {
        return LocationType.LOBBY;
    }

    public void cancel() {
        players.clear();
    }

    public Collection<UUID> getInsidePlayers() {
        return players;
    }

    @Override
    public ArenaTeam getTeam(ArenaPlayer player) {
        return player.getTeam();
    }

    @ArenaEventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (event.isCancelled() || event.getClickedBlock() == null)
            return;
        /// Check to see if it's a sign
        if (event.getClickedBlock().getType().equals(Material.SIGN_POST)||
                event.getClickedBlock().getType().equals(Material.WALL_SIGN)){ /// Only checking for signs
            ArenaMatch.signClick(event,this);
        } else if (event.getClickedBlock().getType().equals(Defaults.READY_BLOCK)) {
            if (respawnTimer == null)
                respawnTimer = new HashMap<UUID, Integer>();
            if (respawnTimer.containsKey(PlayerUtil.getID(event.getPlayer()))){
                ArenaMatch.respawnClick(event,this, respawnTimer);
            } else {
//				readyClick(event);
            }
        }
    }

    public boolean hasSpawns() {
        return !spawns.isEmpty();
    }

    private boolean addPlayer(ArenaPlayer player) {
        boolean added = false;
        synchronized (this) {
            if(players.add(player.getID())){
                added = true;}
        }
        if (Defaults.DEBUG_TRACE) Log.info(player.getName() + "   !!!&2add  " + added + " t=" + player.getTeam());
        if (added){
            updateBukkitEvents(MatchState.ONENTER, player);
        }
        return added;
    }

    private boolean removePlayer(ArenaPlayer player) {
        boolean removed = false;

        synchronized (this) {
            if (players.remove(player.getID())){
                removed = true;
            }
        }
        if (Defaults.DEBUG_TRACE) Log.info(player.getName() + "   !!!&4removed  " + removed + " t=" + player.getTeam());
        if (removed){
            updateBukkitEvents(MatchState.ONLEAVE, player);
        }
        return removed;
    }
    @Override
    public void onPreJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        onPostEnter(player,apte);
    }

    @Override
    public void onPostJoin(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        addPlayer(player);
    }

    @Override
    public void onPreQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
    }

    @Override
    public void onPostQuit(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
    }

    @Override
    public void onPreEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
    }

    @Override
    public void onPostEnter(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        addPlayer(player);
    }

    @Override
    public void onPreLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
        removePlayer(player);
    }

    @Override
    public void onPostLeave(ArenaPlayer player, ArenaPlayerTeleportEvent apte) {
    }

    /**
     * Return a string of appended spawn locations
     * @return String
     */
    public String getSpawnLocationString(){
        StringBuilder sb = new StringBuilder();
        List<List<SpawnLocation>> locs = getSpawns();
        for (int i=0;i<locs.size(); i++ ){
            if (locs.get(i) != null){
                for (SpawnLocation loc : locs.get(i)){
                    sb.append("[").append(i + 1).append(":").
                            append(Util.getLocString(loc)).append("] ");
                }
            }
        }

        return sb.toString();
    }

}
