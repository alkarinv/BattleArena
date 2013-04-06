package mc.alk.arena.objects.regions;

import mc.alk.v1r5.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.World;

public class BoundingBox
{
	protected Location lower;
	protected Location upper;

	public BoundingBox() {}

	public BoundingBox(Location l, Location l2)
	{
		createBoundingBox(l, l2);
	}

	public void createBoundingBox(Location l1, Location l2)
	{
		// System.out.println("createBoundingBox ");
		lower = new Location(l1.getWorld(), Math.min(l1.getBlockX(),
				l2.getBlockX()), Math.min(l1.getBlockY(), l2.getBlockY()),
				Math.min(l1.getBlockZ(), l2.getBlockZ()));
		upper = new Location(l1.getWorld(), Math.max(l1.getBlockX(),
				l2.getBlockX()), Math.max(l1.getBlockY(), l2.getBlockY()),
				Math.max(l1.getBlockZ(), l2.getBlockZ()));
	}

	public boolean contains(Location l)
	{
		return (l.getWorld().getName().equals(lower.getWorld().getName())
				&& (l.getBlockX() >= lower.getBlockX() && l.getBlockX() <= upper
						.getBlockX())
				&& (l.getBlockY() >= lower.getBlockY() && l.getBlockY() <= upper
						.getBlockY()) && (l.getBlockZ() >= lower.getBlockZ() && l
				.getBlockZ() <= upper.getBlockZ()));
	}

	public Location getCorner1(){return lower;}
	public Location getCorner2(){return upper;}

	public Location getLowerCorner(){return lower;}
	public Location getUpperCorner(){return upper;}

	public World getWorld(){ return lower.getWorld();}

	@Override
	public String toString(){
		return "[BB " + SerializerUtil.getLocString(lower) +":"+SerializerUtil.getLocString(upper)+"]";
	}
}
