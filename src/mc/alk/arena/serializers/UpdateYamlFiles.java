package mc.alk.arena.serializers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import mc.alk.arena.BattleArena;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Log;


public class UpdateYamlFiles {

	public static void updateConfig(BAConfigSerializer bacs){
		updateConfigFrom0to1point1(bacs);
	}

	private static void updateConfigFrom0to1point1(BAConfigSerializer bacs) {
		FileConfiguration fc = bacs.getConfig();
		File f = bacs.getFile();
		File tempFile = null;
		
		double version = fc.getDouble("configVersion",0);

		/// configVersion: 1.1, move over to classes.yml
		if (version < 1.1){
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
//						System.out.println("!!!!!! : " + line);
						if (!configStillHasClasses) /// we are finished if no classes exist
							break;
					} else if (line.matches("classes:") || line.matches(".*You can add new classes here.*")){
						inClassSection = true;
//						System.out.println("cfw : " + line);
						cfw.write(line+"\n");
					} else if (inClassSection && line.matches("## default Match Options.*")){
						inClassSection = false;
//						System.out.println("cfw : " + line);
						fw.write(line +"\n");
					} else if (inClassSection) {
						cfw.write(line+"\n");
//						System.out.println("cfw : " + line);
					} else {
						fw.write(line+"\n");
//						System.out.println("!!!!!! : " + line);
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
	}
}
