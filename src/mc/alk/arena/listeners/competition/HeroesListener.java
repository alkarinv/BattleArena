package mc.alk.arena.listeners.competition;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.players.ArenaPlayerEnterMatchEvent;
import mc.alk.arena.util.HeroesUtil;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.api.events.ExperienceChangeEvent;
import com.herocraftonline.heroes.api.events.SkillUseEvent;

public enum HeroesListener implements Listener {
	INSTANCE;

	final Set<String> cancelExpLoss = Collections.synchronizedSet(new HashSet<String>());

	static HashSet<String> disabledSkills = new HashSet<String>();

	public static void enable() {
		Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());
	}

	@EventHandler
	public void onArenaPlayerEnterEvent(ArenaPlayerEnterMatchEvent event){
		HeroesUtil.addedToTeam(event.getTeam(), event.getPlayer().getPlayer());
	}

	/**
	 * Need to be highest to override the standard renames
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void cancelExperienceLoss(ExperienceChangeEvent event) {
		if (event.isCancelled())
			return;
		final String name = event.getHero().getName();
		if (cancelExpLoss.contains(name)){
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void skillDisabled(SkillUseEvent event){
		if (event.getPlayer() == null){
			return;}
		if (!InArenaListener.inArena(event.getPlayer().getName())){
			return;}
		if (event.getSkill().getName().equals("Revive")){
			Player p = event.getArgs().length > 0 ? Bukkit.getPlayer(event.getArgs()[0]) : null;
			if (p != null && !InArenaListener.inArena(p.getName())){
				MessageUtil.sendMessage(event.getPlayer(), "&cYou can't revive a player who is not in the arena!");
				event.setCancelled(true);
			}
		}
		if (!containsHeroesSkill(event.getSkill().getName()))
			return;
		event.setCancelled(true);
	}

	public static void setCancelExpLoss(Player player) {
		INSTANCE.cancelExpLoss.add(player.getName());
	}

	public static void removeCancelExpLoss(Player player) {
		INSTANCE.cancelExpLoss.remove(player.getName());
	}

	public static boolean containsHeroesSkill(String skill) {
		return disabledSkills.contains(skill.toLowerCase());
	}

	public static void addDisabledCommands(Collection<String> disabledCommands) {
		if (disabledSkills == null){
			disabledSkills = new HashSet<String>();}
		for (String s: disabledCommands){
			disabledSkills.add(s.toLowerCase());}
	}

}
