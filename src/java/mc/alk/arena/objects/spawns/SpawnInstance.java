package mc.alk.arena.objects.spawns;

import org.bukkit.Location;
import org.bukkit.World;

public abstract class SpawnInstance implements Spawnable, SpawnableInstance{
	static int classCount = 0;

	final Integer spawnId = classCount++;
	Location loc;

	public SpawnInstance(Location location) {
		this.loc = location;
	}

	public World getWorld() {
		return loc != null ? loc.getWorld() : null;
	}

	public void setLocation(Location l) {
		this.loc = l;
	}

	public Location getLocation() {
		return loc;
	}

    public int getID() {
        return spawnId;
    }

}
