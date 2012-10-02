package mc.alk.arena.util;

import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DmgDeathUtil {
	
	public static ArenaPlayer getPlayerCause(EntityDamageEvent lastDamageCause) {
		if (!(lastDamageCause instanceof EntityDamageByEntityEvent))
			return null;
		EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) lastDamageCause;

		if (edbee.getDamager() instanceof Projectile) { /// we have some sort of projectile
			Projectile proj = (Projectile) edbee.getDamager();
			if (proj.getShooter() instanceof Player){ /// projectile was shot by a player
				return PlayerController.toArenaPlayer((Player) proj.getShooter());
			} else { /// projectile shot by some mob, or other source, get out of here
				return null;
			}
		} else if (! (edbee.getDamager() instanceof Player)) { /// killer not player
			return null;
		} else { /// Killer is a player
			return PlayerController.toArenaPlayer((Player) edbee.getDamager());
		}
	}

	public static ArenaPlayer getPlayerCause(Entity lastDamageCause) {
		if (lastDamageCause instanceof Projectile) { /// we have some sort of projectile
//			System.out.println("Projectile ");
			Projectile proj = (Projectile) lastDamageCause;
			if (proj.getShooter() instanceof Player){ /// projectile was shot by a player
//				System.out.println("Projectile player " + proj.getShooter());

				return PlayerController.toArenaPlayer((Player) proj.getShooter());
			} else { /// projectile shot by some mob, or other source, get out of here
				return null;
			}
		} else if (! (lastDamageCause instanceof Player)) { /// killer not player
			return null;
		} else { /// Killer is a player
//			System.out.println("killer player " + lastDamageCause);
			return PlayerController.toArenaPlayer((Player) lastDamageCause);
		}
	}

	public static ArenaPlayer getPlayerCause(PlayerDeathEvent event) {
		return getPlayerCause(event.getEntity().getLastDamageCause());
	}

}
