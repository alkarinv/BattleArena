package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.signs.ArenaStatusSign;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

public class SignController{
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

	public Collection<ArenaStatusSign> getSigns(String arenaType) {
		Map<String,ArenaStatusSign> signs = statusSigns.get(arenaType);
		return signs == null ? null : signs.values();
	}

	@EventHandler
	public void onArenaPlayerEnterQueueEvent(ArenaPlayerEnterQueueEvent event){
		MatchParams mp = event.getQueueResult().params;
		if (mp == null)
			return;
	}

	@EventHandler
	public void onArenaPlayerLeaveQueueEvent(ArenaPlayerLeaveQueueEvent event){
		MatchParams mp = event.getParams();
		if (mp == null)
			return;
	}

	public void clearQueues() {
		Log.debug("---- clear queues");
		synchronized(statusSigns){
			for (String type: statusSigns.keySet()){
				for (ArenaStatusSign ass : statusSigns.get(type).values()){
					ass.setQ(0,0);
				}
			}
		}

	}
}
