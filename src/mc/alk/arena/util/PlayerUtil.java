package mc.alk.arena.util;

import java.lang.reflect.Method;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EssentialsController;
import mc.alk.arena.controllers.HeroesController;
import mc.alk.arena.objects.CommandLineString;
import mc.alk.arena.util.compat.IPlayerHelper;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerUtil {
	static IPlayerHelper handler = null;

	static {
		Class<?>[] args = {};
		try {
			Method m = Player.class.getMethod("getHealth", new Class<?>[]{});
			if (m.getReturnType() == double.class){
				final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.v1_6_1.PlayerHelper");
				handler = (IPlayerHelper) clazz.getConstructor(args).newInstance((Object[])args);
			} else {
				final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.pre.PlayerHelper");
				handler = (IPlayerHelper) clazz.getConstructor(args).newInstance((Object[])args);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
	}

	public static int getHunger(final Player player) {
		return player.getFoodLevel();
	}

	public static void setHunger(final Player player, final Integer hunger) {
		player.setFoodLevel(hunger);
	}

	public static void setHealthP(final Player player, final Double health) {
		setHealthP(player,health, false);
	}

	public static void setHealthP(final Player player, final Double health, boolean skipHeroes) {
		if (!skipHeroes && HeroesController.enabled()){
			HeroesController.setHealthP(player,health);
			return;
		}
		double val = (player.getMaxHealth() * health/100.0);
		setHealth(player,val);
	}

	public static void setHealth(final Player player, final Double health) {
		setHealth(player,health,false);
	}

	public static void setHealth(final Player player, final Double health, boolean skipHeroes) {
		handler.setHealth(player,health,skipHeroes);
	}

	public static Double getHealth(Player player) {
		return getHealth(player,false);
	}

	public static Double getHealth(Player player, boolean skipHeroes) {
		return !skipHeroes && HeroesController.enabled() ?
				HeroesController.getHealth(player) : handler.getHealth(player);
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
