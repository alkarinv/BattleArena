package mc.alk.arena.util.compat.pre;

import java.lang.reflect.Method;

import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.util.compat.IPlayerHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class PlayerHelper implements IPlayerHelper{
	Method getHealth;
	final Object getHealthArgs[] = {};
	public PlayerHelper(){
		try {
			getHealth = Player.class.getMethod("getHealth", new Class<?>[]{});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setHealth(Player player, Double health, boolean skipHeroes) {
		if (!skipHeroes && HeroesController.enabled()){
			HeroesController.setHealth(player,health);
			return;
		}

		final int oldHealth = (int)getHealth(player);
		if (oldHealth > health){
			EntityDamageEvent event = new EntityDamageEvent(player,  DamageCause.CUSTOM, (int)(oldHealth-health) );
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
				player.setLastDamageCause(event);
				final int dmg = (int) Math.max(0,oldHealth - event.getDamage());
				player.setHealth(dmg);
			}
		} else if (oldHealth < health){
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, (int)(health-oldHealth),RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
				final int regen = (int) Math.min(oldHealth + event.getAmount(),(int)player.getMaxHealth());
				player.setHealth(regen);
			}
		}
	}

	@Override
	public double getHealth(Player player) {
		try {
			return new Double((Integer)getHealth.invoke(player, getHealthArgs));
		} catch (Exception e) {
			e.printStackTrace();
			return 20;
		}
	}
}
