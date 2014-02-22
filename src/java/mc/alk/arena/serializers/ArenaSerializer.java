package mc.alk.arena.serializers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.plugins.WorldGuardController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.arenas.Persistable;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.spawns.BlockSpawn;
import mc.alk.arena.objects.spawns.ChestSpawn;
import mc.alk.arena.objects.spawns.EntitySpawn;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnGroup;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.SpawnTime;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MinMax;
import mc.alk.arena.util.SerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArenaSerializer extends BaseConfig{
    static BattleArenaController arenaController;
    static HashMap<Plugin, Set<ArenaSerializer>> configs = new HashMap<Plugin, Set<ArenaSerializer>>();

    /// Which plugin does this ArenaSerializer belong to
    Plugin plugin;

    public static void setBAC(BattleArenaController bac){
        arenaController = bac;
    }

    public ArenaSerializer(Plugin plugin, File file){
        setConfig(file);
        this.plugin = plugin;

        config = new YamlConfiguration();
        Set<ArenaSerializer> paths = configs.get(plugin);
        if (paths == null){
            paths = new HashSet<ArenaSerializer>();
            configs.put(plugin, paths);
        } else { /// check to see if we have this path already
            for (ArenaSerializer as : paths){
                if (as.file.getPath().equals(this.file.getPath())){
                    return;}
            }
        }
        paths.add(this);
    }

    public static void loadAllArenas(){
        for (Plugin plugin: configs.keySet()){
            loadAllArenas(plugin);
        }
    }

    public static void loadAllArenas(Plugin plugin){
        for (ArenaSerializer serializer: configs.get(plugin)){
            serializer.loadArenas(plugin);
        }
    }

    public static void loadAllArenas(Plugin plugin, ArenaType arenaType){
        Set<ArenaSerializer> serializers = configs.get(plugin);
        if (serializers == null || serializers.isEmpty()){
            Log.err(plugin.getName() +" has no arenas to load");
            return;
        }

        for (ArenaSerializer serializer: serializers){
            serializer.loadArenas(plugin,arenaType);
        }
    }

    public void loadArenas(Plugin plugin){
        try {config.load(file);} catch (Exception e){Log.printStackTrace(e);}
        loadArenas(plugin, BattleArena.getBAController(), config,null);
    }

    public void loadArenas(Plugin plugin, ArenaType arenaType){
        try {config.load(file);} catch (Exception e){Log.printStackTrace(e);}
        loadArenas(plugin, BattleArena.getBAController(), config, arenaType);
    }

    protected void loadArenas(Plugin plugin, BattleArenaController bac, //ArenaSerializer arenaSerializer,
                              ConfigurationSection cs, ArenaType arenaType){
        final String pname = "["+plugin.getName()+"] ";
        if (cs == null){
            Log.info(pname+" " + arenaType + " has no arenas, cs is null");
            return;
        }

        ConfigurationSection as = cs.getConfigurationSection("arenas");
        ConfigurationSection bks = cs.getConfigurationSection("brokenArenas");
        if (as == null && bks == null){
            if (Defaults.DEBUG) Log.info(pname+" " + arenaType + " has no arenas, configSectionPath=" + cs.getCurrentPath());
            return;
        }

        List<String> keys = (as == null) ? new ArrayList<String>() : new ArrayList<String>(as.getKeys(false));
        int oldGoodSize = keys.size();
        Set<String> brokenKeys = bks == null ? new HashSet<String>() : bks.getKeys(false);
        int oldBrokenSize = brokenKeys.size();
        keys.addAll(brokenKeys);

        Set<String> brokenArenas = new HashSet<String>();
        Set<String> loadedArenas = new HashSet<String>();
        for (String name : keys){
            if (loadedArenas.contains(name) || brokenArenas.contains(name)) /// We already tried to load this arena
                continue;
            boolean broken = brokenKeys.contains(name);
            String section = broken ? "brokenArenas" : "arenas";
            if (arenaType != null){ /// Are we looking for 1 particular arena type to load
                String path = section+"."+name;
                String atype = cs.getString(path+".type",null);
                if (atype == null || !ArenaType.isSame(atype,arenaType)){
                    /// Its not the same type.. so don't let it affect the sizes of the arena counts
                    if (brokenArenas.remove(name)){
                        oldBrokenSize--;
                    } else{
                        oldGoodSize--;
                    }
                    continue;
                }
            }
            Arena arena = null;
            try{
                arena = loadArena(plugin, bac,cs.getConfigurationSection(section+"."+name));
                if (arena != null){
                    loadedArenas.add(arena.getName());
                    if (broken){
                        transfer(cs,"brokenArenas."+name, "arenas."+name);}
                }
            } catch(IllegalArgumentException e){
                Log.err(e.getMessage());
            } catch(Exception e){
                Log.printStackTrace(e);
            }
            if (arena == null){
                brokenArenas.add(name);
                if (!broken){
                    transfer(cs,"arenas."+name, "brokenArenas."+name);}
            }
        }
        if (!loadedArenas.isEmpty()) {
            Log.info(pname+"Loaded "+arenaType+" arenas: " + StringUtils.join(loadedArenas,", "));
        } else if (Defaults.DEBUG){
            Log.info(pname+"No arenas found for " + cs.getCurrentPath() +"  arenatype="+arenaType +"  cs="+cs.getName());
        }
        if (!brokenArenas.isEmpty()){
            Log.warn("&c"+pname+"&eFailed loading arenas: " + StringUtils.join(brokenArenas, ", ") + " arenatype="+arenaType +" cs="+cs.getName());
        }
        if (oldGoodSize != loadedArenas.size() || oldBrokenSize != brokenArenas.size()){
            try {
                config.save(file);
            } catch (IOException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private static void transfer(ConfigurationSection cs, String string, String string2) {
        try{
            Map<String,Object> map = new HashMap<String,Object>(cs.getConfigurationSection(string).getValues(false));
            cs.createSection(string2, map);
            cs.set(string,null);
        } catch(Exception e){
            Log.printStackTrace(e);
        }
    }

    public static Arena loadArena(Plugin plugin, BattleArenaController bac, ConfigurationSection cs) {
        String name = cs.getName().toLowerCase();

        ArenaType atype = ArenaType.fromString(cs.getString("type"));
        if (atype==null){
            Log.err(" Arena type not found for " + name);
            return null;
        }
        MatchParams mp = new MatchParams(atype);
        try {
            if (cs.contains("params"))
                mp = ConfigSerializer.loadMatchParams(plugin, atype, name, cs.getConfigurationSection("params"),true);
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        /// Get from the "old" way of specifying teamSize and nTeams
        if (cs.contains("teamSize")) {
            MinMax mm = MinMax.valueOf(cs.getString("teamSize"));
            mp.setTeamSizes(mm);
        }
        if (cs.contains("nTeams")) {
            MinMax mm = MinMax.valueOf(cs.getString("nTeams"));
            mp.setNTeams(mm);
        }

        if (!mp.valid()){
            Log.err( name + " This arena is not valid arenaq=" + mp.toString());
            return null;
        }

        Arena arena = ArenaType.createArena(name, mp,false);
        if (arena == null){
            Log.err("Couldnt load the Arena " + name);
            return null;
        }


        /// Spawns
        ConfigurationSection loccs = cs.getConfigurationSection("locations");
        Map<Integer,Location> locs = SerializerUtil.parseLocations(loccs);
        if (locs != null){
            for (Integer i: locs.keySet()){
                try{
                    arena.setSpawnLoc(i, locs.get(i));
                } catch(IllegalStateException e){
                    Log.printStackTrace(e);
                }
            }
        }

        /// Wait room spawns
        loccs = cs.getConfigurationSection("waitRoomLocations");
        locs = SerializerUtil.parseLocations(loccs);
        if (locs != null){
            for (Integer i: locs.keySet()){
                RoomController.addWaitRoom(arena, i, locs.get(i));}
        }

        /// Wait room spawns
        loccs = cs.getConfigurationSection("spectateLocations");
        locs = SerializerUtil.parseLocations(loccs);
        if (locs != null){
            for (Integer i: locs.keySet()){
                RoomController.addSpectate(arena, i, locs.get(i));}
        }

        /// Item/mob/group spawns
        ConfigurationSection spawncs = cs.getConfigurationSection("spawns");
        if (spawncs != null){
            for (String spawnStr : spawncs.getKeys(false)){
                ConfigurationSection scs = spawncs.getConfigurationSection(spawnStr);
                TimedSpawn s;
                try {
                    s = parseSpawnable(scs);
                } catch (IllegalArgumentException e) {
                    Log.printStackTrace(e);
                    continue;
                }
                if (s == null)
                    continue;
                arena.addTimedSpawn(Long.parseLong(spawnStr), s);
            }
        }
        cs = cs.getConfigurationSection("persistable");
        Persistable.yamlToObjects(arena, cs,Arena.class);
        updateRegions(arena);
        ArenaControllerInterface aci = new ArenaControllerInterface(arena);
        aci.init();
        bac.addArena(arena);

        if (arena.getParams().hasAnyOption(TransitionOption.ALWAYSOPEN)) {
            try {
                mp = arena.getParams();
                EventOpenOptions eoo = EventOpenOptions.parseOptions(new String[]{"COPYPARAMS"}, null, mp);
                Arena a = bac.reserveArena(arena);
                if (a == null){
                    Log.warn("&cArena &6"+arena.getName()+" &cwas set to always open but could not be reserved");
                } else{
                    eoo.setSecTillStart(0);
                    bac.createAndAutoMatch(arena, eoo);
                }
            } catch (NeverWouldJoinException e) {
                e.printStackTrace();
            } catch (InvalidOptionException e) {
                e.printStackTrace();
            }

        }
        return arena;
    }

    private static void updateRegions(Arena arena) {
        if (!WorldGuardController.hasWorldGuard())
            return;
        if (!arena.hasRegion())
            return;
        if (!WorldGuardController.hasRegion(arena.getWorldGuardRegion())){
            Log.err("Arena " + arena.getName() +" has a world guard region defined but it no longer exists inside of WorldGuard."+
                    "You will have to remake the region.  /arena alter <arena name> addregion");}
        MatchParams mp = ParamController.getMatchParamCopy(arena.getArenaType().getName());
        if (mp == null)
            return;
        MatchTransitions trans = mp.getTransitionOptions();
        if (trans == null)
            return;
        WorldGuardController.setFlag(arena.getWorldGuardRegion(), "entry", !trans.hasAnyOption(TransitionOption.WGNOENTER));
        try {
            WorldGuardController.trackRegion(arena.getWorldGuardRegion());
        } catch (RegionNotFound e) {
            Log.printStackTrace(e);
        }
    }

    private void saveArenas(boolean log) {
        ArenaSerializer.saveArenas(BattleArena.getBAController().getArenas().values(), file, config, plugin,log);
        try {
            config.save(file);
        } catch (IOException e) {
            Log.printStackTrace(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void saveArenas(Collection<Arena> arenas, File f, ConfigurationSection config, Plugin plugin, boolean log){
        ConfigurationSection maincs = config.createSection("arenas");
        config.createSection("brokenArenas");
        List<String> saved = new ArrayList<String>();
        for (Arena arena : arenas){
            String arenaname = null;
            try{
                arenaname = arena.getName();
                ArenaType at = arena.getArenaType();
                if (!at.getPlugin().getName().equals(plugin.getName()))
                    continue;
                ArenaParams parentParams = arena.getParams().getParent();
                arena.getParams().setParent(null);
                HashMap<String, Object> amap = new HashMap<String, Object>();
                amap.put("type", at.getName());

                /// Spawn locations
                Map<Integer, Location> mlocs = toMap(arena.getSpawns());
                Location mainSpawn = arena.getMainSpawn();
                if (mlocs != null || mainSpawn != null){
                    HashMap<String,String> locations = SerializerUtil.createSaveableLocations(mlocs);
                    if (mainSpawn!=null){
                        locations.put(String.valueOf(Defaults.MAIN_SPAWN),
                                SerializerUtil.getLocString(mainSpawn));}
                    amap.put("locations", locations);
                }

                /// Wait room spawns
                List<Location> llocs = arena.getWaitRoomSpawnLocs();
                mainSpawn = (arena.getWaitroom() != null ? arena.getWaitroom().getMainSpawn() : null);
                if (llocs!= null || mainSpawn != null){
                    mlocs = new HashMap<Integer,Location>();
                    if (llocs != null){
                        for (int i=0;i<llocs.size();i++){
                            if (llocs.get(i) != null)
                                mlocs.put(i, llocs.get(i));
                        }
                    }
                    HashMap<String,String> locations = SerializerUtil.createSaveableLocations(mlocs);
                    if (mainSpawn!=null){
                        locations.put(String.valueOf(Defaults.MAIN_SPAWN),
                                SerializerUtil.getLocString(mainSpawn));}

                    amap.put("waitRoomLocations", locations);
                }
                /// spectate locations
                llocs = arena.getSpectatorRoom() != null ? arena.getSpectatorRoom().getSpawns() : null;
                if (llocs!= null){
                    mlocs = new HashMap<Integer,Location>();
                    for (int i=0;i<llocs.size();i++){
                        if (llocs.get(i) != null)
                            mlocs.put(i, llocs.get(i));
                    }
                    HashMap<String,String> locations = SerializerUtil.createSaveableLocations(mlocs);
                    amap.put("spectateLocations", locations);
                }

                /// Timed spawns
                Map<Long, TimedSpawn> timedSpawns = arena.getTimedSpawns();
                if (timedSpawns != null && !timedSpawns.isEmpty()){
                    HashMap<String,Object> spawnmap = new HashMap<String,Object>();
                    for (Long key: timedSpawns.keySet() ){
                        TimedSpawn ts = timedSpawns.get(key);
                        HashMap<String,Object> itemSpawnMap = saveSpawnable(key, ts);
                        spawnmap.put(key.toString(), itemSpawnMap);
                    }
                    amap.put("spawns", spawnmap);
                }

                /// Visitor room spawns
                llocs = arena.getVisitorLocs();
                if (llocs!= null){
                    mlocs = new HashMap<Integer,Location>();
                    for (int i=0;i<llocs.size();i++){
                        if (llocs.get(i) != null)
                            mlocs.put(i, llocs.get(i));
                    }
                    HashMap<String,String> locations = SerializerUtil.createSaveableLocations(mlocs);
                    amap.put("waitRoomLocations", locations);
                }

                Map<String,Object> persisted = Persistable.objectsToYamlMap(arena, Arena.class);
                if (persisted != null && !persisted.isEmpty()){
                    amap.put("persistable", persisted);
                }
                saved.add(arenaname);

                ConfigurationSection arenacs = maincs.createSection(arenaname);
                SerializerUtil.expandMapIntoConfig(arenacs, amap);

                ConfigSerializer.saveParams(arena.getParams(), arenacs.createSection("params"), true);
                arena.getParams().setParent(parentParams);

                config.set("brokenArenas."+arenaname, null); /// take out any duplicate names in broken arenas
            } catch (Exception e){
                Log.printStackTrace(e);
                if (arenaname != null){
                    transfer(config, "arenas."+arenaname, "brokenArenas."+arenaname);
                }

            }
        }
        if (log)
            Log.info(plugin.getName() + " Saving arenas " + StringUtils.join(saved,",") +" to " +
                    f.getPath() + " configSection="+config.getCurrentPath()+"." + config.getName());

        //		SerializerUtil.expandMapIntoConfig(maincs, map);
    }

    private static Map<Integer, Location> toMap(List<Location> spawns) {
        if (spawns == null)
            return null;
        HashMap<Integer,Location> map = new HashMap<Integer,Location>();
        for (int i=0;i<spawns.size();i++)
            map.put(i, spawns.get(i));
        return map;
    }

    protected void saveArenas() {
        saveArenas(false);
    }
    @Override
    public void save() {
        this.saveArenas(true);
    }

    public static void saveAllArenas(boolean log){
        for (Plugin plugin: configs.keySet()){
            for (ArenaSerializer serializer: configs.get(plugin)){
                serializer.saveArenas(log);
            }
        }
    }

    public static void saveArenas(Plugin plugin){
        if (!configs.containsKey(plugin))
            return;
        for (ArenaSerializer serializer: configs.get(plugin)){
            serializer.saveArenas(true);
        }
    }

    private static TimedSpawn parseSpawnable(ConfigurationSection cs) throws IllegalArgumentException {
        if (!cs.contains("spawn") || !cs.contains("time") || !cs.contains("loc")){
            Log.err("configuration section cs = " + cs +"  is missing either spawn,time,or loc");
            return null;
        }
        SpawnTime st = parseSpawnTime(cs.getString("time"));
        Location loc = SerializerUtil.getLocation(cs.getString("loc"));
        List<String> strings = SpawnSerializer.convertToStringList(cs.getString("spawn"));
        if (strings == null || strings.isEmpty())
            return null;
        SpawnInstance si;
        if (cs.contains("type") && cs.getString("type").equalsIgnoreCase("block")){
            si = new BlockSpawn(loc.getBlock(),false);
            Material mat = Material.valueOf(cs.getString("spawn"));
            ((BlockSpawn)si).setMaterial(mat);
        }else if (cs.contains("type") && cs.getString("type").equalsIgnoreCase("chest")) {
            si = new ChestSpawn(loc.getBlock(), false);
            Material mat = Material.valueOf(cs.getString("spawn"));
            ((BlockSpawn)si).setMaterial(mat);
            List<ItemStack> items = InventoryUtil.getItemList(cs,"giveItems");
            if (items == null)
                items = new ArrayList<ItemStack>();
            ((ChestSpawn)si).setItems(items);
        } else {
            List<SpawnInstance> spawns = SpawnSerializer.parseSpawnable(strings);
            if (spawns == null || spawns.isEmpty())
                return null;

            spawns.get(0).setLocation(loc);
            si = spawns.get(0);
        }
        return new TimedSpawn(st.i1,st.i2, st.i3,si);
    }

    public static SpawnTime parseSpawnTime(String str){
        String strs[] = str.split(" ");
        Integer is[] = new Integer[strs.length];
        for (int i=0;i<strs.length;i++){
            is[i] = Integer.valueOf(strs[i]);
        }
        return new SpawnTime(is[0],is[1],is[2]);
    }

    private static HashMap<String, Object> saveSpawnable(Long i, TimedSpawn ts) {
        HashMap<String, Object> spawnMap = new HashMap<String,Object>();
        SpawnInstance si = ts.getSpawn();
        String key = null;
        String value =null;
        if (si instanceof SpawnGroup){
            SpawnGroup in = (SpawnGroup) si;
            key = in.getName();
            value =  "1";
        } else if (si instanceof ItemSpawn){
            ItemSpawn in = (ItemSpawn) si;
            key = InventoryUtil.getItemString(in.getItemStack());

        } else if (si instanceof EntitySpawn){
            EntitySpawn in = (EntitySpawn) si;
            key = in.getEntityString() + " " + in.getNumber();
        } else if (si instanceof ChestSpawn){
            ChestSpawn bs = (ChestSpawn) si;
            key = bs.getMaterial().name();
            ItemStack[] items = bs.getItems();
            spawnMap.put("type", "chest");
            if (items != null) {
                spawnMap.put("items", ConfigSerializer.getItems(Arrays.asList(items)));
            }
        } else if (si instanceof BlockSpawn){
            BlockSpawn bs = (BlockSpawn) si;
            spawnMap.put("type", "block");
            key = bs.getMaterial().name();
        }

        if (value == null)
            spawnMap.put("spawn", key);
        else
            spawnMap.put("spawn", key+":" + value);
        spawnMap.put("loc", SerializerUtil.getLocString(si.getLocation()));
        spawnMap.put("time", ts.getFirstSpawnTime() + " " + ts.getRespawnTime() + " " + ts.getTimeToDespawn());
        return spawnMap;
    }


}
