package mc.alk.arena.serializers;

import mc.alk.arena.controllers.ModuleController;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.util.Log;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ModuleLoader {
	public void loadModules(File moduleDirectory) {
		if (!moduleDirectory.exists()){
			return;
		}
		List<String> loadedModules = new ArrayList<String>();
		for(File f:moduleDirectory.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				int period = name.lastIndexOf('.');
				final String sname = name.substring(period + 1);
				return period != -1 && sname.equals("class") || sname.equals("jar");
			}}))
		{
			ArenaModule am = null;
			try {
				am = loadModule(f);
				loadedModules.add(am.getName() +"_"+am.getVersion());
				am.setEnabled(true);
				am.onEnable();
				ModuleController.addModule(am);
			} catch (Exception e){
				Log.err("[BA Error] Error loading the module " + f.getName());
				if (am != null){
					am.setEnabled(false);
				}
				Log.printStackTrace(e);
			}
		}
		if (loadedModules.isEmpty()){
			Log.info("[BattleArena] no additional Arena modules");
		} else {
			Log.info("[BattleArena] loaded modules ["+StringUtils.join(loadedModules,", ")+"]");
		}

	}

	private ArenaModule loadModule(File f) throws Exception{
		ClassLoader loader = this.getClass().getClassLoader();
		URL url = f.toURI().toURL();

		URL[] urls = {url};
		URLClassLoader ucl = new URLClassLoader(urls,loader);

		//Load the class
		String shortName = f.getName().substring(0,f.getName().indexOf('.'));
		Class<?> clazz = ucl.loadClass(shortName);

		Class<?>[] args = {};
		Class<?extends ArenaModule> moduleClass = clazz.asSubclass(ArenaModule.class);
		Constructor<?> constructor = moduleClass.getConstructor(args);
        return (ArenaModule) constructor.newInstance((Object[])args);
	}
}
