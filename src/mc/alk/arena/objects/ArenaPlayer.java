package mc.alk.arena.objects;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;

public class ArenaPlayer {
	Player player;
	ArenaClass preferredClass;
	ArenaClass chosenClass;
	
	public ArenaPlayer(Player player) {
		this.player = player;
	}

	public String getName() {
		return player.getName();
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
		return getName().hashCode();
	}

	public Player getPlayer() {
		return player;
	}
	public int getHealth() {
		return player.getHealth();
	}

	public boolean isOnline() {
		return player.isOnline();
	}

	public void setHealth(int health) {
		player.setHealth(health);
	}

	public void setFoodLevel(int hunger) {
		player.setFoodLevel(hunger);
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

	public Inventory getInventory() {
		return player.getInventory();
	}

	public void setPlayer(Player player) {
		this.player = player;
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

}
