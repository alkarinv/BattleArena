package mc.alk.arena.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DmgDeathUtil {
	
	public static Player getPlayerCause(EntityDamageEvent lastDamageCause) {
		if (!(lastDamageCause instanceof EntityDamageByEntityEvent))
			return null;
		EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) lastDamageCause;

		if (edbee.getDamager() instanceof Projectile) { /// we have some sort of projectile
			Projectile proj = (Projectile) edbee.getDamager();
			if (proj.getShooter() instanceof Player){ /// projectile was shot by a player
				return (Player) proj.getShooter();
			} else { /// projectile shot by some mob, or other source, get out of here
				return null;
			}
		} else if (! (edbee.getDamager() instanceof Player)) { /// killer not player
			return null;
		} else { /// Killer is a player
			return (Player) edbee.getDamager();
		}
	}

}
