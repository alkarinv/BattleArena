package mc.alk.arena.serializers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.TournamentEvent;
import mc.alk.arena.competition.match.ArenaMatch;
import mc.alk.arena.controllers.APIRegistrationController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.OptionSetController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.controllers.plugins.McMMOController;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.executors.DuelExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.TournamentExecutor;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.joining.ArenaMatchQueue;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.messaging.AnnouncementOptions.AnnouncementOption;
import mc.alk.arena.objects.options.DuelOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.victoryconditions.Custom;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.FileUtil;
import mc.alk.arena.util.KeyValue;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MinMax;
import mc.alk.plugin.updater.v1r6.PluginUpdater.AnnounceUpdateOption;
import mc.alk.plugin.updater.v1r6.PluginUpdater.UpdateOption;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BAConfigSerializer extends BaseConfig{

    public void loadDefaults(){
        try {config.load(file);} catch (Exception e){Log.printStackTrace(e);}
        EventParams defaults = new EventParams(ArenaType.register(Defaults.DEFAULT_CONFIG_NAME, Arena.class, BattleArena.getSelf()));
        VictoryType.register(Custom.class, BattleArena.getSelf());
        defaults.setVictoryType(VictoryType.getType(Custom.class));
        defaults.setName(Defaults.DEFAULT_CONFIG_NAME);
        defaults.setCommand(Defaults.DEFAULT_CONFIG_NAME);

        ParamController.addMatchParams(defaults);

        parseDefaultOptions(config.getConfigurationSection("defaultOptions"),defaults);
        if (!Defaults.MONEY_SET)
            Defaults.MONEY_STR = config.getString("moneyName",Defaults.MONEY_STR);
        UpdateOption o = UpdateOption.fromString(config.getString("autoUpdate", "none"));
        Defaults.AUTO_UPDATE =  o != null ? o : UpdateOption.NONE;
        AnnounceUpdateOption ao = AnnounceUpdateOption.fromString(config.getString("announceUpdate", "none"));
        Defaults.ANNOUNCE_UPDATE =  ao != null ? ao : AnnounceUpdateOption.NONE;
        Defaults.TELEPORT_Y_OFFSET = config.getDouble("teleportYOffset", Defaults.TELEPORT_Y_OFFSET);
        Defaults.TELEPORT_Y_VELOCITY = config.getDouble("teleportYVelocity", Defaults.TELEPORT_Y_VELOCITY);
        Defaults.NUM_INV_SAVES = config.getInt("numberSavedInventories", Defaults.NUM_INV_SAVES);
        Defaults.USE_SIGN_PERMS = config.getBoolean("useSignPerms", Defaults.USE_SIGN_PERMS);
        Defaults.ITEMS_IGNORE_STACKSIZE = config.getBoolean("ignoreMaxStackSize", Defaults.ITEMS_IGNORE_STACKSIZE);
        Defaults.ITEMS_UNSAFE_ENCHANTMENTS = config.getBoolean("unsafeItemEnchants", Defaults.ITEMS_UNSAFE_ENCHANTMENTS);
        Defaults.USE_ARENAS_ONLY_IN_ORDER = config.getBoolean("useArenasOnlyInOrder", Defaults.USE_ARENAS_ONLY_IN_ORDER);
        Defaults.ENABLE_TELEPORT_FIX = config.getBoolean("enableInvisibleTeleportFix", Defaults.ENABLE_TELEPORT_FIX);
        Defaults.ANNOUNCE_GIVEN_ITEMS = config.getBoolean("announceGivenItemsOrClass", Defaults.ANNOUNCE_GIVEN_ITEMS);
        Defaults.NEED_SAME_ITEMS_TO_CHANGE_CLASS = config.getBoolean("needSameItemsToChangeClass", Defaults.NEED_SAME_ITEMS_TO_CHANGE_CLASS);
        parseOptionSets(config.getConfigurationSection("optionSets"));
        ArenaMatch.setDisabledCommands(config.getStringList("disabledCommands"));
        ArenaMatch.setEnabledCommands(config.getStringList("enabledCommands"));
        ArenaMatchQueue.setDisabledCommands(config.getStringList("disabledQueueCommands"));
        ArenaMatchQueue.setEnabledCommands(config.getStringList("enabledQueueCommands"));
        loadOtherFiles();

        if (Defaults.TESTSERVER)
            return;
        ModuleLoader ml = new ModuleLoader();
        ml.loadModules(BattleArena.getSelf().getModuleDirectory());
    }

    public void loadCompetitions(){
        try {config.load(file);} catch (Exception e){Log.printStackTrace(e);}
        Set<String> defaultMatchTypes = new HashSet<String>(Arrays.asList(
                new String[] {"Arena","Skirmish","Colosseum","Battleground", "Duel"}));
        Set<String> defaultEventTypes = new HashSet<String>(Arrays.asList(new String[] {"FreeForAll","DeathMatch"}));
        Set<String> exclude = new HashSet<String>(Arrays.asList(new String[] {}));

        Set<String> allTypes = new HashSet<String>(defaultMatchTypes);
        allTypes.addAll(defaultEventTypes);
        JavaPlugin plugin = BattleArena.getSelf();

        APIRegistrationController api = new APIRegistrationController();
        ArenaType.register("Tourney", Arena.class, plugin);

        File dir = plugin.getDataFolder();
        File compDir = new File(dir+"/competitions");

        /// Load all default types
        for (String comp : allTypes){
            /// For some reason this next line is almost directly in APIRegistration and works
            /// for extensions but not for BattleArena defaults.
            /// ONLY doesnt work in Windows... odd...
            FileUtil.load(BattleArena.getSelf().getClass(),dir.getPath()+"/competitions/"+comp+"Config.yml",
                    "/default_files/competitions/"+comp+"Config.yml");
            String capComp = StringUtils.capitalize(comp);
            CustomCommandExecutor executor = comp.equalsIgnoreCase("duel") ? new DuelExecutor() : null;
            api.registerCompetition(plugin, capComp, capComp, Arena.class, executor,
                    new File(compDir+"/"+capComp+"Config.yml"),
                    new File(compDir+"/"+capComp+"Messages.yml"),
                    new File("/default_files/competitions/"+capComp+"Config.yml"),
                    new File(dir.getPath()+"/saves/arenas.yml"));
            exclude.add(capComp+"Config.yml");
        }

        /// These commands arent specified in the config, so manually add.
        ArenaType.addAliasForType("FreeForAll","ffa");
        ArenaType.addAliasForType("DeathMatch","dm");
        ArenaType.addAliasForType("Colosseum","col");

        /// And lastly.. add our tournament which is different than the rest
        createTournament(plugin, dir);
    }

    private void createTournament(JavaPlugin plugin, File dir) {
        File cf = FileUtil.load(BattleArena.getSelf().getClass(),dir.getPath()+"/competitions/TourneyConfig.yml",
                "/default_files/competitions/TourneyConfig.yml");
        ConfigSerializer cs = new ConfigSerializer(plugin,cf, "Tourney");
        MatchParams mp;
        try {
            mp = cs.loadMatchParams();
            EventParams ep = new EventParams(mp);
            TournamentEvent tourney = new TournamentEvent(ep);
            EventController.addEvent(tourney);
            try{
                EventExecutor executor = new TournamentExecutor();
                BattleArena.getSelf().getCommand("tourney").setExecutor(executor);
                EventController.addEventExecutor(tourney.getParams(), executor);
                ParamController.addMatchParams(ep);
            } catch (Exception e){
                Log.err("Tourney could not be added");
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    protected static void parseDefaultOptions(ConfigurationSection cs, EventParams defaults) {
        if (cs == null){
            Log.err("[BA Error] defaultConfig section not found!!! Using default settings" );
            return;
        }

        defaults.setTimeBetweenRounds(cs.getInt("timeBetweenRounds",Defaults.TIME_BETWEEN_ROUNDS));

        Defaults.USE_SCOREBOARD = cs.getBoolean("useScoreboard", Defaults.USE_SCOREBOARD);
        Defaults.USE_COLORNAMES = cs.getBoolean("useColoredNames", Defaults.USE_COLORNAMES);

        Defaults.SECONDS_TILL_MATCH = cs.getInt("secondsTillMatch", Defaults.SECONDS_TILL_MATCH);
        defaults.setSecondsTillMatch(Defaults.SECONDS_TILL_MATCH);

        Defaults.SECONDS_TO_LOOT = cs.getInt("secondsToLoot", Defaults.SECONDS_TO_LOOT);
        defaults.setSecondsToLoot(Defaults.SECONDS_TO_LOOT);

        Defaults.MATCH_TIME = cs.getInt("matchTime", Defaults.MATCH_TIME);
        defaults.setMatchTime(Defaults.MATCH_TIME);

        Defaults.MATCH_UPDATE_INTERVAL = cs.getInt("matchUpdateInterval", Defaults.MATCH_UPDATE_INTERVAL);
        defaults.setIntervalTime(Defaults.MATCH_UPDATE_INTERVAL);

        Defaults.AUTO_EVENT_COUNTDOWN_TIME = cs.getInt("eventCountdownTime",Defaults.AUTO_EVENT_COUNTDOWN_TIME);
        defaults.setSecondsTillStart(Defaults.AUTO_EVENT_COUNTDOWN_TIME);

        Defaults.ANNOUNCE_EVENT_INTERVAL = cs.getInt("eventCountdownInterval", Defaults.ANNOUNCE_EVENT_INTERVAL);
        defaults.setAnnouncementInterval(Defaults.ANNOUNCE_EVENT_INTERVAL);
        if (cs.contains("playerOpenOptions")){
            defaults.setPlayerOpenOptions(cs.getStringList("playerOpenOptions"));}

        parseOnServerStartOptions(cs);
        AnnouncementOptions an = new AnnouncementOptions();
        parseAnnouncementOptions(an,true,cs.getConfigurationSection("announcements"), true);
        parseAnnouncementOptions(an,false,cs.getConfigurationSection("eventAnnouncements"),true);
        AnnouncementOptions.setDefaultOptions(an);
        defaults.setAnnouncementOptions(an);

        Defaults.MATCH_FORCESTART_TIME = cs.getInt("matchForceStartTime", Defaults.MATCH_FORCESTART_TIME);
        defaults.setForceStartTime(Defaults.MATCH_FORCESTART_TIME);

        Defaults.TIME_BETWEEN_CLASS_CHANGE = cs.getInt("timeBetweenClassChange", Defaults.TIME_BETWEEN_CLASS_CHANGE);

        Defaults.DUEL_ALLOW_RATED = cs.getBoolean("allowRatedDuels", Defaults.DUEL_ALLOW_RATED);
        Defaults.DUEL_CHALLENGE_ADMINS = cs.getBoolean("allowChallengeOps", Defaults.DUEL_CHALLENGE_ADMINS);
        Defaults.DUEL_CHALLENGE_INTERVAL = cs.getInt("challengeInterval", Defaults.DUEL_CHALLENGE_INTERVAL);

        Defaults.TIME_BETWEEN_SCHEDULED_EVENTS = cs.getInt("timeBetweenScheduledEvents", Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
        Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT = cs.getBoolean("announceTimeTillNextEvent", Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT);

        Defaults.ALLOW_PLAYER_EVENT_CREATION = cs.getBoolean("allowPlayerCreation", Defaults.ALLOW_PLAYER_EVENT_CREATION);

        Defaults.ENABLE_PLAYER_READY_BLOCK = cs.getBoolean("enablePlayerReadyBlock", Defaults.ENABLE_PLAYER_READY_BLOCK);
        try {
            Material value = Material.matchMaterial(cs.getString("readyBlockType", Defaults.READY_BLOCK.name()).toUpperCase());
            if (value != null){
                Defaults.READY_BLOCK = value;}
        } catch (Exception e) { /* do nothing*/}

        defaults.setWaitroomClosedWhileRunning(true);
        defaults.setCancelIfNotEnoughPlayers(false);
        defaults.setRated(true);
        defaults.setUseTrackerPvP(false);
        defaults.setTeamRating(false);
        defaults.setUseTrackerMessages(true);
        defaults.setNLives(1);
        defaults.setTeamSizes(new MinMax(1,ArenaSize.MAX));
        defaults.setNTeams(new MinMax(2,ArenaSize.MAX));
        defaults.setArenaCooldown(cs.getInt("arenaCooldown",1));
        defaults.setAllowedTeamSizeDifference(cs.getInt("allowedTeamSizeDifference",1));

        defaults.setNConcurrentCompetitions(ArenaSize.toInt(cs.getString("nConcurrentCompetitions","infinite")));

        List<String> list = cs.getStringList("defaultDuelOptions");
        if (list != null && !list.isEmpty()){
            try {
                DuelOptions dop = DuelOptions.parseOptions(list.toArray(new String[list.size()]));
                DuelOptions.setDefaults(dop);
            } catch (InvalidOptionException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private static void parseOnServerStartOptions( ConfigurationSection cs) {
        if (cs ==null || !cs.contains("onServerStart")){
            Log.warn(BattleArena.getPluginName() +" No onServerStart options found");
            return;
        }
        List<String> options = cs.getStringList("onServerStart");
        for (String op : options){
            if (op.equalsIgnoreCase("startContinuous")) Defaults.START_CONTINUOUS = true;
            else if (op.equalsIgnoreCase("startNext")) Defaults.START_NEXT = true;
        }
    }

    private void parseOptionSets(ConfigurationSection cs) {
        if (cs != null){
            Set<String> keys = cs.getKeys(false);
            if (keys != null){
                for (String key : keys){
                    /// dont let people override defaults
                    if (key.equalsIgnoreCase("storeAll") || key.equalsIgnoreCase("restoreAll")){
                        Log.err("You can't override the default 'storeAll' and 'restoreAll'");
                        continue;
                    }
                    try {
                        TransitionOptions to = ConfigSerializer.getTransitionOptions(cs.getConfigurationSection(key));
                        if (to != null){
                            OptionSetController.addOptionSet(key, to);}
                    } catch (Exception e) {
                        Log.err("Couldn't parse optionSet=" + key);
                        Log.printStackTrace(e);
                    }
                }
            }
        }

        try{
            TransitionOptions tops = new TransitionOptions();
            tops.addOption(TransitionOption.STOREEXPERIENCE);
            tops.addOption(TransitionOption.STOREGAMEMODE);
            tops.addOption(TransitionOption.STOREHEROCLASS);
            tops.addOption(TransitionOption.STOREHEALTH);
            tops.addOption(TransitionOption.STOREHUNGER);
            tops.addOption(TransitionOption.STOREMAGIC);
            tops.addOption(TransitionOption.CLEARINVENTORY);
            tops.addOption(TransitionOption.CLEAREXPERIENCE);
            tops.addOption(TransitionOption.STOREITEMS);
            tops.addOption(TransitionOption.DEENCHANT);
            tops.addOption(TransitionOption.GAMEMODE,GameMode.SURVIVAL);
            tops.addOption(TransitionOption.FLIGHTOFF);
            OptionSetController.addOptionSet("storeAll", tops);

            tops = new TransitionOptions();
            tops.addOption(TransitionOption.RESTOREEXPERIENCE);
            tops.addOption(TransitionOption.RESTOREGAMEMODE);
            tops.addOption(TransitionOption.RESTOREHEROCLASS);
            tops.addOption(TransitionOption.RESTOREHEALTH);
            tops.addOption(TransitionOption.RESTOREHUNGER);
            tops.addOption(TransitionOption.RESTOREMAGIC);
            tops.addOption(TransitionOption.RESTOREITEMS);
            tops.addOption(TransitionOption.CLEARINVENTORY);
            tops.addOption(TransitionOption.DEENCHANT);
            OptionSetController.addOptionSet("restoreAll", tops);
        } catch (Exception e){
            Log.err("Couldn't set default setOptions");
        }
    }
    public static AnnouncementOptions parseAnnouncementOptions(AnnouncementOptions an , boolean match, ConfigurationSection cs, boolean warn) {
        if (cs == null){
            if (warn)
                Log.err((match? "match" : "event" ) + " announcements are null. cs= ");
            return null;
        }
        Set<String> keys = cs.getKeys(false);
        if (keys != null){
            for (String key: keys){
                MatchState ms = MatchState.fromString(key);
                if (ms == null){
                    Log.err("Couldnt recognize matchstate " + key +" in the announcement options");
                    continue;
                }
                List<String> list = cs.getStringList(key);
                for (String s: list){
                    KeyValue<String,String> kv = KeyValue.split(s,"=");
                    AnnouncementOption bo = AnnouncementOption.fromName(kv.key);
                    if (bo == null){
                        Log.err("Couldnt recognize AnnouncementOption " + s);
                        continue;
                    }
                    an.setBroadcastOption(match, ms, bo,kv.value);
                }
            }
        }
        return an;
    }

    private void loadOtherFiles() {
        loadHeroes();
        loadMcMMO();
    }


    public void loadVictoryConditions() {
        for (VictoryType vt : VictoryType.values()) {
            String name = vt.getName();
            if (name.equalsIgnoreCase("HighestKills")) { /// Old name for PlayerKills
                name = "PlayerKills";}
            BaseConfig c = loadOtherConfig(BattleArena.getSelf().getDataFolder() +
                    "/victoryConditions/" + name + ".yml");
            if (c == null)
                continue;
            VictoryType.addConfig(vt, c);
        }
    }

    private BaseConfig loadOtherConfig(String file){
        File f = new File(file);
        if (!f.exists()) /// File not found, get outta here
            return null;
        return new BaseConfig(f);
    }

    private ConfigurationSection loadOtherConfigSection(String file) {
        BaseConfig c = loadOtherConfig(file);
        return c == null ? null : c.getConfig();
    }

    public ConfigurationSection getWorldGuardConfig(){
        /// Look for it in the old location first, config.yml
        if (config.contains("defaultWGFlags")) {
            return config;
        } else {
            return loadOtherConfigSection(BattleArena.getSelf().getDataFolder() +
                    "/otherPluginConfigs/WorldGuardConfig.yml");
        }
    }

    private void loadHeroes(){
        if (HeroesController.enabled()){
            /// Look for it in the old location first, config.yml
            List<String> disabled = config.getStringList("disabledHeroesSkills");
            if (disabled != null && !disabled.isEmpty()){
                HeroesController.addDisabledCommands(disabled);
            } else { /// look for options in the new config
                ConfigurationSection cs = loadOtherConfigSection(BattleArena.getSelf().getDataFolder() +
                        "/otherPluginConfigs/HeroesConfig.yml");
                if (cs == null)
                    return;
                disabled = cs.getStringList("disabledSkills");
                if (disabled != null && !disabled.isEmpty()) {
                    HeroesController.addDisabledCommands(disabled);
                }
            }
        }
    }

    private void loadMcMMO(){
        if (McMMOController.enabled()){
            /// Look for it in the old location first, config.yml
            ConfigurationSection cs = loadOtherConfigSection(BattleArena.getSelf().getDataFolder() +
                    "/otherPluginConfigs/McMMOConfig.yml");
            if (cs == null)
                return;
            List<String> disabled = cs.getStringList("disabledSkills");
            if (disabled != null && !disabled.isEmpty()) {
                McMMOController.setDisabledSkills(disabled);
            }
        }
    }
}
