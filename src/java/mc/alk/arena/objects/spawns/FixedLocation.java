package mc.alk.arena.objects.spawns;

import mc.alk.arena.objects.exceptions.SerializationException;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;

/**
 * @author alkarin
 */
public class FixedLocation extends Location implements SpawnLocation{
    public FixedLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public FixedLocation(Location loc) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public Location getLocation() {
        return this;
    }

    @Override
    public Object yamlToObject(Map<String, Object> map, String value) throws SerializationException {
        return null;
    }

    @Override
    public Object objectToYaml() throws SerializationException {
        return null;
    }
}
