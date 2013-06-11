package mc.alk.arena.util.compat.post;

import java.awt.Color;
import java.util.List;

import mc.alk.arena.util.compat.IInventoryHelper;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryHelper implements IInventoryHelper{

	@Override
	public void setItemColor(ItemStack itemStack, Color color) {
		ItemMeta meta = itemStack.getItemMeta();
		if (meta != null && itemStack.getItemMeta() instanceof LeatherArmorMeta){
			org.bukkit.Color bukkitColor = getBukkitColor(color);
			LeatherArmorMeta lam = (LeatherArmorMeta) itemStack.getItemMeta();
			lam.setColor(bukkitColor);
			itemStack.setItemMeta(lam);
		}
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

	@Override
	public void setDisplayName(ItemStack itemStack, String displayName) {
		ItemMeta meta = itemStack.getItemMeta();
		if(meta != null){
			meta.setDisplayName(displayName);
			itemStack.setItemMeta(meta);
		}
	}

	@Override
	public void setOwnerName(ItemStack itemStack, String ownerName) {
		ItemMeta im = itemStack.getItemMeta();
		if (im != null && im instanceof SkullMeta){
			SkullMeta sm = (SkullMeta) im;
		    sm.setOwner(ownerName);
		    itemStack.setItemMeta(sm);
		}
	}
}
