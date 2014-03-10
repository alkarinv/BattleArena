package mc.alk.arena.serializers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.SpawnController;
import mc.alk.arena.objects.options.SpawnOptions;
import mc.alk.arena.objects.options.SpawnOptions.SpawnOption;
import mc.alk.arena.objects.spawns.EntitySpawn;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnGroup;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.util.EntityUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class SpawnSerializer {
	public YamlConfiguration config = new YamlConfiguration();
	File f = new File(BattleArena.getSelf().getDataFolder()+"/spawns.yml");


	public void setConfig(File f){
		this.f = f;
		config = new YamlConfiguration();
		loadAll();
	}

	public void loadAll(){
		try {config.load(f);} catch (Exception e){Log.printStackTrace(e);}

		ConfigurationSection as = config.getConfigurationSection("spawnGroups");
		if (as == null){
			Log.info("spawn section is empty in config cs=" + config.getCurrentPath());
			return;
		}
		Set<String> keys = as.getKeys(false);
		for (String key: keys){
			SpawnGroup sg = parseSpawnGroup(as.getConfigurationSection(key));
			if (sg == null)
				continue;
			SpawnController.registerSpawn(sg.getName(), sg);
		}
	}

	private static SpawnGroup parseSpawnGroup(ConfigurationSection cs) {
		if (cs == null){
			//			System.out.println("parsing spawn group cs=" + cs);
			return null;
		}
		//		System.out.println("parsing spawn group " + cs.getName());
		List<SpawnInstance> spawns = getSpawnList(cs);
		SpawnGroup sg = new SpawnGroup(cs.getName());
		sg.addSpawns(spawns);
		return sg;
	}

	public static ArrayList<SpawnInstance> getSpawnList(ConfigurationSection cs) {
		//		System.out.println("getSpawnList cs=" + cs.getName() +"   curpath=" + cs.getCurrentPath());
		ArrayList<SpawnInstance> spawns = new ArrayList<SpawnInstance>();
		try {
			Set<String> keys = cs.getKeys(false);
			for (String key: keys){
				List<SpawnInstance> sis = parseSpawnable(convertToStringList(cs,key));
				if (sis != null){
					for (SpawnInstance si: sis)
						spawns.add(si);
				}
			}
		} catch (Exception e){
			Log.warn(cs.getCurrentPath() +" could not be parsed in config.yml");
			Log.printStackTrace(e);
		}
		return spawns;
	}

	public static List<String> convertToStringList(ConfigurationSection cs, String key) {
		List<String> args = new ArrayList<String>();
		args.add(key);
		args.addAll(convertToStringList(cs.getString(key)));
		return args;
	}

	public static List<String> convertToStringList(String str) {
		return new ArrayList<String>(Arrays.asList(str.split(" ")));
	}

	public static List<SpawnInstance> parseSpawnable(List<String> args) {
		final String key = args.get(0);
		StringBuilder sb = new StringBuilder(key);
		List<SpawnInstance> spawns = new ArrayList<SpawnInstance>();
		for (int i=1;i< args.size();i++){
			sb.append(" ").append(args.get(i));
		}
		final String value = sb.toString();
		try {
			SpawnInstance sg = SpawnController.getSpawnable(key);
			if (sg != null){
				int number = 1;
				try{number = Integer.parseInt(args.get(1));}catch (Exception e){}
				for (int i=0;i< number;i++)
					spawns.add(sg);
				return spawns;
			}
			int number = 1;
			try{number = Integer.parseInt(value);} catch(Exception e){}
//			System.out.println(InventoryUtil.isItem(key)+" is item " + InventoryUtil.isItem(key+":" + value) +"     key=" + key+" value=" + value);
			ItemStack is = InventoryUtil.parseItem(value);
            EntityType et = EntityUtil.parseEntityType(key);
//						System.out.println("is = " + InventoryUtil.getItemString(is) +"   et=" + et);
			if (is != null && et != null){
				int keysize = key.length();
				int isizedif = Math.abs(is.getType().name().length() - keysize);
				int esizedif = Math.abs(et.getName().length() - keysize);
				if (isizedif <= esizedif){
					spawns.add(new ItemSpawn(is));
				} else {
                    try{number = Integer.parseInt(args.get(args.size()-1));} catch(Exception e){}
					spawns.add(new EntitySpawn(et,number));
				}
				return spawns;
			} else if (is != null){
				spawns.add(new ItemSpawn(is));
				return spawns;
			}  else if (et != null){
                try{number = Integer.parseInt(args.get(args.size()-1));} catch(Exception e){}
				spawns.add(new EntitySpawn(et,number));
				return spawns;
			} else {
				String split[] = key.split(":");
				sg = SpawnController.getSpawnable(split[0]);
				number = 1;
				try {number = Integer.valueOf(split[1]);} catch(Exception e){}
				if (sg != null){
					for (int i=0;i< number;i++)
						spawns.add(sg);
					return spawns;
				}
			}
		} catch (Exception e){}
		return null;
	}

    public static TimedSpawn parseSpawn(String[] args) {
        List<String> spawnArgs = new ArrayList<String>();
        //		List<EditOption> optionArgs = new ArrayList<EditOption>();
        Integer fs = 0; /// first spawn time
        Integer rs = 30; /// Respawn time
        Integer ds = 0; /// Despawn time
        for (int i=1;i< args.length;i++){
            String arg = args[i];
            if (arg.contains("=")){
                String as[] = arg.split("=");
                Integer time = null;
                try{
                    time = Integer.valueOf(as[1]);
                } catch (Exception e){}
                if (as[0].equalsIgnoreCase("fs")){
                    fs = time;
                } else if (as[0].equalsIgnoreCase("rs") || as[0].equalsIgnoreCase("rt")){
                    rs = time;
                } else if (as[0].equalsIgnoreCase("ds")){
                    ds = time;
                }
            } else {
                spawnArgs.add(arg);
            }
        }
        int number = -1;
        if (spawnArgs.size() > 1){
            try {number = Integer.parseInt(spawnArgs.get(spawnArgs.size()-1));} catch(Exception e){}
        }
        if (number == -1){
            spawnArgs.add("1");}
        List<SpawnInstance> spawn = SpawnSerializer.parseSpawnable(spawnArgs);
        if (spawn == null){
            return null;
        }
        SpawnInstance si = spawn.get(0);
        if (si == null)
            return null;
        return new TimedSpawn(fs,rs,ds,si);
    }

    public static TimedSpawn createTimedSpawn(SpawnInstance si, SpawnOptions so) {
        long fs = (Integer) (so.options.containsKey(SpawnOption.FIRST_SPAWN) ? so.options.get(SpawnOption.FIRST_SPAWN) : 0);
        long rs = (Integer) (so.options.containsKey(SpawnOption.RESPAWN) ? so.options.get(SpawnOption.RESPAWN) : 30);
        if (fs < 0)
            fs = 0;
        if (rs <= 0)
            rs = 1;
        long ds = (Integer) (so.options.containsKey(SpawnOption.DESPAWN) ? so.options.get(SpawnOption.DESPAWN) : 0);
        return new TimedSpawn(fs,rs,ds,si);
    }
}
