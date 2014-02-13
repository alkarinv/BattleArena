package mc.alk.arena.objects.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnOptions {
    public HashMap<SpawnOption, Object> options = new HashMap<SpawnOption, Object>();
    public String remainingArgs[];

    public enum SpawnOption {
        FIRST_SPAWN, RESPAWN, DESPAWN
    }


    public static SpawnOptions parseSpawnOptions(String[] args) {
        HashMap<SpawnOption, Object> options = new HashMap<SpawnOption, Object>();
        List<String> spawnArgs = new ArrayList<String>();
        for (int i=1;i< args.length;i++){
            String arg = args[i];
            if (arg.contains("=")){
                String as[] = arg.split("=");
                Integer time = null;
                try{
                    time = Integer.valueOf(as[1]);
                } catch (Exception e) {
                    throw new IllegalStateException("time value " + time +" not valid. arg='" + arg+"'");
                }
                if (as[0].equalsIgnoreCase("fs")) {
                    options.put(SpawnOption.FIRST_SPAWN, time);
                } else if (as[0].equalsIgnoreCase("rs") || as[0].equalsIgnoreCase("rt")){
                    options.put(SpawnOption.RESPAWN, time);
                } else if (as[0].equalsIgnoreCase("ds")){
                    options.put(SpawnOption.DESPAWN, time);
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
