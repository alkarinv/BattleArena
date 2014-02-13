package mc.alk.arena.objects.spawns;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;


public class ChestSpawn extends BlockSpawn{

    ItemStack[] items;

    public ChestSpawn(Block block, boolean setItems){
		super(block, setItems);
        if (setItems && block.getState() instanceof Chest){
            Chest chest = (Chest) block.getState();
            ItemStack[] contents = chest.getInventory().getContents();
            items = new ItemStack[contents.length];
            for (int i = 0; i < contents.length; i++) {
                items[i] = (contents[i] != null) ? contents[i].clone() : null;
            }
        }
    }

    public void setItems(Collection<ItemStack> items) {
        this.items = items.toArray(new ItemStack[items.size()]);
    }

    @Override
    public void spawn() {
        super.spawn();
        Chest chest = (Chest) loc.getBlock().getState();
        chest.getInventory().clear();
        chest.getInventory().setContents(items);
        chest.update(true);
    }

    @Override
	public void despawn() {
        World w = getLocation().getWorld();
        Block b = w.getBlockAt(getLocation());
        if (b.getState() instanceof Chest){
            Chest chest = (Chest) loc.getBlock().getState();
            chest.getInventory().clear();
            chest.update(true);
        }
        b.setType(Material.AIR);
	}

	@Override
	public String toString(){
		return "[ChestSpawn "+mat.name()+"]";
	}

    public boolean isDoubleChest() {
        final Block b = loc.getBlock();
        return (isChest(b.getRelative(BlockFace.NORTH)) ||
                isChest(b.getRelative(BlockFace.SOUTH)) ||
                isChest(b.getRelative(BlockFace.EAST))  ||
                isChest(b.getRelative(BlockFace.WEST)));
    }

    public static boolean isChest(Block block){
        return block.getState() instanceof Chest;
    }

    public ItemStack[] getItems() {
        return items;
    }
}
