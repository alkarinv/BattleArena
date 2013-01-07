package mc.alk.arena.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.events.matches.TeamJoinedQueueEvent;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.signs.ArenaStatusSign;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SignController implements Listener{
	Map<String, Map<String,ArenaStatusSign>> statusSigns = new HashMap<String, Map<String,ArenaStatusSign>>();

	public void addStatusSign(ArenaStatusSign arenaStatusSign) {
		Map<String,ArenaStatusSign> map = getMatchSigns(arenaStatusSign.getType());
		map.put(toKey(arenaStatusSign.getLocation()), arenaStatusSign);
	}

	private Map<String, ArenaStatusSign> getMatchSigns(String arenaType) {
		Map<String,ArenaStatusSign> map = statusSigns.get(arenaType);
		if (map == null){
			map = Collections.synchronizedMap(new HashMap<String,ArenaStatusSign>());
			statusSigns.put(arenaType, map);
		}
		return map;
	}

	public String toKey(Location loc){
		return SerializerUtil.getBlockLocString(loc);
	}

	public Map<String, Map<String, ArenaStatusSign>> getStatusSigns() {
		return statusSigns;
	}

	public void updateAllSigns() {
		for (String arenaType : statusSigns.keySet()){
			updateAllSigns(arenaType);}
	}

	public void updateAllSigns(String arenaType) {
		Map<String,ArenaStatusSign> signs = statusSigns.get(arenaType);
		if (signs == null)
			return;
		for (ArenaStatusSign sign: signs.values()){

		}

	}

	@EventHandler
	public void onTeamJoinedQueueEvent(TeamJoinedQueueEvent event){
		ArenaParams params = event.getParams();
		Map<String,ArenaStatusSign> signs = statusSigns.get(params.getType().getName());
		if (signs == null)
			return;

		for (ArenaStatusSign sign: signs.values()){
			Location l = sign.getLocation();
			final Material type = l.getBlock().getState().getType();
			if (type != Material.SIGN_POST && type != Material.SIGN){
				continue;}
			Sign s = (Sign) l.getBlock().getState();
			s.setLine(3, event.getPos() +"/" + params.getMinTeams());
			s.update();
		}
	}

}
