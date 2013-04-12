package mc.alk.arena.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamFactory;
import mc.alk.arena.util.heroes.Heroes_1_5_2;
import mc.alk.arena.util.heroes.Heroes_pre1_5_2;
import mc.alk.plugin.updater.v1r2.Version;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.classes.HeroClassManager;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.party.HeroParty;

public abstract class HeroesUtil {
	static Heroes heroes = null;
	static Map<ArenaTeam,HeroParty> parties = Collections.synchronizedMap(new HashMap<ArenaTeam,HeroParty>());
	static HeroesUtil util = null;

	public abstract void setHeroPlayerHealth(Player player, int health);
	public abstract int getHeroHealth(Player player);
	public abstract void setHeroHealthP(Player player, int health);

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
		Version v = new Version(heroes.getDescription().getVersion());
		if (v.compareTo("1.5.2") >= 0){
			util = new Heroes_1_5_2();
		} else {
			util = new Heroes_pre1_5_2();
		}
	}

	public static String getHeroClassName(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return null;
		HeroClass hc = hero.getHeroClass();
		return hc == null ? null : hc.getName();
	}

	public static int getLevel(Player player) {
		Hero hero = getHero(player);
		return hero == null ? -1 : hero.getLevel();
	}

	protected static Hero getHero(Player player) {
		final CharacterManager cm = heroes.getCharacterManager();
		return cm.getHero(player);
	}

	public static boolean isInCombat(Player player) {
		Hero hero = getHero(player);
		return hero == null ? false : hero.isInCombat();
	}

	public static void deEnchant(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return;
		for (Effect effect : hero.getEffects()){
			hero.removeEffect(effect);}
	}

	public static HeroParty createParty(ArenaTeam team, Hero hero){
		HeroParty party = new HeroParty(hero, heroes);
		heroes.getPartyManager().addParty(party);
		parties.put(team, party);
		return party;
	}

	private static void removeOldParty(Hero hero, HeroParty newParty){
		HeroParty party = hero.getParty();
		if (party == null || (newParty != null && newParty==party))
			return;
		party.removeMember(hero);
		hero.setParty(null);
	}

	public static void removeTeam(ArenaTeam team){
		HeroParty party = parties.remove(team);
		if (party != null){
			heroes.getPartyManager().removeParty(party);}
	}

	public static HeroParty createTeam(ArenaTeam team) {
		HeroParty party = null;
		for (ArenaPlayer player: team.getPlayers()){
			Hero hero = getHero(player.getPlayer());
			if (hero == null)
				continue;

			removeOldParty(hero,null); /// Remove from any old parties
			/// if the party doesnt exist create it
			if (party == null) {
				party = createParty(team,hero);}
			/// Add the hero to the party,
			/// and the tell the hero which party they are in
			party.addMember(hero);
			hero.setParty(party);
		}
		return party;
	}

	public static void addedToTeam(ArenaTeam team, Player player){
		HeroParty party = parties.get(team);
		if (party == null)
			party = createTeam(team);

		Hero hero = getHero(player);
		if (hero == null)
			return;

		removeOldParty(hero, party);

		if (party == null) {
			party = createParty(team,hero);}

		/// Add the hero to the party,
		/// and the tell the hero which party they are in
		party.addMember(hero);
		hero.setParty(party);
	}

	public static void removedFromTeam(ArenaTeam team, Player player){
		HeroParty party = parties.get(team);
		if (party == null) {
			return;}
		Hero hero = getHero(player);
		if (hero == null){
			return;}
		removeOldParty(hero,null);
	}

	public static ArenaTeam getTeam(Player player) {
		Hero hero = getHero(player);
		if (hero == null)
			return null;
		HeroParty party = hero.getParty();
		if (party == null)
			return null;
		ArenaTeam t = TeamFactory.createCompositeTeam();
		Hero leader = party.getLeader();
		if (leader != null)
			t.addPlayer(BattleArena.toArenaPlayer(leader.getPlayer()));

		Set<Hero> members = party.getMembers();
		if (members != null){
			for (Hero h: members){
				t.addPlayer(BattleArena.toArenaPlayer(h.getPlayer()));}
		}
		return t.size() > 0 ? t : null;
	}

	public static Integer getMagicLevel(Player player) {
		Hero hero = getHero(player);
		return hero == null ? null : hero.getMana();
	}

	public static void setMagicLevel(Player player, Integer val) {
		Hero hero = getHero(player);
		if (hero == null)
			return;
		hero.setMana(val);
	}

	public static void setMagicLevelP(Player player, Integer magic) {
		Hero hero = getHero(player);
		if (hero == null)
			return;
		double val = (double)hero.getMaxMana() * magic/100.0;
		hero.setMana((int)val);
	}

	public static void setHealth(Player player, int health) {
		util.setHeroPlayerHealth(player, health);
	}

	public static void setHealthP(Player player, int health) {
		util.setHeroHealthP(player, health);
	}

	public static int getHealth(Player player) {
		return util.getHeroHealth(player);
	}

}
