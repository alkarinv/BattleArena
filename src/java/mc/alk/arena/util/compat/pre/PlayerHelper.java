package mc.alk.arena.util.compat.pre;

import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.compat.IPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import java.lang.reflect.Method;

@SuppressWarnings({"UnnecessaryBoxing", "BoxingBoxedValue"})
public class PlayerHelper implements IPlayerHelper{
	Method getHealth;
	Method setHealth;
	Method getMaxHealth;
	Method getAmount;
	final Object blankArgs[] = {};
	public PlayerHelper(){
		try {
			setHealth = Player.class.getMethod("setHealth", int.class);
			getHealth = Player.class.getMethod("getHealth");
			getMaxHealth = Player.class.getMethod("getMaxHealth");
			getAmount = EntityRegainHealthEvent.class.getMethod("getAmount");
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setHealth(Player player, double health, boolean skipHeroes) {
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
				setHealth(player,dmg);
			}
		} else if (oldHealth < health){
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, (int)(health-oldHealth),RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()){
				final Integer regen = Math.min(oldHealth + getAmount(event),(int)getMaxHealth(player));
				setHealth(player, regen);
			}
		}
	}

	@Override
	public double getHealth(Player player) {
		try {
			return new Double((Integer)getHealth.invoke(player, blankArgs));
		} catch (Exception e) {
			Log.printStackTrace(e);
			return 20;
		}
	}

	@Override
	public double getMaxHealth(Player player) {
		try {
			return new Double((Integer)getMaxHealth.invoke(player, blankArgs));
		} catch (Exception e) {
			Log.printStackTrace(e);
			return 20;
		}
	}

	public Integer getAmount(EntityRegainHealthEvent event) {
		try {
			return new Integer((Integer)getAmount.invoke(event, blankArgs));
		} catch (Exception e) {
			Log.printStackTrace(e);
			return null;
		}
	}

	public void setHealth(Player player, Integer health){
		try {
			setHealth.invoke(player, health);
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}
}
