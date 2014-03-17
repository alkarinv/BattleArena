package mc.alk.arena.listeners.competition.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.skills.McMMOPlayerSkillEvent;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.util.Log;
import org.bukkit.event.Cancellable;

import java.util.Collection;
import java.util.HashSet;

public class McMMOListener implements ArenaListener {

	static HashSet<SkillType> disabledSkills;

	@ArenaEventHandler
	public void skillDisabled(McMMOPlayerSkillEvent event){
		if (disabledSkills ==null || event.getPlayer() == null || !(event instanceof Cancellable) ||
                !disabledSkills.contains(event.getSkill())){
			return;}
        ((Cancellable)event).setCancelled(true);
	}

	public static void setDisabledSkills(Collection<String> disabledCommands) {
		if (disabledSkills == null){
            disabledSkills = new HashSet<SkillType>();}
		for (String s: disabledCommands) {
            SkillType st = SkillType.getSkill(s);
            if (st == null){
                Log.err("mcMMO skill " + s +" was not found");
                continue;
            }
            disabledSkills.add(st);
        }
    }

    public static boolean hasDisabledSkills() {
        return disabledSkills != null && !disabledSkills.isEmpty();
    }
}
