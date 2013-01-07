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

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.Log;
import mc.alk.plugin.updater.Version;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class YamlFileUpdater {
	BufferedReader br = null;
	BufferedWriter fw =null;
	File tempFile = null;
	File configFile = null;
	File backupDir;
	public YamlFileUpdater(Plugin plugin){
		backupDir = new File(plugin.getDataFolder() +"/backups");
		if (!backupDir.exists()){
			backupDir.mkdirs();}
	}

	public void updateMessageSerializer(MessageSerializer ms) {
		FileConfiguration fc = ms.getConfig();
		configFile = ms.getFile();
		Version version = new Version(fc.getString("version","0"));
		File dir = BattleArena.getSelf().getDataFolder();
		/// configVersion: 1.2, move over to new messages.yml
		/// this will delete their previous messages.yml
		if (version.compareTo(1.2)<0){
			File msgdir = new File(dir+"/messages");
			if (!msgdir.renameTo(new File(dir+"/backups/messages1.1"))){
				Log.warn("Couldn't rename the messages yml");
			}
			File messageFile = new File(dir+"/messages.yml");
			messageFile.renameTo(new File(dir+"/backups/messages.1.1.yml"));
			Log.warn("Updating to messages.yml version 1.2");
			Log.warn("If you had custom changes to messages you will have to redo them");
			Log.warn("But the old messages are saved as backups/messages.1.1.yml");
			Log.warn("You can override specific match/event messages inside the messages folder");
			move("/default_files/messages.yml",dir+"/messages.yml");
			ms.setConfig(new File(dir+"/messages.yml"));
		} else if (version.compareTo("1.5") < 0){
			messageTo1Point5(ms.getConfig(), version);
			ms.setConfig(new File(dir+"/messages.yml"));
		} else if (version.compareTo("1.5.1") < 0){
			messageTo1Point51(ms.getConfig(), version, new Version("1.5.1"));
			ms.setConfig(new File(dir+"/messages.yml"));
		} else if (version.compareTo("1.5.2") < 0){
			messageTo1Point52(ms.getConfig(), version, new Version("1.5.2"));
			ms.setConfig(new File(dir+"/messages.yml"));
		}
	}

	public static void updateAllConfig(Plugin plugin, ConfigSerializer cc) {
		Version version = new Version(cc.getString("configVersion","0"));
		YamlFileUpdater yfu = new YamlFileUpdater(plugin);
		yfu.configFile = cc.getFile();
		if (version.compareTo("2.0")<0){
			yfu.to2Point0(plugin, cc.getConfig(), version);}
	}

	public static void updateBaseConfig(Plugin plugin, BAConfigSerializer bacs) {
		File tempFile = null;
		FileConfiguration fc = bacs.getConfig();
		Version version = new Version(fc.getString("configVersion","0"));
		YamlFileUpdater yfu = new YamlFileUpdater(plugin);
		yfu.configFile = bacs.getFile();
		/// configVersion: 1.1, move over to classes.yml
		if (version.compareTo(1.1) <0){
			yfu.to1Point1(bacs, bacs.getConfig(), bacs.getFile(), tempFile, version);}
		if (version.compareTo(1.2)<0){
			yfu.to1Point2(bacs, bacs.getConfig(), version);}
		if (version.compareTo(1.3)<0){
			yfu.to1Point3(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.3.5")<0){
			yfu.to1Point35(bacs, bacs.getConfig(), version);}
		if (version.compareTo(1.4)<0){
			yfu.to1Point4(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.4.5")<0){
			yfu.to1Point45(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.5")<0){
			yfu.to1Point5(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.5.5")<0){
			yfu.to1Point55(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.6")<0){
			yfu.to1Point6(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.6.5")<0){
			yfu.to1Point65(bacs, bacs.getConfig(), version);}
		if (version.compareTo("1.7")<0){
			yfu.to1Point7(bacs, bacs.getConfig(), version);}
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
			yfu.to2Point11(bacs, bacs.getConfig(), version, new Version("2.1.1"));}

	}

	private void to1Point1(BAConfigSerializer bacs, FileConfiguration fc, File f, File tempFile, Version version) {
		Log.warn("BattleArena updating config to 1.1");
		Log.warn("Classes are now located in the classes.yml");
		Boolean configStillHasClasses = fc.contains("classes");
		File classesFile = new File(BattleArena.getSelf().getDataFolder()+"/classes.yml");
		BufferedWriter cfw =null;

		String line =null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedWriter fw =null;
		try {
			tempFile = new File(BattleArena.getSelf().getDataFolder()+"/temp.yml");
			fw = new BufferedWriter(new FileWriter(tempFile));
			cfw = new BufferedWriter(new FileWriter(classesFile));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			boolean inClassSection = false;
			if (version.compareTo(0)==0){
				fw.write("configVersion: 1.1\n");
			}
			while ((line = br.readLine()) != null){
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.1\n");
					if (!configStillHasClasses) /// we are finished if no classes exist
						break;
				} else if (line.matches("classes:") || line.matches(".*You can add new classes here.*")){
					inClassSection = true;
					cfw.write(line+"\n");
				} else if (inClassSection && line.matches("## default Match Options.*")){
					inClassSection = false;
					fw.write(line +"\n");
				} else if (inClassSection) {
					cfw.write(line+"\n");
				} else {
					fw.write(line+"\n");
				}
			}
			fw.close();
			if (br != null) br.close();
			cfw.close();
			renameTo(tempFile, f);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void to1Point2(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.2");
		Log.warn("You will have to remake any changes you made to defaultOptions");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			boolean inDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches("defaultMatchOptions:.*") || line.matches("## default Match Options.*")) + " "+line);
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.2\n");
				} else if (!updatedDefaultSection && (line.matches("defaultMatchOptions:.*") || line.matches("## default Match Options.*"))){
					updatedDefaultSection = true;
					inDefaultSection = true;
					fw.write("## default Options (these can be overridden by each match/event type)\n");
					fw.write("defaultOptions:\n");
					fw.write("    ### Match Options\n");
					fw.write("    secondsTillMatch: 3 ## Time between onPrestart and onStart\n");
					fw.write("    secondsToLoot: 5 ## Time after winning to run around and collect loot\n");
					fw.write("    matchTime: 120 ## How long do timed matches last, (in seconds)\n");
					fw.write("    matchUpdateInterval: 30 ## For timed matched, how long between sending players match updates\n");
					fw.write("\n");
					fw.write("    ### Event Options\n");
					fw.write("    eventCountdownTime: 180 ## How long before announcing an automated event and its start\n");
					fw.write("    eventCountdownInterval: 60 ## How often will it announce a reminder that its open and you can join\n");
					fw.write("\n");
					fw.write("    ### Match/Event Announcements\n");
					fw.write("    ## these only affect the broadcasts to the server or channel, not the messages the fighting players receive\n");
					fw.write("    ## announce : announce these messages \n");
					fw.write("    ## dontannounce : dont announce these messages\n");
					fw.write("    ## server : use herochat with the channel specified\n");
					fw.write("    ## herochat=<channel> : use herochat with the channel specified\n");
					fw.write("    announcements:\n");
					fw.write("        onPreStart: [ announce, server ]  ## match going to happen soon, example 'P1[p1Elo] vs P2[p2elo]'\n");
					fw.write("        onStart: [ dontannounce ]  ## match starting\n");
					fw.write("        onVictory:  [ announce, server ] ## match has been won, exmaple 'P1[p1elo] has defeated P2[p2elo]'\n");
					fw.write("\n");
					fw.write("    ### Default event Announcements\n");
					fw.write("    eventAnnouncements:\n");
					fw.write("        onOpen: [ announce, server ]  ## event is now open\n");
					fw.write("        onStart: [ announce, server ]  ## event is starting\n");
					fw.write("        onVictory:  [ announce, server ] ## event has been won\n");
					fw.write("\n");
				} else if (inDefaultSection && (line.matches("### Prerequisites.*") || line.matches("arena:.*"))){
					inDefaultSection = false;
					fw.write(line +"\n");
				} else if (inDefaultSection){
					/// dont print
				} else if ((line.matches(".*prefix.*FFA.*") || line.matches(".*prefix.*DeathMatch.*"))){
					fw.write(line +"\n");
					fw.write("    announcements: ### Override the match victory announcement as the event has one too\n");
					fw.write("        onVictory:  [ dontannounce ]\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void to1Point3(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.3");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.3\n");
				} else if (!updatedDefaultSection && (line.matches(".*Event Announcements.*"))){
					updatedDefaultSection = true;
					fw.write("    ### Duel Options\n");
					fw.write("    allowRatedDuels: false\n");
					fw.write("    # after a player rejects a duel, how long before they can be challenged again\n");
					fw.write("    challengeInterval: 1800 # (seconds) 1800 = 30minutes\n");
					fw.write("\n");
					fw.write(line+"\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void to1Point35(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.3.5");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.3.5\n");
				} else if (!updatedDefaultSection && (line.matches(".*challengeInterval.*"))){
					fw.write(line +"\n");
					fw.write("\n");
					fw.write("    ### Scheduled Event Options\n");
					fw.write("    ### Valid options [startContinuous, startNext]\n");
					fw.write("    onServerStart: []");
					fw.write("\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void to1Point4(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.4");

		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.4\n");
				} else if (!updatedDefaultSection && (line.matches(".*moneyName:.*"))){
					fw.write(line +"\n");
					fw.write("\n");
					fw.write("### Misc Options\n");
					fw.write("# some servers like to teleport people into the floor, you can adjust the Y offset of the teleport\n");
					fw.write("# to make them teleport higher by default, 1.0 = 1 block\n");
					fw.write("teleportYOffset: 1.0\n");
					fw.write("\n");
					fw.write("# which player commands should be disabled when they enter an arena\n");
					fw.write("disabledCommands: [home, spawn, payhome, warp, watch, sethome, ma]\n");
					fw.write("\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void to1Point45(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.4.5");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.4.5\n\n");
					fw.write("# Auto Update the BattleArena plugin (only works for unix/linux/mac)\n");
					fw.write("# Updates will be retrieved from the latest plugin on the bukkit site\n");
					fw.write("autoUpdate: true\n");
					fw.write("\n");
				} else if (!updatedDefaultSection && (line.matches(".*teleportYOffset.*"))){
					fw.write(line +"\n");
					fw.write("\n");
					fw.write("# When a player joins an arena and their inventory is stored\n");
					fw.write("# how many old inventories should be saved\n");
					fw.write("# put in 0 if you don't want this option\n");
					fw.write("numberSavedInventories: 5\n");
					fw.write("\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void to1Point5(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.5");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.5\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*disabledCommands:.*"))){
					fw.write(line +"\n");
					fw.write("\n");
					fw.write("# If set to true, items that are usually not stackable will be stacked when\n");
					fw.write("# a player is given items.  Examples: 64 mushroom soup, or 64 snow_ball, instead of 1 or 16\n");
					fw.write("ignoreMaxStackSize: false\n");
					fw.write("\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to1Point55(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.5.5");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.5.5\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*eventCountdownInterval:.*"))){
					fw.write(line +"\n");
					fw.write("    ## If true, when a player joins and an event that can be opened, it will be\n");
					fw.write("    ## silently opened and the player will join\n");
					fw.write("    allowPlayerCreation: true \n");
					fw.write("\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to1Point6(BAConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.6");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.6\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*ignoreMaxStackSize:.*"))){
					fw.write(line +"\n\n");
					fw.write("# If true if a player joins a match which has 2 arenas. 1v1 and 1v1v1v1. Then 1v1 will happen first\n");
					fw.write("# afterwards the 1v1v1v1 is guaranteed to be the next arena used.\n");
					fw.write("# if false.  if after the 1v1 is used, and the match ends, the 1v1 can be used again before the 1v1v1v1\n");
					fw.write("useArenasOnlyInOrder: false\n\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to1Point65(ConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.6.5");
		if (!openFiles())
			return;
		String line =null;
		try {
			if (version.compareTo(0)==0){
				fw.write("configVersion: 1.6.5\n");
			}

			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.6.5\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*matchUpdateInterval:.*"))){
					fw.write(line +"\n\n");
					fw.write("    ## when set to true when a player joins a queue the match will attempt to \n");
					fw.write("    ## start after the forceStartTime regardless if the minimum amount of people\n");
					fw.write("    ## have joined.  Example: say 2 teams of 4 people each is needed, if after\n");
					fw.write("    ## the forceStartTime is exceeded only 2 teams of 1 person is needed to start.\n");
					fw.write("    matchEnableForceStart: false\n");
					fw.write("    matchForceStartTime: 180\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to1Point7(ConfigSerializer bacs, FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating config to 1.7");
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				//				System.out.println((line.matches(".*Event Announcements.*") +"   " + line));
				if (line.contains("configVersion")){
					fw.write("configVersion: 1.7\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*enableForceStart:.*"))){
					line = line.replace("enableForceStart", "matchEnableForceStart");
					fw.write(line+"\n");
				} else if (!updatedDefaultSection && (line.matches(".*forceStartTime:.*"))){
					line = line.replace("forceStartTime", "matchForceStartTime");
					fw.write(line+"\n");
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(configFile,new File(backupDir +"/"+configFile.getName()+"1.7"));
			renameTo(tempFile, configFile);
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void to1Point73(ConfigSerializer bacs, FileConfiguration fc, Version version) {
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

	private void to2Point05(ConfigSerializer bacs, FileConfiguration fc, Version version) {
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

	private void to2Point10(ConfigSerializer bacs, FileConfiguration fc, Version version, Version newVersion) {
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

	private void to2Point11(ConfigSerializer bacs, FileConfiguration fc, Version version, Version newVersion) {
		Log.warn("BattleArena updating config to "+newVersion.getVersion());
		if (!openFiles())
			return;
		String line =null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				if (line.contains("configVersion")){
					fw.write("configVersion: "+newVersion.getVersion()+"\n\n");
				} else if (!updatedDefaultSection && (line.matches(".*disabledCommands:.*"))){
					fw.write(line+"\n\n");
					fw.write("# which heroes skills should be disabled when they enter an arena\n");
					fw.write("disabledHeroesSkills: []\n");
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

	private void messageTo1Point5(FileConfiguration fc, Version version) {
		Log.warn("BattleArena updating messages.yml to 1.5");
		if (!openFiles())
			return;
		String line =null;
		ConfigurationSection cs = fc.getConfigurationSection("system");
		boolean hasSystem = cs != null;
		try {
			boolean updatedDefaultSection = false;
			while ((line = br.readLine()) != null){
				if (line.contains("version")){
					fw.write("version: 1.5\n");
					if (!hasSystem){
						fw.write("system:\n");
						fw.write("    type_enabled: '&2 type &6%s&2 enabled'\n");
						fw.write("    type_disabled: '&2 type &6%s&2 disabled'\n");
						messageWrites1Point5();
					}
				} else if (!updatedDefaultSection && (line.matches(".*type_disabled:.*"))){
					fw.write(line+"\n");
					messageWrites1Point5();
				} else {
					fw.write(line+"\n");
				}
			}
			closeFiles();
			renameTo(configFile,new File(backupDir +"/"+configFile.getName()+"1.5"));
			renameTo(tempFile,configFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {br.close();} catch (Exception e) {}
			try {fw.close();} catch (Exception e) {}
		}
	}

	private void messageWrites1Point5() throws IOException {
		fw.write("    time_format: '&6%s&e %s '\n");
		fw.write("    zero_time: '&60'\n");
		fw.write("    match_disabled: '&cThe &6%s&c is currently disabled'\n");
		fw.write("    all_disabled: '&cThe arena system and all types are currently disabled'\n");
		fw.write("    currently_enabled: '&cEnabled &6%s'\n");
		fw.write("    no_join_perms: '&cYou dont have permission to join a &6%s'\n");
		fw.write("    teammate_cant_join: '&cOne of your teammates cant join the &6%s'\n");
		fw.write("    valid_arena_not_built: '&cA valid &6%s&c arena has not been built'\n");
		fw.write("    need_the_following: '&eYou need the following to join'\n");
		fw.write("    you_added_to_team: '&eYou have been added to a team'\n");
		fw.write("    queue_busy: '&cTeam queue was busy.  Try again in a sec.'\n");
		fw.write("    no_arena_for_size: '&cAn arena has not been built yet for that size of team'\n");
		fw.write("    joined_the_queue: '&eYou have joined the queue for the &6%s&e.'\n");
		fw.write("    server_joined_the_queue: '%s &6%s&e has joined the queue. &6%s/%s'\n");
		fw.write("    you_joined_the_queue: '&ePosition: &6%s&e. Match start when &6%s&e players join. &6%s/%s'\n");
		fw.write("    or_time: '&eor in %s when at least 2 players have joined'\n");
		fw.write("    you_start_when_free_pos: '&ePosition: &6%s&e. your match will start when an arena is free'\n");
		fw.write("    you_start_when_free: '&eYour match will start when an arena is free'\n");
		fw.write("    you_left_match: '&eYou have left the match'\n");
		fw.write("    you_left_event: '&eYou have left the %s event'\n");
		fw.write("    you_left: '&eYou have left'\n");
		fw.write("    you_not_in_queue: '&eYou are not currently in a queue'\n");
		fw.write("    you_left_queue: '&eYou have left the queue for the &6%s'\n");
		fw.write("    team_left_queue: '&6The team has left the &6%s&e queue. &6%s&e issued the command'\n");
		fw.write("    you_cant_join_event: '&cThe event can not be joined at this time'\n");
		fw.write("    no_event_open: '&cThere is no event currently open'\n");
		fw.write("    you_cant_join_event_while: '&eYou cant join the &6%s&e while its %s'\n");
		fw.write("    you_will_join_when_matched: '&eYou have already joined the and will enter when you get matched up with a team'\n");
		fw.write("    event_will_start_in: '&2The event will start in &6%s'\n");
		fw.write("    event_invalid_team_size: '&cThis Event can only support up to &6%s&e your team has &6%s'\n");
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


	private static boolean isWindows() {
		return System.getProperty("os.name").toUpperCase().contains("WINDOWS");
	}

	private static void renameTo(File file1, File file2) {
		/// That's right, I can't just rename the file, i need to move and delete
		if (isWindows()){
			File temp = new File(file2.getAbsoluteFile() +".backup");
			if (temp.exists()){
				temp.delete();
			}
			if (file2.exists()){
				file2.renameTo(temp);
				file2.delete();
			}
			if (!file1.renameTo(file2)){
				System.err.println(temp.getName() +" could not be renamed to " + file2.getName());
			} else {
				temp.delete();
			}
		} else {
			if (!file1.renameTo(file2)){
				System.err.println(file1.getName() +" could not be renamed to " + file2.getName());
			}
		}
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
}
