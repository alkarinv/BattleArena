package mc.alk.arena.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mc.alk.arena.BattleArena;

import org.bukkit.plugin.Plugin;

public class FileUtil {

	public static InputStream getInputStream(Plugin plugin, File defaultFile, File defaultPluginFile) {
		InputStream inputStream = null;
		/// Load from pluginJar
		inputStream = plugin.getClass().getClassLoader().getResourceAsStream(defaultPluginFile.getPath());
		if (inputStream == null){ /// Load from BattleArena.jar
			inputStream = BattleArena.getSelf().getClass().getClassLoader().getResourceAsStream(defaultFile.getPath());
		}
		return inputStream;
	}

	public static File load(Plugin plugin, String config_file, String default_file) {
		File file = new File(config_file);
		if (!file.exists()){ /// Create a new file from our default example
			try{
				InputStream inputStream = plugin.getClass().getResourceAsStream(default_file);
				OutputStream out=new FileOutputStream(config_file);
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0){
					out.write(buf,0,len);}
				out.close();
				inputStream.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return file;
	}
}
