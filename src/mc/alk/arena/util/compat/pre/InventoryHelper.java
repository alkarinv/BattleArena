package mc.alk.arena.util.compat.pre;

import java.awt.Color;
import java.util.List;

import mc.alk.arena.util.compat.IInventoryHelper;

import org.bukkit.inventory.ItemStack;

public class InventoryHelper implements IInventoryHelper{
	@Override
	public void setItemColor(ItemStack itemStack, Color color) {/* do nothing */}

	@Override
	public void setLore(ItemStack itemStack, List<String> lore) {/* do nothing */}

	@Override
	public void setDisplayName(ItemStack itemStack, String displayName) {/* do nothing */}

	@Override
	public void setOwnerName(ItemStack itemStack, String ownerName) {/* do nothing */}
}
