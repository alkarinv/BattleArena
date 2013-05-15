package mc.alk.arena.serializers;

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
import java.util.Arrays;
import java.util.HashSet;

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.Log;
import mc.alk.plugin.updater.v1r2.FileUpdater;
import mc.alk.plugin.updater.v1r2.Version;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class YamlFileUpdater {
	BufferedReader br = null;
	BufferedWriter fw =null;
	File tempFile = null;
	File configFile = null;
	File backupDir;

	public YamlFileUpdater(File backupDir){
		this.backupDir = backupDir;
		if (!backupDir.exists()){
			backupDir.mkdirs();}
	}

	public YamlFileUpdater(Plugin plugin){
		backupDir = new File(plugin.getDataFolder() +"/saves/backups");
		if (!backupDir.exists()){
			backupDir.mkdirs();}
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
			fu.replace(".*teammate_cant_join.*", "    teammate_cant_join: \"&cOne of your teammates can't join\"");
			try {version = fu.update();} catch (IOException e) {e.printStackTrace();}
		}
		newVersion = new Version("1.5.5");
		if (version.compareTo(newVersion) < 0){
			FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
			fu.replace("version:.*", "version: "+newVersion);
			fu.replace(".*joined_the_queue:.*",
					"    joined_the_queue: '&eYou joined the &6%s&e queue.'",
					"    position_in_queue: 'Position: &6%s/%s'");
			try {version = fu.update();} catch (IOException e) {e.printStackTrace();}
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
			fu.replaceAll("eventprefix","prefix");
			fu.replace(".*match_starts_when_time.*",
					"    match_starts_when_time: '&eMatch starts in %s'");
			try {version = fu.update();} catch (IOException e) {e.printStackTrace();}
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
			if (version.compareTo("1.7.3")<0){
				yfu.to1Point73(bacs, bacs.getConfig(), version);}
			if (version.compareTo("2.0")<0){
				yfu.to2Point0(BattleArena.getSelf(), bacs.getConfig(), version);
				bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
			}
			if (version.compareTo("2.0.5")<0){
				yfu.to2Point05(bacs, bacs.getConfig(), version);}
			if (version.compareTo("2.1.0")<0){
				yfu.to2Point10(bacs, bacs.getConfig(), version, new Version("2.1.0"));}
			if (version.compareTo("2.1.1")<0){
				yfu.to2Point11(configFile, version, new Version("2.1.1"));}

			Version newVersion = new Version("2.1.2");
			if (version.compareTo(newVersion) < 0){
				version = to2Point12(version, yfu, configFile, newVersion);}

			newVersion = new Version("2.1.4");
			if (version.compareTo(newVersion) < 0){
				version = to2Point14(version, yfu, configFile, newVersion);}

			newVersion = new Version("2.2");
			if (version.compareTo(newVersion) < 0){
				version = to2Point2(version, yfu, configFile, newVersion);}
			newVersion = new Version("2.2.5");
			if (version.compareTo(newVersion) < 0){
				version = to2Point25(version, yfu, configFile, newVersion);}
			newVersion = new Version("2.2.6");
			if (version.compareTo(newVersion) < 0){
				version = to2Point26(version, yfu, configFile, newVersion);}

		} catch (IOException e){
			e.printStackTrace();
		}

		bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
	}


	private void to1Point73(BaseConfig bacs, FileConfiguration fc, Version version) {
		Version newVersion = new Version("1.7.3");
		Log.warn("BattleArena updating config to "+newVersion.getVersion());
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: "+newVersion.getVersion()+"\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*useArenasOnlyInOrder:.*"))){
					fw.write(line+"\n\n");
					fw.write("### Option sets allow you to give an easy to remember name for a group of options\n");
					fw.write("# you can add as many of your own as you want\n");
					fw.write("# there are two hidden defaults. storeAll, and restoreAll that can not be overridden\n");
					fw.write("# storeAll: with options [storeExperience, storeGamemode, storeHealth, storeHunger, storeItems, storeHeroclass, storeMagic, clearExperience, clearInventory, deEnchant]\n");
					fw.write("# restoreAll: with options [restoreExperience, restoreGamemode, restoreHealth, restoreHunger, restoreItems, restoreHeroclass, restoreMagic, clearInventory, deEnchant]\n");
					fw.write("optionSets:\n");
					fw.write("  storeAll1: \n");
					fw.write("      options: [storeExperience, storeGamemode, storeHealth, storeHunger, storeItems, storeHeroclass, storeMagic, clearExperience, clearInventory, deEnchant]\n");
					fw.write("  restoreAll1:\n");
					fw.write("      options: [restoreExperience, restoreGamemode, restoreHealth, restoreHunger, restoreItems, restoreHeroclass, restoreMagic, clearInventory, deEnchant]\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(configFile,new File(backupDir +"/"+configFile.getName()+"1.7.3"));
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to2Point0(Plugin plugin, FileConfiguration fc, Version version) {
		Version newVersion = new Version("2.0");
		Log.warn("BattleArena updating "+plugin.getName() +" configuration file='" + fc.getName() +"' to "+newVersion.getVersion());
		if (!openFiles())
			return;
		String line =null;
		try {
			if (version.compareTo(0)==0){
				fw.write("configVersion: "+newVersion.getVersion()+"\n");
			}
			boolean updatedDefaultSection = false;
			boolean lineRightAfterPreReqs = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: "+newVersion.getVersion()+"\n\n");
				} else if (line.matches(".*preReqs:.*")){
					lineRightAfterPreReqs = true;
					line = line.replace("enableForceStart", "matchEnableForceStart");
					fw.write(line+"\n");
				} else if (!updatedDefaultSection && (line.matches(".*forceStartTime:.*"))){
					line = line.replace("forceStartTime", "matchForceStartTime");
					fw.write(line+"\n");
				} else if (lineRightAfterPreReqs && line.matches(".*options:.*")) {
					lineRightAfterPreReqs = false;
					if (line.matches(".*options:.*\\[\\s*clearInventory\\s*\\].*")){
						fw.write("        options: []\n");
					} else if (line.matches(".*options:.*clearInventory\\s*,.*")){
						line = line.replaceAll("clearInventory\\s*,", "");
						fw.write(line+"\n");
					} else if (line.matches(".*options:.*,\\s*clearInventory.*")){
						line = line.replaceAll(",\\s*clearInventory", "");
						fw.write(line+"\n");
					}
					fw.write("    onEnter:\n");
					fw.write("        options: [storeAll]\n");
					fw.write("    onLeave:\n");
					fw.write("        options: [restoreAll]\n");
				} else {
					lineRightAfterPreReqs = false;
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(configFile,new File(backupDir +"/"+configFile.getName()+newVersion.getVersion()));
			renameTo(tempFile, configFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to2Point05(BaseConfig bacs, FileConfiguration fc, Version version) {
		Version newVersion = new Version("2.0.5");
		Log.warn("BattleArena updating config to "+newVersion.getVersion());
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				if (line.contains("configVersion")){
					fw.write("configVersion: "+newVersion.getVersion()+"\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*useArenasOnlyInOrder:.*"))){
					fw.write(line+"\n\n");
					fw.write("## Bukkit or Minecraft had a bug that sometimes caused players to be invisible after teleporting\n");
					fw.write("# If this is happening on your server set this to true.  \n");
					fw.write("# This option will be taken away once I have confirmed bukkit has fixed the problem\n");
					fw.write("enableInvisibleTeleportFix: false\n\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(configFile,new File(backupDir +"/"+configFile.getName()+newVersion.getVersion()));
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to2Point10(BaseConfig bacs, FileConfiguration fc, Version version, Version newVersion) {
		Log.warn("BattleArena updating config to "+newVersion.getVersion());
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				if (line.contains("configVersion")){
					fw.write("configVersion: "+newVersion.getVersion()+"\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*matchForceStartTime:.*"))){
					fw.write(line+"\n\n");
					fw.write("    ## Enable ready block (a block players can click to signify they are ready)\n");
					fw.write("    enablePlayerReadyBlock: false\n");
					fw.write("    readyBlockType: 42  ## what is the ready block (42 is iron_block)\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(configFile,new File(backupDir +"/"+configFile.getName()+newVersion.getVersion()));
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private boolean to2Point11(File file, Version oldVersion, Version newVersion) {
		try{
			FileUpdater fu = new FileUpdater(file, backupDir, newVersion, oldVersion);
			fu.replace("configVersion:.*", "configVersion: "+newVersion.getVersion());
			fu.addAfter(".*disabledCommands:.*", "",
					"# which heroes skills should be disabled when they enter an arena",
					"disabledHeroesSkills: []");
			return fu.update() != null;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}

	private static Version to2Point12(Version version, YamlFileUpdater yfu,
			File configFile, Version newVersion) throws IOException {
		FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
		fu.replace("configVersion:.*", "configVersion: "+newVersion);
		fu.addAfter(".*ignoreMaxStackSize:.*", "",
				"# if set to true, given enchanted items will not be limited to 'normal' safe enchantment levels",
				"# Example, most weapons are limited to sharpness 5.  if unsafeItemEnchants: true, this can be any level",
				"unsafeItemEnchants: false");
		return fu.update();
	}

	private static Version to2Point14(Version version, YamlFileUpdater yfu,
			File configFile, Version newVersion) throws IOException {
		FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
		fu.replace("configVersion:.*", "configVersion: "+newVersion);
		fu.delete(".*matchEnableForce.*");
		fu.addAfter(".*disabledCommands.*", "",
				"# What commands should be disabled when the player is inside of a queue, but not in a match",
				"disabledQueueCommands: []");
		return fu.update();
	}

	private static Version to2Point2(Version version, YamlFileUpdater yfu,
			File configFile, Version newVersion) throws IOException {
		Version tv = new Version("2.1.9");
		FileUpdater fu = new FileUpdater(configFile, yfu.backupDir, tv, version);
		fu.replace("configVersion:.*", "configVersion: "+tv);
		fu.delete(".*only works for uniz.*");
		fu.update();

		File dir = new File(configFile.getParentFile().getAbsolutePath());
		FileUpdater.makeIfNotExists(new File(dir +"/competitions"));
		FileUpdater.makeIfNotExists(new File(dir +"/saves"));
		FileUpdater.makeIfNotExists(new File(dir +"/saves/backups"));
		FileUpdater.makeIfNotExists(new File(dir +"/saves/inventories"));
		String moves[] = new String[]{"arenaplayers.yml","log.txt",
				"arenas.yml","scheduledEvents.yml","signs.yml","state.yml"};
		for (String s: moves){
			FileUpdater.moveIfExists(new File(dir+"/"+s), new File(dir+"/saves/"+s));
		}
		File f = new File(dir+"/backups");
		if (f.exists()){
			for (String s: f.list()){
				FileUpdater.moveIfExists(new File(dir+"/backups/"+s), new File(dir+"/saves/backups/"+s));
			}
		}
		FileUpdater.deleteIfExists(f);
		f = new File(dir+"/inventories");
		if (f.exists()){
			for (String s: f.list()){
				FileUpdater.moveIfExists(new File(dir+"/inventories/"+s), new File(dir+"/saves/inventories/"+s));
			}
		}
		FileUpdater.deleteIfExists(f);

		/// now that we have saved and updated the config... do the rest
		String[] sections = new String[]{"arena","skirmish","battleground","colliseum",
				"freeForAll","deathMatch","tourney"};
		for (String section: sections){
			updateSection(configFile, section);}

		/// move everything around
		sections = new String[]{"arena","skirmish","battleground","colosseum",
				"freeForAll","deathMatch","tourney"};
		for (String section: sections){
			String s = StringUtils.capitalize(section);
			FileUpdater.moveIfExists(new File(dir+"/"+s+"Config.yml"), new File(dir+"/competitions/"+s+"Config.yml"));
		}
		yfu.backupDir = new File(dir+"/saves/backups");

		sections = new String[]{"Arena","Skirmish","Battleground","Colosseum","FreeForAll","DeathMatch"};
		HashSet<String> queues = new HashSet<String>(
				Arrays.asList(new String[]{"Arena","Skirmish","Battleground","Colosseum"}));

		for (String s: sections){
			FileUpdater fur = new FileUpdater(new File(dir+"/competitions/"+s+"Config.yml"), yfu.backupDir, newVersion, version);
			if (queues.contains(s)){
				fur.addAfter(".*enabled:.*", "    joinType: Queue");
			} else {
				fur.addAfter(".*enabled:.*", "    joinType: JoinPhase");
			}
			fur.update();
		}

		FileUpdater fur = new FileUpdater(configFile, yfu.backupDir, newVersion, version);
		fur.replace("configVersion:.*", "configVersion: "+newVersion);
		fur.deleteAllFrom("### Arena");
		return fur.update();
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
			return false;
		}
		try {
			tempFile = new File(BattleArena.getSelf().getDataFolder()+"/temp.yml");
			fw = new BufferedWriter(new FileWriter(tempFile));
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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

	private static void updateSection(File file, String section) {
		Log.warn("BattleArena updating " + section +" to new form");
		String colliseum = "colliseum";
		String capcolliseum = "Colliseum";
		File dir = file.getParentFile().getAbsoluteFile();
		FileUpdater.deleteIfExists(new File(dir+"/"+section+"Messages.yml"));
		FileUpdater.deleteIfExists(new File(dir+"/"+StringUtils.capitalize(section)+"Messages.yml"));
		FileUpdater.deleteIfExists(new File(dir+"/"+section+"Config.yml"));
		FileUpdater.deleteIfExists(new File(dir+"/"+StringUtils.capitalize(section)+"Config.yml"));

		if (section.equals(colliseum))
			section = "colosseum";

		FileWriter fw = null;
		BufferedReader br = null;
		String capSection = StringUtils.capitalize(section);
		try {
			fw = new FileWriter(dir+"/"+capSection+"Config.yml");
			br = new BufferedReader(new FileReader(file));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		boolean inSection = false;
		String line =null;
		try {
			fw.write("configVersion: 2.0\n");
			while ((line = br.readLine()) != null){
				line = line.replaceAll(colliseum, "colosseum").replaceAll(capcolliseum, "Colosseum");
				//				System.out.println(inSection +"-"+section+"  :  " + line);
				if (line.matches(section+":.*")){
					inSection = true;
					fw.write(capSection +":\n");
				} else if (inSection && line.matches("^\\s*$")){
					break;
				} else if (inSection){
					if (!line.contains("type: ffa"))
						fw.write(line+"\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	public File getBackupDir() {
		return backupDir;
	}
}
