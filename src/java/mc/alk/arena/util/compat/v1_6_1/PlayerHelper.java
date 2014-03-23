package mc.alk.arena.util.compat.v1_6_1;

import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.util.compat.IPlayerHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class PlayerHelper implements IPlayerHelper{

	@Override
	public void setHealth(Player player, double health, boolean skipHeroes) {
		if (!skipHeroes && HeroesController.enabled()){
			HeroesController.setHealth(player,health);
			return;
		}

		final double oldHealth = player.getHealth();
		if (oldHealth > health){
			EntityDamageEvent event = new EntityDamageEvent(player,  DamageCause.CUSTOM, oldHealth-health );
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
				player.setLastDamageCause(event);
				final double dmg = Math.max(0,oldHealth - event.getDamage());
				player.setHealth(dmg);
			}
		} else if (oldHealth < health){
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, health-oldHealth,RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
				final double regen = Math.min(oldHealth + event.getAmount(),player.getMaxHealth());
				player.setHealth(regen);
			}
		}
	}

	@Override
	public double getHealth(Player player) {
		return player.getHealth();
	}

	@Override
	public double getMaxHealth(Player player) {
		return player.getMaxHealth();
	}


}
