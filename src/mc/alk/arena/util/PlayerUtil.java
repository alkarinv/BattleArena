package mc.alk.arena.util;

import mc.alk.arena.controllers.HeroesInterface;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class PlayerUtil {

	public static int getHunger(final Player player) {
		return player.getFoodLevel();
	}

	public static void setHunger(final Player player, final Integer hunger) {
		player.setFoodLevel(hunger);
	}

	public static void setHealthP(final Player player, final Integer health) {
		if (HeroesInterface.enabled()){
			HeroesInterface.setHealthP(player,health);
			return;
		}
		setHealth(player,health);
	}
	public static void setHealth(final Player player, final Integer health) {
		if (HeroesInterface.enabled()){
			HeroesInterface.setHealth(player,health);
			return;
		}
		final int oldHealth = player.getHealth();
		if (oldHealth > health){
			EntityDamageEvent event = new EntityDamageEvent(player,  DamageCause.CUSTOM, oldHealth-health );
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
				player.setLastDamageCause(event);
                final int dmg = Math.max(0,oldHealth - event.getDamage());
                player.setHealth(dmg);
			}
		} else if (oldHealth < health){
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, health-oldHealth,RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
                final int regen = Math.min(oldHealth + event.getAmount(),player.getMaxHealth());
                player.setHealth(regen);
			}
		}
	}

	public static Integer getHealth(Player player) {
		return HeroesInterface.enabled() ? HeroesInterface.getHealth(player) : player.getHealth();
	}

	public static void setInvulnerable(Player player, Integer invulnerableTime) {
		player.setNoDamageTicks(invulnerableTime);
		player.setLastDamage(Integer.MAX_VALUE);
	}

}
