package mc.alk.arena.util.compat.v1_4_5;

import mc.alk.arena.util.compat.IEntityHelper;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;

/**
 * @author alkarin
 */
public class EntityHelper implements IEntityHelper{
    @Override
    public void setCollarColor(Wolf wolf, DyeColor color) {
        wolf.setCollarColor(color);
    }
}
