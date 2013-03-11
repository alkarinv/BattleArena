package mc.alk.arena.util.heroes;

import mc.alk.arena.util.HeroesUtil;
import mc.alk.arena.util.PlayerUtil;

import org.bukkit.entity.Player;

public class Heroes_1_5_2 extends HeroesUtil{

	@Override
	public void setHeroPlayerHealth(Player player, int health) {
		PlayerUtil.setHealth(player, health, true);
	}

	@Override
	public int getHeroHealth(Player player) {
		return PlayerUtil.getHealth(player,true);
	}

	@Override
	public void setHeroHealthP(Player player, int health) {
		double val = (double)player.getMaxHealth() * health/100.0;
		PlayerUtil.setHealth(player, (int) val, true);
	}

}
