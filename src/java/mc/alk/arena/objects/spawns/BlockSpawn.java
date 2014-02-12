package mc.alk.arena.objects.spawns;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;


public class BlockSpawn extends SpawnInstance{
	final Block block;

	public BlockSpawn(Block block){
		super(block.getLocation());
        this.block = block;
    }

    @Override
    public void spawn() {
        World w = getLocation().getWorld();
        Block b = w.getBlockAt(getLocation());
        if (b.getType() != block.getType())
            b.setType(block.getType());
	}

    @Override
	public void despawn() {
        World w = getLocation().getWorld();
        Block b = w.getBlockAt(getLocation());
        b.setType(Material.AIR);
	}

	@Override
	public String toString(){
		return "[BlockSpawn "+block+"]";
	}

    public Block getBlock() {
        return block;
    }
}
