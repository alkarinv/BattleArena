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

	@SuppressWarnings("resource")
	public static InputStream getInputStream(Class<?> clazz, File defaultFile, File defaultPluginFile) {
		InputStream inputStream = null;
		if (defaultPluginFile.exists()){
			try {
				inputStream = new FileInputStream(defaultPluginFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		/// Try to load a default file from the given plugin
		/// Load from ExtensionPlugin.Jar
		if (inputStream == null)
			inputStream = clazz.getResourceAsStream(defaultPluginFile.getPath());
		if (inputStream == null) /// will this work to fix the problems in windows??
			inputStream = clazz.getClassLoader().getResourceAsStream(defaultPluginFile.getPath());
		/// Load from the defaults
		/// Load from BattleArena.jar
		if (inputStream == null)
			inputStream = BattleArena.getSelf().getClass().getResourceAsStream(defaultFile.getPath());
		if (inputStream == null)
			inputStream = BattleArena.getSelf().getClass().getClassLoader().getResourceAsStream(defaultFile.getPath());

		return inputStream;
	}

	public static File load(Class<?> clazz, String config_file, String default_file) {
		File file = new File(config_file);
		if (!file.exists()){ /// Create a new file from our default example
			InputStream inputStream = null;
			OutputStream out = null;
			try{
				inputStream = clazz.getResourceAsStream(default_file);
				if (inputStream == null){ /// will this work to fix the problems in windows??
					inputStream = clazz.getClassLoader().getResourceAsStream(default_file);}

				out=new FileOutputStream(config_file);
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0){
					out.write(buf,0,len);}
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				if (out != null) try {out.close();} catch (Exception e){}
				if (inputStream != null) try {inputStream.close();} catch (Exception e){}
			}
		}
		return file;
	}
}
