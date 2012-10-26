package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.util.InventoryUtil;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class ArenaClass {
	public static final Integer DEFAULT = Integer.MAX_VALUE;
	public static final ArenaClass CHOSEN_CLASS = new ArenaClass("CHOSENCLASS","chosenClass", null, null);
	final String name;
	final List<ItemStack> items;
	final List<PotionEffect> effects;
	final String prettyName;
	public ArenaClass(String name, String prettyName, List<ItemStack> items, List<PotionEffect> effects){
		this.name = name;
		CopyOnWriteArrayList<ItemStack> listitems = new CopyOnWriteArrayList<ItemStack>();
		ArrayList<ItemStack> armoritems = new ArrayList<ItemStack>();
		if (items != null){
			for (ItemStack is: items){
				if (InventoryUtil.isArmor(is)){
					armoritems.add(is);
				} else {
					listitems.add(is);
				}
			}
		}
		this.items = listitems;
		this.items.addAll(armoritems);
		this.effects = effects;
		this.prettyName = prettyName;
	}
	public String getName() {
		return name;
	}
	public List<ItemStack> getItems() {
		return items;
	}

	public List<PotionEffect> getEffects() {
		return effects;
	}

	@Override
	public String toString(){
		return "[ArenaClass "+name+" items="+items +" enchants=" + effects+"]";
	}
	public String getPrettyName() {
		return prettyName != null ? prettyName : name;
	}
}
