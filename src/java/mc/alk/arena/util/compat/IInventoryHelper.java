package mc.alk.arena.util.compat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.List;

public interface IInventoryHelper {

	void setColor(ItemStack itemStack, Color color);

	Color getColor(ItemStack itemStack);

	void setLore(ItemStack itemStack, List<String> lore);

	List<String> getLore(ItemStack itemStack);

	void setDisplayName(ItemStack itemStack, String displayName);

	String getDisplayName(ItemStack itemStack);

	void setOwnerName(ItemStack itemStack, String ownerName);

	String getOwnerName(ItemStack itemStack);

    String getCommonNameByEnchantment(Enchantment enchantment);

    Enchantment getEnchantmentByCommonName(String itemName);

    boolean isEnderChest(InventoryType type);
}
