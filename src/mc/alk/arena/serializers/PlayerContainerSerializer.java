package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.controllers.LobbyController;
import mc.alk.arena.controllers.containers.LobbyContainer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;


public class PlayerContainerSerializer extends BaseConfig{

	public void load(MatchParams params){
		ConfigurationSection cs = config.getConfigurationSection("lobbies."+params.getType());
//		Log.debug("####222 K EY  " + params.getType() +"    " + params + "    " + cs);
		if (cs != null){
			List<String> strlocs = cs.getStringList("spawns");
			if (strlocs == null || strlocs.isEmpty())
				return;

			for (int i = 0;i< strlocs.size();i++){
				Location l = SerializerUtil.getLocation(strlocs.get(i));
//				Log.debug("@@@@@@@@  " +params.getType()  +"   " + l);
				LobbyController.addLobby(params.getType(), i, l);
			}
		}
	}

	@Override
	public void save(){
		ConfigurationSection main = config.createSection("lobbies");
		Util.printStackTrace();
		//		HashMap<String, Object> map = new HashMap<String, Object>();
		for (LobbyContainer lobby: LobbyController.getLobbies()){
			Log.debug(" --- saving  "  + lobby +"    " + lobby.getDisplayName() +"   " + lobby.getParams());
			//			ConfigurationSection cs = main.createSection(lobby.getParams().getType().getName());
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
