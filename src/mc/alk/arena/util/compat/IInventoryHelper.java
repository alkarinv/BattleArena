package mc.alk.arena.util.compat;

import java.awt.Color;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface IInventoryHelper {

	void setColor(ItemStack itemStack, Color color);

	Color getColor(ItemStack itemStack);

	void setLore(ItemStack itemStack, List<String> lore);

	List<String> getLore(ItemStack itemStack);

	void setDisplayName(ItemStack itemStack, String displayName);

	String getDisplayName(ItemStack itemStack);

	void setOwnerName(ItemStack itemStack, String ownerName);

	String getOwnerName(ItemStack itemStack);

}
