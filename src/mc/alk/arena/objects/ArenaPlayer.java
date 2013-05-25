package mc.alk.arena.objects;

import java.util.List;
import java.util.Stack;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.PlayerUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.PlayerInventory;


public class ArenaPlayer {

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
	Stack<Competition> competitions = new Stack<Competition>();

	/** Which class did the player pick during the competition */
	ArenaClass preferredClass;

	/** Which class is the player currently */
	ArenaClass currentClass;

	/** The players old location, from where they were first teleported*/
	Location oldLocation;

	/** The current location of the player (in arena, lobby, etc)*/
	LocationType curLocation = LocationType.HOME;

	List<SpawnInstance> mobs;

	/** Has the player specified they are "ready" by clicking a block or sign */
	boolean isReady;

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
		this.despawnMobs();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ArenaPlayer)) {
			return false;}
		if (obj == this) return true;
		final ArenaPlayer o = (ArenaPlayer) obj;
		return o.getName().equals(getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
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

	public int getHealth() {
		return PlayerUtil.getHealth(player);
	}

	public void setHealth(int health) {
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
	public void setCurLocation(LocationType type){
		this.curLocation = type;
	}
	public LocationType getCurLocation(){
		return this.curLocation;
	}
	public void despawnMobs(){
		if (mobs != null){
			for (SpawnInstance es: mobs){
				es.despawn();}
			mobs.clear();
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
			}
		}
	}
}
