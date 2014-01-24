package mc.alk.arena.objects.spawns;

import mc.alk.arena.util.InventoryUtil;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;


public class ItemSpawn extends SpawnInstance{
	Entity uid;
	ItemStack is;
	public ItemSpawn(ItemStack is){
		super(null);
		this.is = is;

	}

	public int spawn() {
		if (uid != null && !uid.isDead()){
			return spawnId;
		}
		uid = loc.getWorld().dropItemNaturally(loc, is);
		return spawnId;
	}

	public void despawn() {
		if (uid != null){
			uid.remove();
			uid = null;
		}
	}
	public ItemStack getItemStack() {
		return is;
	}

	@Override
	public String toString(){
		return "[ItemSpawn "+InventoryUtil.getItemString(is)+"]";
	}


}
