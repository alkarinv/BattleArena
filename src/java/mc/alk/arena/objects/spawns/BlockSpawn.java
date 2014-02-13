package mc.alk.arena.objects.spawns;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;


public class BlockSpawn extends SpawnInstance{
	Material mat;

	public BlockSpawn(Block block, boolean setMaterial){
		super(block.getLocation());
        if (setMaterial){
            this.mat = block.getType();
        }
    }
    public void setMaterial(Material mat) {
        this.mat = mat;
    }
    @Override
    public void spawn() {
        World w = getLocation().getWorld();
        Block b = w.getBlockAt(getLocation());
        if (b.getType() != mat)
            b.setType(mat);
	}

    @Override
	public void despawn() {
        World w = getLocation().getWorld();
        Block b = w.getBlockAt(getLocation());
        b.setType(Material.AIR);
	}

	@Override
	public String toString(){
		return "[BlockSpawn "+mat.name()+"]";
	}

    public Block getBlock() {
        return this.getLocation().getBlock();
    }
    public Material getMaterial() {
        return mat;
    }
}
