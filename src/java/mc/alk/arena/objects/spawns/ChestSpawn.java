package mc.alk.arena.objects.spawns;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;


public class ChestSpawn extends BlockSpawn{
	final Chest chest;
    ItemStack[] items;

    public ChestSpawn(Chest chest){
		super(chest.getBlock());
        this.chest = chest;
        ItemStack[] contents = chest.getInventory().getContents();
        items = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            items[i] = (contents[i] != null) ? contents[i].clone() : null;
        }
    }

    @Override
    public void spawn() {
        super.spawn();
        chest.getInventory().clear();
        chest.getInventory().setContents(items);
    }

    @Override
	public void despawn() {
        World w = getLocation().getWorld();
        Block b = w.getBlockAt(getLocation());
        b.setType(Material.AIR);
	}

	@Override
	public String toString(){
		return "[ChestSpawn "+block+"]";
	}

    public Chest getChest() {
        return chest;
    }

    public boolean isDoubleChest() {
        final Block b = chest.getBlock();
        return (isChest(b.getRelative(BlockFace.NORTH)) ||
                isChest(b.getRelative(BlockFace.SOUTH)) ||
                isChest(b.getRelative(BlockFace.EAST))  ||
                isChest(b.getRelative(BlockFace.WEST)));
    }

    public static boolean isChest(Block block){
        return block.getState() instanceof Chest;
    }

}
