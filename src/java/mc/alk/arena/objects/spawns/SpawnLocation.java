package mc.alk.arena.objects.spawns;

import mc.alk.arena.objects.YamlSerializable;
import org.bukkit.Location;

/**
 * @author alkarin
 */
public interface SpawnLocation extends YamlSerializable {
    public Location getLocation();
}
