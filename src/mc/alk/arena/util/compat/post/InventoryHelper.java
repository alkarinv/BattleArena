package mc.alk.arena.util.compat.post;

import java.awt.Color;
import java.util.List;

import mc.alk.arena.util.compat.IInventoryHelper;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class InventoryHelper implements IInventoryHelper{

	@Override
	public void setItemColor(ItemStack itemStack, Color color) {
		org.bukkit.Color bukkitColor = getBukkitColor(color);
		LeatherArmorMeta lam = (LeatherArmorMeta) itemStack.getItemMeta();
		lam.setColor(bukkitColor);
		itemStack.setItemMeta(lam);
	}

	public static org.bukkit.Color getBukkitColor(Color color){
		return org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
	}

	@Override
	public void setLore(ItemStack itemStack, List<String> lore) {
		ItemMeta meta = itemStack.getItemMeta();
		if(meta != null){
			meta.setLore(lore);
			itemStack.setItemMeta(meta);
		}
	}
}
