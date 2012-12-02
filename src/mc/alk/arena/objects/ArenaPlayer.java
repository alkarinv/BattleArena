package mc.alk.arena.objects;

import mc.alk.arena.controllers.HeroesInterface;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.PlayerUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.PlayerInventory;

public class ArenaPlayer {
	final String name;
	Player player;
	ArenaClass preferredClass;
	ArenaClass chosenClass;

	public ArenaPlayer(Player player) {
		this.player = player;
		this.name = player.getName();
	}

	public String getName() {
		return name;
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

	public PlayerInventory getInventory() {
		return player.getInventory();
	}

	public boolean hasPermission(String perm) {
		return player.hasPermission(perm);
	}


	public ArenaClass getPreferredClass() {
		return preferredClass;
	}

	public void setPreferredClass(ArenaClass preferredClass) {
		this.preferredClass = preferredClass;
	}

	public ArenaClass getChosenClass() {
		return chosenClass;
	}

	public void setChosenClass(ArenaClass chosenClass) {
		this.chosenClass = chosenClass;
	}

	public int getPriority() {
		return PermissionsUtil.getPriority(player);
	}

	public int getLevel() {
		return (HeroesInterface.enabled()) ? HeroesInterface.getLevel(player) : player.getLevel();
	}

}
