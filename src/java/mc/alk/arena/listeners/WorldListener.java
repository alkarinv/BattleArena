package mc.alk.arena.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import mc.alk.arena.objects.RegisteredCompetition;

import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {
	Map<RegisteredCompetition, List<String>> ext = null;
	AtomicBoolean loading = new AtomicBoolean();
	public void onWorldLoad(WorldLoadEvent e){
		if (ext == null || ext.isEmpty())
			return;
		loading.set(true);
		List<RegisteredCompetition> removeRC = new ArrayList<RegisteredCompetition>();
		for(Entry<RegisteredCompetition, List<String>> entry: ext.entrySet()){
			List<String> remove = new ArrayList<String>();
			for(String wstr : entry.getValue()){
				if(wstr.equalsIgnoreCase(e.getWorld().getName())){
					entry.getKey().reload();
					remove.add(wstr);
					break; /// only need to reload once
				}
			}
			entry.getValue().removeAll(remove);
			if (entry.getValue().isEmpty()){
				removeRC.add(entry.getKey());
			}
		}
		for (RegisteredCompetition rc : removeRC){
			ext.remove(rc);
		}
		loading.set(false);
	}

	public void addCompetition(RegisteredCompetition rc, String world){

	}
}
