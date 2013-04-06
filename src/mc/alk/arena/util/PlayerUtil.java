package mc.alk.arena.util;

import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EssentialsController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.objects.CommandLineString;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
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
		if (HeroesController.enabled()){
			HeroesController.setHealthP(player,health);
			return;
		}
		int val = (int) ((double)player.getMaxHealth() * health/100.0);
		setHealth(player,val);
	}

	public static void setHealth(final Player player, final Integer health) {
		setHealth(player,health,false);
	}

	public static void setHealth(final Player player, final Integer health, boolean skipHeroes) {
		if (!skipHeroes && HeroesController.enabled()){
			HeroesController.setHealth(player,health);
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
		return getHealth(player,false);
	}

	public static Integer getHealth(Player player, boolean skipHeroes) {
		return !skipHeroes && HeroesController.enabled() ?
				HeroesController.getHealth(player) : player.getHealth();
	}

	public static void setInvulnerable(Player player, Integer invulnerableTime) {
		player.setNoDamageTicks(invulnerableTime);
		player.setLastDamage(Integer.MAX_VALUE);
	}

	public static void setGameMode(Player p, GameMode gameMode) {
		if (p.getGameMode() != gameMode){
			PermissionsUtil.givePlayerInventoryPerms(p);
			p.setGameMode(gameMode);
		}
	}

	public static void doCommands(Player p, List<CommandLineString> doCommands) {
		final String name = p.getName();
		for (CommandLineString cls: doCommands){
			CommandSender cs = cls.isConsoleSender() ? Bukkit.getConsoleSender() : p;
			try{
				if (Defaults.DEBUG_TRANSITIONS) {Log.info("BattleArena doing command '"+cls.getCommand(name)+"' as "+cs.getName());}
				Bukkit.getServer().dispatchCommand(cs, cls.getCommand(name));
			} catch (Exception e){
				Log.err("[BattleArena] Error executing command as console or player");
				e.printStackTrace();
			}

		}
	}

	public static void setFlight(Player player, boolean enable) {
		if (player.getAllowFlight() != enable){
			player.setAllowFlight(enable);}
		if (player.isFlying() != enable){
			player.setFlying(enable);}
		/* Essentials (v2.10) fly just goes through bukkit, no need to call Essentials setFlight */
	}

	public static void setFlightSpeed(Player player, Float flightSpeed) {
		player.setFlySpeed(flightSpeed);
		/* Essentials (v2.10) fly just goes through bukkit, no need to call Essentials setFlySpeed */
	}

	public static void setGod(Player player, boolean enable) {
		if (EssentialsController.enabled()){
			EssentialsController.setGod(player, enable);}
	}

}
