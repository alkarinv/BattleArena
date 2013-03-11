package mc.alk.arena.util.heroes;

import mc.alk.arena.util.HeroesUtil;

import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;

public class Heroes_pre1_5_2 extends HeroesUtil{

	@Override
	public void setHeroPlayerHealth(Player player, int health) {
		Hero hero = getHero(player);
		if (hero == null){
			player.setHealth(health);
		} else{
			hero.setHealth(health);
			hero.syncHealth();
		}
	}

	@Override
	public int getHeroHealth(Player player) {
		Hero hero = getHero(player);
		return hero == null ? player.getHealth() : hero.getHealth();
	}

	@Override
	public void setHeroHealthP(Player player, int health) {
		Hero hero = getHero(player);
		if (hero == null){
			double val = (double)player.getMaxHealth() * health/100.0;
			player.setHealth((int)val);
		} else{
			double val = (double)hero.getMaxHealth() * health/100.0;
			hero.setHealth((int)val);
			hero.syncHealth();
		}
	}

}
