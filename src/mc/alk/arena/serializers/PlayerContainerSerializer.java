package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.controllers.LobbyController;
import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;


public class PlayerContainerSerializer extends BaseConfig{

	public void load(MatchParams params){
		ConfigurationSection cs = config.getConfigurationSection("lobbies."+params.getType());
		if (cs != null){
			List<String> strlocs = cs.getStringList("spawns");
			if (strlocs == null || strlocs.isEmpty())
				return;

			for (int i = 0;i< strlocs.size();i++){
				Location l = SerializerUtil.getLocation(strlocs.get(i));
				LobbyController.addLobby(params.getType(), i, l);
			}
		}
	}

	@Override
	public void save(){
		ConfigurationSection main = config.createSection("lobbies");
		Util.printStackTrace();
		for (LobbyContainer lobby: LobbyController.getLobbies()){
			HashMap<String, Object> amap = new HashMap<String, Object>();
			/// Spawn locations
			List<Location> locs = lobby.getSpawns();
			if (locs != null){
				List<String> strlocs =new ArrayList<String>();
				for (Location l : locs){
					strlocs.add(SerializerUtil.getLocString(l));}

				amap.put("spawns", strlocs);
			}
			main.set(lobby.getParams().getType().getName(), amap);
		}
		super.save();
	}

}
