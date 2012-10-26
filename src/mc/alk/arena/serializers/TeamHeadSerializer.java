package mc.alk.arena.serializers;

import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.Exceptions.ConfigException;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.arena.util.TeamUtil.TeamHead;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class TeamHeadSerializer extends BaseSerializer{

	public void loadAll(){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}
		loadTeams(config);
	}

	public static void loadTeams(ConfigurationSection cs) {
		if (cs == null){
			Log.info(BattleArena.getPName() +" has no teamHeads");
			return;}
		StringBuilder sb = new StringBuilder();
		List<String> keys = cs.getStringList("teams");
		boolean first = true;
		for (String teamStr : keys){
			String teamName;
			try {
				teamName = addTeamHead(teamStr);
			} catch (Exception e) {
				Log.err("Error parsing teamHead " + teamStr);
				e.printStackTrace();
				continue;
			}
			if (first) first = false;
			else sb.append(", ");
			sb.append(teamName);
		}
		if (first){
			Log.info(BattleArena.getPName() +" no predefined teamHeads found. inside of " + cs.getCurrentPath());
		}
	}


	private static String addTeamHead(String str) throws Exception {
		String[] split = str.split(",");
		if (split.length != 2){
			throw new ConfigException("Team Head must be in format 'Name,ItemStack'");
		}
		String name =MessageUtil.decolorChat(split[0]);
		if (name.isEmpty()){
			throw new ConfigException("Team Name must not be empty 'Name,ItemStack'");
		}
		ItemStack item = InventoryUtil.parseItem(split[1]);
		item.setAmount(1);
		TeamHead th = new TeamHead(item,split[0]);
		TeamUtil.addTeamHead(name,th);
		return name;
	}

}
