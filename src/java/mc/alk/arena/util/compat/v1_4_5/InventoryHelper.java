package mc.alk.arena.util.compat.v1_4_5;

import java.awt.Color;
import java.util.List;

import mc.alk.arena.util.compat.IInventoryHelper;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryHelper implements IInventoryHelper{

	@Override
	public void setColor(ItemStack itemStack, Color color) {
		ItemMeta meta = itemStack.getItemMeta();
		if (meta != null && itemStack.getItemMeta() instanceof LeatherArmorMeta){
			org.bukkit.Color bukkitColor = getBukkitColor(color);
			LeatherArmorMeta lam = (LeatherArmorMeta) itemStack.getItemMeta();
			lam.setColor(bukkitColor);
			itemStack.setItemMeta(lam);
		}
	}

	@Override
	public Color getColor(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		if (meta != null && itemStack.getItemMeta() instanceof LeatherArmorMeta){
			LeatherArmorMeta lam = (LeatherArmorMeta) itemStack.getItemMeta();
			return new Color(lam.getColor().getRed(), lam.getColor().getGreen(), lam.getColor().getBlue());
		}
		return null;
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
	public List<String> getLore(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		return meta == null ? null : meta.getLore();
	}

	@Override
	public void setDisplayName(ItemStack itemStack, String displayName) {
		ItemMeta meta = itemStack.getItemMeta();
		if(meta != null){
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',displayName));
			itemStack.setItemMeta(meta);
		}
	}

	@Override
	public String getDisplayName(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		return meta == null ? null : meta.getDisplayName();
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

	@Override
	public String getOwnerName(ItemStack itemStack) {
		ItemMeta im = itemStack.getItemMeta();
		if (im != null && im instanceof SkullMeta){
			return ((SkullMeta)im).getOwner();}
		return null;
	}

    @Override
    public String getCommonNameByEnchantment(Enchantment enchantment) {
        if (enchantment.getId() == Enchantment.THORNS.getId()){return "Thorns";}
		else return enchantment.getName();
    }

    @Override
    public Enchantment getEnchantmentByCommonName(String itemName) {
        if (itemName.contains("thorn")) return Enchantment.THORNS;
		return null;
    }

    @Override
    public boolean isEnderChest(InventoryType type) {
        return type == InventoryType.ENDER_CHEST;
    }
}
