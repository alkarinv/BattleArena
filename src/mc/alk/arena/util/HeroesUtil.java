package mc.alk.arena.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamFactory;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.classes.HeroClassManager;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.party.HeroParty;

public class HeroesUtil {
	static Heroes heroes = null;
	static Map<Team,HeroParty> parties = Collections.synchronizedMap(new HashMap<Team,HeroParty>());

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

	public static void createTeam(Team team) {
		HeroParty party = null;
		for (ArenaPlayer player: team.getPlayers()){
			Hero hero = getHero(player.getPlayer());
			if (hero == null)
				continue;
			if (party == null) {
				party = new HeroParty(hero, heroes);
				heroes.getPartyManager().addParty(party);
				parties.put(team, party);
			} else {
				party.addMember(hero);
			}
		}
	}
	public static void removeTeam(Team team){
		HeroParty party = parties.remove(team);
		if (party != null){
			heroes.getPartyManager().removeParty(party);
		}
	}
	public static void addedToTeam(Team team, Player player){
		HeroParty party = parties.get(team);
		if (party == null) {
			createTeam(team);
			party = parties.get(team);
		}

		Hero hero = getHero(player);
		if (hero == null)
			return;
		party.addMember(hero);
	}

	public static void removedFromTeam(Team team, Player player){
		HeroParty party = parties.get(team);
		if (party == null) {
			return;}
		Hero hero = getHero(player);
		if (hero == null){
			return;}
		party.removeMember(hero);
	}

	public static Team getTeam(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return null;
		HeroParty party = hero.getParty();
		if (party == null)
			return null;
		Team t = TeamFactory.createCompositeTeam();
		t.addPlayer(BattleArena.toArenaPlayer(party.getLeader().getPlayer()));
		Set<Hero> members = party.getMembers();
		if (members != null){
			for (Hero h: members){
				t.addPlayer(BattleArena.toArenaPlayer(h.getPlayer()));
			}
		}
		return t;
	}

	public static Integer getMagicLevel(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return null;
		return hero.getMana();
	}

	public static void setMagicLevel(Player player, Integer val) {
		Hero hero = getHero(player);
		if (hero == null)
			return;
		hero.setMana(val);
	}
}
