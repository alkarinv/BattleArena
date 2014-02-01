package mc.alk.arena.listeners.competition.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.skills.McMMOPlayerSkillEvent;
import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashSet;

public enum McMMOListener implements Listener {
	INSTANCE;

	HashSet<SkillType> disabledSkills;

	public static void enable(boolean enable) {
        if (enable){
            Bukkit.getPluginManager().registerEvents(INSTANCE, BattleArena.getSelf());
        } else {
            HandlerList.unregisterAll(INSTANCE);
        }
    }

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void skillDisabled(McMMOPlayerSkillEvent event){
		if (INSTANCE.disabledSkills ==null || event.getPlayer() == null || !(event instanceof Cancellable) ||
                !InArenaListener.inArena(event.getPlayer().getName()) ||
                !INSTANCE.disabledSkills.contains(event.getSkill())){
			return;}
        ((Cancellable)event).setCancelled(true);
	}

	public static void setDisabledSkills(Collection<String> disabledCommands) {
		if (INSTANCE.disabledSkills == null){
            INSTANCE.disabledSkills = new HashSet<SkillType>();}
		for (String s: disabledCommands) {
            SkillType st = SkillType.getSkill(s);
            if (st == null){
                Log.err("mcMMO skill " + s +" was not found");
                continue;
            }
            INSTANCE.disabledSkills.add(st);
        }
    }

}
