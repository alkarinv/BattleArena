package mc.alk.arena.objects.options;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnOptions {
    public HashMap<SpawnOption, Object> options = new HashMap<SpawnOption, Object>();
    public String remainingArgs[];

    public enum SpawnOption {
        FIRST_SPAWN, RESPAWN, DESPAWN, DESPAWNMATERIAL
    }


    public static SpawnOptions parseSpawnOptions(String[] args) {
        HashMap<SpawnOption, Object> options = new HashMap<SpawnOption, Object>();
        List<String> spawnArgs = new ArrayList<String>();
        for (int i=1;i< args.length;i++){
            String arg = args[i];
            if (arg.contains("=")){
                String as[] = arg.split("=");
                SpawnOption so = null;
                try {
                     so = SpawnOption.valueOf(as[0].toUpperCase());
                } catch (Exception e ){
                    /* do nothing */
                }
                if (so ==null || so != SpawnOption.DESPAWNMATERIAL){
                    Integer time;
                    try{
                        time = Integer.valueOf(as[1]);
                    } catch (Exception e) {
                        throw new IllegalStateException("time value not valid. arg='" + arg+"'");
                    }
                    if (as[0].equalsIgnoreCase("fs")) {
                        options.put(SpawnOption.FIRST_SPAWN, time);
                    } else if (as[0].equalsIgnoreCase("rs") || as[0].equalsIgnoreCase("rt")){
                        options.put(SpawnOption.RESPAWN, time);
                    } else if (as[0].equalsIgnoreCase("ds")){
                        options.put(SpawnOption.DESPAWN, time);
                    }
                } else {
                    try {
                        Material m = Material.valueOf(as[1]);
                        options.put(SpawnOption.DESPAWNMATERIAL, m);
                    } catch (Exception e ){
                        throw new IllegalStateException("Material value not valid. arg='" + arg+"'");
                    }
                }
            } else {
                spawnArgs.add(arg);
            }
        }
        SpawnOptions po = new SpawnOptions();
        po.options = options;
        po.remainingArgs = spawnArgs.toArray(new String[spawnArgs.size()]);
        return po;
    }
}
