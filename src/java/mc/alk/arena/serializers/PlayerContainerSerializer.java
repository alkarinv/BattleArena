package mc.alk.arena.serializers;

import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.spawns.FixedLocation;
import mc.alk.arena.objects.spawns.SpawnLocation;
import mc.alk.arena.util.SerializerUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayerContainerSerializer extends BaseConfig{

	public void load(MatchParams params){
		ConfigurationSection cs = config.getConfigurationSection("lobbies."+params.getType());
		if (cs != null){
			List<String> strlocs = cs.getStringList("spawns");
			if (strlocs == null || strlocs.isEmpty())
				return;

			for (int i = 0;i< strlocs.size();i++){
				Location l = SerializerUtil.getLocation(strlocs.get(i));
				RoomController.addLobby(params.getType(), i,0, new FixedLocation(l));
			}
		}
	}

	@Override
	public void save(){
		ConfigurationSection main = config.createSection("lobbies");
		for (RoomContainer lobby: RoomController.getLobbies()){
			HashMap<String, Object> amap = new HashMap<String, Object>();
			/// Spawn locations
			List<List<SpawnLocation>> locs = lobby.getSpawns();
			if (locs != null) {
                Map<String, List<String>> strlocs =  SerializerUtil.createSaveableLocations(SerializerUtil.toMap(locs));
                amap.put("spawns", strlocs);
            }
            main.set(lobby.getParams().getType().getName(), amap);
		}
		super.save();
	}

}
