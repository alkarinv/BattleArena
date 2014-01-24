package mc.alk.arena.util.compat.pre;

import mc.alk.arena.util.Log;
import mc.alk.arena.util.compat.IEventHelper;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.reflect.Method;

public class EventHelper implements IEventHelper {
    Method ede_setDamage;

    public EventHelper(){
        try {
            ede_setDamage = EntityDamageEvent.class.getMethod("setDamage", int.class);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    @Override
    public void setDamage(EntityDamageEvent event, double damage) {
        try {
            ede_setDamage.invoke(event, (int) damage);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }
}
