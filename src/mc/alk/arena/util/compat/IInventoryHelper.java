package mc.alk.arena.util.compat;

import java.awt.Color;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface IInventoryHelper {

	void setItemColor(ItemStack itemStack, Color color);

	void setLore(ItemStack itemStack, List<String> lore);

}
