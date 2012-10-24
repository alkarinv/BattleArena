package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.util.EffectUtil.EffectWithArgs;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.inventory.ItemStack;

public class ArenaClass {
	public static final Integer DEFAULT = Integer.MAX_VALUE;

	final String name;
	final List<ItemStack> items;
	final List<EffectWithArgs> effects;
	final String prettyName;
	public ArenaClass(String name, String prettyName, List<ItemStack> items, List<EffectWithArgs> effects){
		this.name = name;
		ArrayList<ItemStack> listitems = new ArrayList<ItemStack>();
		ArrayList<ItemStack> armoritems = new ArrayList<ItemStack>();
		for (ItemStack is: items){
			if (InventoryUtil.isArmor(is)){
				armoritems.add(is);
			} else {
				listitems.add(is);
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

	public List<EffectWithArgs> getEffects() {
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
