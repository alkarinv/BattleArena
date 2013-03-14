package test.mc.alk.arena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;
import mc.alk.arena.serializers.YamlFileUpdater;
import mc.alk.arena.util.Log;
import mc.alk.plugin.updater.FileUpdater;
import mc.alk.plugin.updater.Version;

import org.apache.commons.lang.StringUtils;

public class TestConfigUpdate extends TestCase{
	public void testUpdates(){
		YamlFileUpdater yfu = new YamlFileUpdater(new File("test_files/backups"));
		File configFile = new File("test_files/config.yml");
		Version version = new Version("0");
		try {
			Version newVersion = new Version("2.2");
			if (version.compareTo(newVersion) < 0){
				version = to2Point2(version, yfu, configFile, newVersion);
				version = to2Point2(version, yfu, configFile, newVersion);
				version = to2Point2(version, yfu, configFile, newVersion);
				version = to2Point2(version, yfu, configFile, newVersion);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Version to2Point2(Version version, YamlFileUpdater yfu,
			File configFile, Version newVersion) throws IOException {
		FileUpdater fu = new FileUpdater(configFile, yfu.getBackupDir(), newVersion, version);
		//		fu.replace("configVersion:.*", "configVersion: "+newVersion);

		return fu.update();
	}

	public void testUpdateTo2point2(){
		//		BaseConfig bc = new BaseConfig( new File("test_files/testconfig.yml"));
		//		updateSection(bc.getFile(), "arena");
		//		updateSection(bc.getFile(), "skirmish");
		//		updateSection(bc.getFile(), "battleground");
		//		updateSection(bc.getFile(), "colliseum");
		//		updateSection(bc.getFile(), "freeForAll");
		//		updateSection(bc.getFile(), "deathMatch");
		//		updateSection(bc.getFile(), "tourney");
		//		deleteAllAfter(bc.getFile(), "### Arena");
	}

	private void updateSection(File file, String section) {
		Log.warn("BattleArena updating " + section +" to new form");
		String colliseum = "colliseum";
		String capcolliseum = "Colliseum";
		File oldMessageFile = new File(section+"Messages.yml");
		if (oldMessageFile.exists()){
			oldMessageFile.delete();}

		if (section.equals(colliseum))
			section = "colosseum";

		FileWriter fw = null;
		BufferedReader br = null;
		String capSection = StringUtils.capitalize(section);
		try {
			fw = new FileWriter("test_files/"+capSection+"Config.yml");
			br = new BufferedReader(new FileReader(file));
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		boolean inSection = false;
		String line =null;
		try {
			while ((line = br.readLine()) != null){
				line = line.replaceAll(colliseum, "colosseum").replaceAll(capcolliseum, "Colosseum");
				//				System.out.println(inSection +"-"+section+"  :  " + line);
				if (line.matches(section+":.*")){
					inSection = true;
					fw.write(capSection +":\n");
				} else if (inSection && line.matches("^\\s*$")){
					break;
				} else if (inSection){
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
}
