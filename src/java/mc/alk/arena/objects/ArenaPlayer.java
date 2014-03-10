package mc.alk.arena.objects;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.controllers.containers.AreaContainer;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.meta.PlayerMetaData;
import mc.alk.arena.objects.spawns.EntitySpawn;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.ServerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Stack;


public class ArenaPlayer {
    static int count = 0;

    final int id=count++;

    /** Player name, needed if Player is not available or null */
    final String name;

    /** The bukkit player, refreshed with each new event having the player */
    Player player;

    /**
     * Which competitions is the player inside
     * This can be up to 2, in cases of a tournament or a reserved arena event
     * where they have the event, and the match
     * The stack order is the order in which they joined, the top being the most recent
     */
    final Stack<Competition> competitions = new Stack<Competition>();

    Arena arena;

    /** Which class did the player pick during the competition */
    ArenaClass preferredClass;

    /** Which class is the player currently */
    ArenaClass currentClass;

    /** The players old location, from where they were first teleported*/
    Location oldLocation;

    /** The current location of the player (in arena, lobby, etc)*/
    ArenaLocation curLocation = new ArenaLocation(AreaContainer.HOMECONTAINER, null , LocationType.HOME);

    List<SpawnInstance> mobs;

    /** Has the player specified they are "ready" by clicking a block or sign */
    boolean isReady;

    final PlayerMetaData meta = new PlayerMetaData();

    public ArenaPlayer(Player player) {
        this.player = player;
        this.name = player.getName();
        reset();
    }

    public String getName() {
        return name;
    }

    public void reset() {
        this.isReady = false;
        this.currentClass = null;
        this.preferredClass = null;
        if (mobs != null) {
            despawnMobs();
            mobs.clear();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArenaPlayer &&
                ((ArenaPlayer) obj).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    public double getHealth() {
        return PlayerUtil.getHealth(player);
    }

    public void setHealth(double health) {
        PlayerUtil.setHealth(player,health);
    }

    public int getFoodLevel() {
        return PlayerUtil.getHunger(player);
    }

    public void setFoodLevel(int hunger) {
        PlayerUtil.setHunger(player,hunger);
    }

    public String getDisplayName() {
        return player.getDisplayName();
    }

    public void sendMessage(String colorChat) {
        player.sendMessage(colorChat);
    }

    public Location getLocation() {
        return player.getLocation();
    }

    public EntityDamageEvent getLastDamageCause() {
        return player.getLastDamageCause();
    }

    public void setFireTicks(int i) {
        player.setFireTicks(i);
    }

    public boolean isDead() {
        return player.isDead();
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public PlayerInventory getInventory() {
        return player.getInventory();
    }

    public boolean hasPermission(String perm) {
        return player.hasPermission(perm);
    }

    public ArenaClass getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(ArenaClass arenaClass) {
        this.currentClass = arenaClass;
    }

    public ArenaClass getPreferredClass() {
        return preferredClass;
    }

    public void setPreferredClass(ArenaClass arenaClass) {
        this.preferredClass = arenaClass;
    }

    public int getPriority() {
        return PermissionsUtil.getPriority(player);
    }

    public int getLevel() {
        return (HeroesController.enabled()) ? HeroesController.getLevel(player) : player.getLevel();
    }

    public Competition getCompetition() {
        return competitions.isEmpty() ? null : competitions.peek();
    }

    public void addCompetition(Competition competition) {
        if (!competitions.contains(competition))
            competitions.push(competition);
    }

    public boolean removeCompetition(Competition competition) {
        return competitions.remove(competition);
    }

    /**
     * Returns their current team, based on whichever competition is top of the stack
     * This is NOT a self made team, only the team from the competition
     * @return Team, or null if they are not inside a competition
     */
    public ArenaTeam getTeam() {
        return competitions.isEmpty() ? null : competitions.peek().getTeam(this);
    }

    public void markOldLocation(){
        if (oldLocation == null){
            oldLocation = getLocation();}
    }

    public void clearOldLocation(){
        oldLocation = null;
    }

    public Location getOldLocation(){
        return oldLocation;
    }

    public void setCurLocation(ArenaLocation type){
        this.curLocation = type;
    }

    public ArenaLocation getCurLocation(){
        return this.curLocation;
    }

    public void despawnMobs(){
        if (mobs != null){
            for (SpawnInstance es: mobs){
                es.despawn();}
        }
    }

    public void setMobs(List<SpawnInstance> mobs){
        this.mobs = mobs;
    }

    public void spawnMobs(){
        if (mobs != null){
            for (SpawnInstance es: mobs){
                es.despawn();
                es.setLocation(this.getLocation());
                es.spawn();
                if (es instanceof EntitySpawn) {
                    ((EntitySpawn) es).setOwner(getPlayer());
                }
            }
        }
    }

    public PlayerMetaData getMetaData(){
        return meta;
    }


    public ArenaStat getStat(MatchParams type) {
        return StatController.loadRecord(type, this);
    }

    public Player regetPlayer() {
        return ServerUtil.findPlayerExact(this.getName());
    }

    public String toString() {
        return "[AP " + this.getName() + "]";
    }

}
