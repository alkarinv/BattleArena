package mc.alk.arena.serializers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.Log;
import mc.alk.plugin.updater.FileUpdater;
import mc.alk.plugin.updater.Version;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class YamlFileUpdater {
    private BufferedReader br = null;
    private BufferedWriter fw =null;
    private File tempFile = null;
    private File configFile = null;
    private File backupDir;

    public YamlFileUpdater(File backupDir){
        this.backupDir = backupDir;
        if (!backupDir.exists()){
            try{backupDir.mkdirs();}catch(Exception e){}
        }
    }

    public YamlFileUpdater(Plugin plugin){
        backupDir = new File(plugin.getDataFolder() +"/saves/backups");
        if (!backupDir.exists()){
            try{backupDir.mkdirs();}catch(Exception e){}
        }
    }

    public void updateMessageSerializer(Plugin plugin, MessageSerializer ms) {
        FileConfiguration fc = ms.getConfig();
        configFile = ms.getFile();
        Version version = new Version(fc.getString("version","0"));
        File dir = BattleArena.getSelf().getDataFolder();
        if (version.compareTo("1.5.1") < 0){
            messageTo1Point51(ms.getConfig(), version, new Version("1.5.1"));
            ms.setConfig(new File(dir+"/messages.yml"));
        } else if (version.compareTo("1.5.2") < 0){
            messageTo1Point52(ms.getConfig(), version, new Version("1.5.2"));
            ms.setConfig(new File(dir+"/messages.yml"));
        }
        version = new Version(fc.getString("version","0"));
        YamlFileUpdater yfu = new YamlFileUpdater(plugin);
        Version newVersion = new Version("1.5.3");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.delete(".*you_joined_the_queue.*");
            fu.delete(".*you_left_event.*");
            fu.replace(".*joined_the_queue:.*",
                    "    joined_the_queue: '&eYou joined the &6%s&e queue. Position: &6%s/%s'",
                    "    match_starts_players_or_time: '&eMatch start when &6%s&e more players join or in %s &ewith at least &6%s&e players'",
                    "    match_starts_when_time: '&eMatch start in %s'");
            fu.replace(".*you_left_match:.*","    you_left_competition: '&eYou have left the %s event'");
            fu.replace(".*you_left_queue:.*", "    you_left_queue: '&eYou have left the &6%s queue'");
            fu.replace(".*teammate_cant_join.*", "    teammate_cant_join: \"&cOne of your teammates can't add\"");
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.5.5");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.replace(".*joined_the_queue:.*",
                    "    joined_the_queue: '&eYou joined the &6%s&e queue.'",
                    "    position_in_queue: 'Position: &6%s/%s'");
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.0");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.addAfter(".*time_format:.*",
                    "    second: 'second'",
                    "    seconds: 'seconds'",
                    "    minute: 'minute'",
                    "    minutes: 'minutes'",
                    "    hour: 'hour'",
                    "    hours: 'hours'",
                    "    day: 'day'",
                    "    days: 'days'");
            fu.addAfter(".*your_team_not_ready.*",
                    "    added_to_team: '&6{playername} &ehas joined the team'",
                    "    onjoin: '&eYou have joined the &6{compname}'",
                    "    onjoin_server: '{prefix} &e&6%s&e has &2joined&e. There are &6{nplayers}&e inside'");
            fu.replaceAll("matchprefix","prefix");
            fu.replaceAll("eventprefix", "prefix");
            fu.replace(".*match_starts_when_time.*",
                    "    match_starts_when_time: '&eMatch starts in %s'");
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.1");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: " + newVersion);
            fu.addAfter(".*match_starts_players_or_time:.*",
                    "    match_starts_players_or_time2: '&eMatch starts in %s &ewith at least &6%s&e players'");
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.2");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.addAfter(".*you_joined_event:.*",
                    "    cancelled_lack_of_players: '{prefix} &cThe &6{matchname} &cwas cancelled because there were not enough players'");
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.3");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.addAfter(".*match_starts_players_or_time2.*",
                    "    match_starts_immediately: '&eMatch starts immediately with at least &6%s&e players'");
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.4");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.addAfter(".*server_onjoin.*",
                    "    interval_update: '{prefix} Game ends in {time}.'",
                    "    interval_update_winning: '&6{winner}&e leads with &6{winnerpointsfor} &ekills and &6{winnerpointsagainst} deaths'",
                    "    interval_update_tied: 'Is tied between &6{teams}'"
            );
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.5");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.replace(".*match_starts_plyers_or_time:.*",
                    "    match_starts_players_or_time: '&eMatch starts when &6%s&e more players join or in %s &ewith at least &6%s&e players'");
            fu.addAfter(".*match_starts_players_or_time2.*",
                    "    match_starts_players_or_time3: '&eMatch starts when &6%s&e more players join or in %s'"
            );
            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }
        newVersion = new Version("1.6.7");
        if (version.compareTo(newVersion) < 0){
            FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
            fu.replace("version:.*", "version: "+newVersion);
            fu.addAfter(".*cancelled_lack_of_players.*",
                    "    class_chosen: '&2You have chosen the &6%s'",
                    "    class_cant_switch_after_items: \"&cYou can't switch classes after changing items!\"",
                    "    class_wait_time: '&cYou must wait &6%s&c before changing your class again'",
                    "    class_you_are_already: '&cYou already are a &6&s'",
                    "    class_no_perms: \"&cYou don't have permissions to use the &6&s&c class!\"");

            try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
        }

        ms.setConfig(new File(dir+"/messages.yml"));
    }

    public static void updateBaseConfig(Plugin plugin, BAConfigSerializer bacs) {
        FileConfiguration fc = bacs.getConfig();
        Version version = new Version(fc.getString("configVersion","0"));
        YamlFileUpdater yfu = new YamlFileUpdater(plugin);
        yfu.configFile = bacs.getFile();
        File configFile = bacs.getFile();
        try{
            Version newVersion = new Version("2.2.5");
            if (version.compareTo(newVersion) < 0){
                version = to2Point25(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.2.6");
            if (version.compareTo(newVersion) < 0){
                version = to2Point26(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.2.7");
            if (version.compareTo(newVersion) < 0){
                version = to2Point27(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.2.8");
            if (version.compareTo(newVersion) < 0){
                version = to2Point28(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.2.9");
            if (version.compareTo(newVersion) < 0){
                version = to2Point29(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.3.0");
            if (version.compareTo(newVersion) < 0){
                version = to2Point30(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.3.1");
            if (version.compareTo(newVersion) < 0){
                version = to2Point31(version, yfu, configFile, newVersion);}
            newVersion = new Version("2.3.2");
            if (version.compareTo(newVersion) < 0){
                FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
                fu.replace("configVersion:.*", "configVersion: "+newVersion);
                fu.replace(".*which player commands should be disabled .*",
                        "# which player commands should be disabled when they enter an arena (use 'all' to disable everything)");
                fu.addAfter(".*disabledCommands.*", "",
                        "# which player commands will be allowed. commands specified here will work even if ('all') is specified above",
                        "enabledCommands: []");
                fu.replace(".*What commands should be disabled when the.*",
                        "# What commands should be disabled when the player is inside of a queue, but not in a match, (use 'all' to disable everything)");
                fu.addAfter(".*disabledQueueCommands.*", "",
                        "# which player commands will be allowed in a queue. commands specified here will work even if ('all') is specified above",
                        "enabledQueueCommands: []");
                fu.delete(".*secondsTillBegin:.*");
                try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
            }
            newVersion = new Version("2.3.3");
            if (version.compareTo(newVersion) < 0){
                FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
                fu.replace("configVersion:.*", "configVersion: "+newVersion);
                fu.addAfter(".*needSameItemsToChangeClass.*", "",
                        "# Use perms for join/leave signs.",
                        "useSignPerms: false");
                try {version = fu.update();} catch (IOException e) {Log.printStackTrace(e);}
            }
        } catch (IOException e){
            Log.printStackTrace(e);
        }

        bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
    }

    private static Version to2Point25(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.addAfter(".*defaultOptions.*",
                "    useScoreboard: true ### Use the scoreboard",
                "    useColoredNames: true  ## color team names (needs TagAPI or Scoreboard)","");
        return fu.update();
    }

    private static Version to2Point26(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.addAfter(".*allowRatedDuels.*",
                "    # default duel options to pass in. Example [rated,money=100]",
                "    defaultDuelOptions: []","");
        return fu.update();
    }

    private static Version to2Point27(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.addAfter(".*enableInvisibleTeleportFix.*", "",
                "# When using WorldGuard when you select an area with the worldguard wand and perform the action",
                "# /<game type> alter <arena name> addregion",
                "# what are the default WG flags that will be used",
                "# These can be changed by the region name, which are called, ba-<arena name> inside of WG",
                "defaultWGFlags:",
                "  build: false",
                "  mob-spawning: false", "");
        return fu.update();
    }

    private static Version to2Point28(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.addAfter(".*announceTimeTillNextEvent.*", "",
                "    announceGivenItemsOrClass: true ## When players are given items or a class tell them the items");
        return fu.update();
    }

    private static Version to2Point29(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.addAfter("teleportYOffset.*", "teleportYVelocity: 0");
        fu.delete("# If true if a player joins a.*");
        fu.delete("# afterwards the 1v1v1v1.*");
        fu.delete("# if false.  if after the 1v1.*");
        fu.delete(".*useArenasOnlyInOrder.*");
        fu.replace("## when set to true when a player .*");
        fu.replace("## start after the forceStartTime.*");
        fu.replace("## have joined.  Example: say .*");
        fu.replace("## the forceStartTime is exceeded *");
        fu.replace("matchEnableForceStart: true.*");

        fu.addBefore(".*matchForceStartTime.*", "",
                "    ## Default time before a match is started with the minimum amount of players");
        return fu.update();
    }

    private static Version to2Point30(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.addAfter("enableInvisibleTeleportFix: true.*", "",
                "# Check to make sure players have not used or dropped any items before letting them change classes",
                "needSameItemsToChangeClass: true");
        return fu.update();
    }

    private static Version to2Point31(Version version, YamlFileUpdater yfu, File configFile, Version newVersion) throws IOException {
        FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
        fu.replace("configVersion:.*", "configVersion: "+newVersion);
        fu.replace(".*Wiki:.*", "# Wiki: # https://wiki.github.com/alkarinv/BattleArena/wiki");
        fu.delete(".*Updates will be retrieved.*");
        fu.replace("autoUpdate:.*",
                "# Updates will be retrieved from the latest plugin on the bukkit site. Valid Options : none, release, beta, all",
                "# none (don't auto update)",
                "# release (only get release versions, ignore beta and alpha)",
                "# beta (get release and beta versions, ignore alpha)",
                "# all (get all new updates)",
                "autoUpdate: release",
                "",
                "# show newer versions. Valid Options: none, console, ops",
                "# none (don't show new versions)",
                "# console (show only to console log on startup)",
                "# ops (announce to ops on join, will only show this message once per server start)",
                "announceUpdate: console");
        return fu.update();
    }

    private void messageTo1Point51(FileConfiguration fc, Version version, Version newVersion) {
        Log.warn("BattleArena updating messages.yml to "+newVersion.getVersion());
        if (!openFiles())
            return;
        String line =null;
        try {
            while ((line = br.readLine()) != null){
                if (line.contains("version")){
                    fw.write("version: "+newVersion.getVersion()+"\n");
                } else if ((line.matches(".*countdownTillEvent:.*"))){
                    fw.write(line+"\n");
                    fw.write("    team_cancelled: ''\n");
                    fw.write("    server_cancelled: '&cEvent was cancelled'\n");
                } else {
                    fw.write(line+"\n");
                }
            }
            closeFiles();
            renameTo(configFile,new File(backupDir +"/"+configFile.getName()+newVersion.getVersion()));
            renameTo(tempFile,configFile);
        } catch (IOException e) {
            Log.printStackTrace(e);
        } finally{
            try {br.close();} catch (Exception e) {}
            try {fw.close();} catch (Exception e) {}
        }
    }

    private void messageTo1Point52(FileConfiguration fc, Version version, Version newVersion) {
        Log.warn("BattleArena updating messages.yml to "+newVersion.getVersion());
        if (!openFiles())
            return;
        String line =null;
        try {
            while ((line = br.readLine()) != null){
                if (line.contains("version")){
                    fw.write("version: "+newVersion.getVersion()+"\n");
                } else if ((line.matches(".*event_invalid_team_size.*"))){
                    fw.write(line+"\n");
                    fw.write("    you_joined_event: 'You have joined the %s'\n");
                } else {
                    fw.write(line+"\n");
                }
            }
            closeFiles();
            renameTo(configFile,new File(backupDir +"/"+configFile.getName()+newVersion.getVersion()));
            renameTo(tempFile,configFile);
        } catch (IOException e) {
            Log.printStackTrace(e);
        } finally{
            try {br.close();} catch (Exception e) {}
            try {fw.close();} catch (Exception e) {}
        }
    }

    private static void renameTo(File file1, File file2) throws IOException {
        FileUpdater.renameTo(file1, file2);
    }

    private boolean openFiles() {
        try {
            br = new BufferedReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            Log.printStackTrace(e);
            return false;
        }
        try {
            tempFile = new File(BattleArena.getSelf().getDataFolder()+"/temp.yml");
            fw = new BufferedWriter(new FileWriter(tempFile));
        } catch (IOException e) {
            Log.printStackTrace(e);
            try{br.close();}catch (Exception e2){}
            return false;
        }
        return true;
    }

    private void closeFiles() {
        try{
            fw.close();
            br.close();
        } catch(Exception e){
            Log.printStackTrace(e);
        }
    }

    public File move(String default_file, String config_file) {
        File file = new File(config_file);
        try{
            InputStream inputStream = getClass().getResourceAsStream(default_file);
            OutputStream out=new FileOutputStream(config_file);
            byte buf[]=new byte[1024];
            int len;
            while((len=inputStream.read(buf))>0){
                out.write(buf,0,len);}
            out.close();
            inputStream.close();
        } catch (Exception e){
        }
        return file;
    }


    public File getBackupDir() {
        return backupDir;
    }
}
