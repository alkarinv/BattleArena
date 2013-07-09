package mc.alk.arena.util.heroes;

import mc.alk.arena.util.HeroesUtil;
import mc.alk.arena.util.PlayerUtil;

import org.bukkit.entity.Player;

public class Heroes_1_5_2 extends HeroesUtil{

	@Override
	public void setHeroPlayerHealth(Player player, double health) {
		PlayerUtil.setHealth(player, health, true);
	}

	@Override
	public double getHeroHealth(Player player) {
		return PlayerUtil.getHealth(player,true);
	}

	@Override
	public void setHeroHealthP(Player player, double health) {
		double val = player.getMaxHealth() * health/100.0;
		PlayerUtil.setHealth(player, val, true);
	}

}
