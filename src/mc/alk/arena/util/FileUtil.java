package mc.alk.arena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mc.alk.arena.BattleArena;

public class FileUtil {

	public static InputStream getInputStream(Class<?> clazz, File file) {
		InputStream inputStream = null;
		if (file.exists()){
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		String path = file.getPath();
		/// Load from pluginJar
		inputStream = clazz.getResourceAsStream(path);
		if (inputStream == null)
			inputStream = clazz.getClassLoader().getResourceAsStream(path);
		return inputStream;
	}

	public static InputStream getInputStream(Class<?> clazz, File defaultFile, File defaultPluginFile) {
		InputStream inputStream = null;
		if (defaultPluginFile.exists()){
			try {
				inputStream = new FileInputStream(defaultPluginFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (inputStream != null)
			return inputStream;
		/// Load from pluginJar
		inputStream = clazz.getResourceAsStream(defaultPluginFile.getPath());
		if (inputStream == null) /// will this work to fix the problems in windows??
			inputStream = clazz.getClassLoader().getResourceAsStream(defaultPluginFile.getPath());
		if (inputStream == null) /// Load from BattleArena.jar
			inputStream = BattleArena.getSelf().getClass().getClassLoader().getResourceAsStream(defaultFile.getPath());

		return inputStream;
	}

	public static File load(Class<?> clazz, String config_file, String default_file) {
		File file = new File(config_file);
		if (!file.exists()){ /// Create a new file from our default example
			try{
				InputStream inputStream = clazz.getResourceAsStream(default_file);
				if (inputStream == null) /// will this work to fix the problems in windows??
					inputStream = clazz.getClassLoader().getResourceAsStream(default_file);

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
