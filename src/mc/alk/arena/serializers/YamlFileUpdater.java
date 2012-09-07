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

import org.bukkit.configuration.file.FileConfiguration;

public class YamlFileUpdater {

	public static void updateConfig(BAConfigSerializer bacs){
		updateConfigFrom0to1point1(bacs);
	}

	private static void updateConfigFrom0to1point1(BAConfigSerializer bacs) {
		File tempFile = null;
		FileConfiguration fc = bacs.getConfig();
		File f = bacs.getFile();

		double version = fc.getDouble("configVersion",0);

		/// configVersion: 1.1, move over to classes.yml
		if (version < 1.1){
			to1Point1(bacs, fc, f, tempFile, version);
		}
		if (version < 1.2){
			fc = bacs.getConfig();
			f = bacs.getFile();
			to1Point2(bacs, fc, f, tempFile, version);			
		}
	}

	private static void to1Point1(BAConfigSerializer bacs, FileConfiguration fc, File f, File tempFile, double version) {
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
			if (version == 0){
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
			cfw.close();
			tempFile.renameTo(f.getAbsoluteFile());
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void to1Point2(BAConfigSerializer bacs, FileConfiguration fc, File f, File tempFile, double version) {
		Log.warn("BattleArena updating config to 1.2");
		Log.warn("You will have to remake any changes you made to defaultOptions");

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
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 

		try {
			boolean updatedDefaultSection = false;
			boolean inDefaultSection = false;
			if (version == 0){
				fw.write("configVersion: 1.2\n");}
			while ((line = br.readLine()) != null){
				System.out.println((line.matches("defaultMatchOptions:.*") || line.matches("## default Match Options.*")) + " "+line);
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
			fw.close();
			tempFile.renameTo(f.getAbsoluteFile());
			bacs.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void updateMessageSerializer(MessageSerializer ms) {
		FileConfiguration fc = ms.getConfig();

		double version = fc.getDouble("version",0);

		/// configVersion: 1.1, move over to new messages.yml
		/// this will delete their previous messages.yml
		if (version < 1.1){
			Log.warn("Updating to messages.yml version 1.1");
			Log.warn("If you had custom changes to messages you will have to redo them");
			Log.warn("You can now override specific match/event messages inside the messages folder");
			move("/default_files/messages.yml",BattleArena.getSelf().getDataFolder()+"/messages.yml");
			ms.setConfig(new File(BattleArena.getSelf().getDataFolder()+"/messages.yml"));
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
