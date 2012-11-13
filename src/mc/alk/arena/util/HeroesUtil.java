package mc.alk.arena.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.classes.HeroClassManager;
import com.herocraftonline.heroes.characters.effects.Effect;

public class HeroesUtil {
	static Heroes heroes = null;

	public static boolean hasHeroClass(String name) {
		if (heroes == null)
			return false;
		HeroClassManager manager = heroes.getClassManager();
		return manager.getClass(name) != null;
	}

	public static void setHeroClass(Player player, String name) {
		HeroClassManager manager = heroes.getClassManager();
		HeroClass hc = manager.getClass(name);
		if (hc == null)
			return;
		Hero hero = getHero(player);
		if (hero == null)
			return;
//		System.out.println("Heroes oldClass=" + hero.getHeroClass().getName()+"  new=" + hc.getName());
		if (hero.getHeroClass().getName().equals(hc.getName()))
			return;
		hero.setHeroClass(hc, false);
	}

	public static void setHeroes(Plugin plugin){
		heroes = (Heroes) plugin;
	}

	public static String getHeroClassName(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return null;
		HeroClass hc = hero.getHeroClass();
		if (hc == null)
			return null;
		return hc.getName();
	}

	public static int getLevel(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return -1;
		return hero.getLevel();
	}

	public static void setMagic(Player player, Integer magic) {
		Hero hero = getHero(player);
		if (hero == null)
			return;
		double max = (double)hero.getMaxMana() * magic/100.0;
		hero.setMana((int)max);
	}

	private static Hero getHero(Player player) {
		CharacterManager cm = heroes.getCharacterManager();
		Hero hero = cm.getHero(player);
		if (hero == null)
			return null;
		return hero;
	}

	public static boolean isInCombat(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return false;
		return hero.isInCombat();
	}

	public static void deEnchant(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return;
		for (Effect effect : hero.getEffects()){
			hero.removeEffect(effect);
		}
	}

}
