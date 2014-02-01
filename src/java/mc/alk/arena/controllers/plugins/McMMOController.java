package mc.alk.arena.controllers.plugins;

import mc.alk.arena.listeners.competition.plugins.McMMOListener;
import mc.alk.arena.util.Log;

import java.util.List;


public class McMMOController {
	static boolean enabled = false;

	public static boolean enabled() {
		return enabled;
	}

	public static void setEnable(boolean enable) {
        enabled = enable;
		McMMOListener.enable(enable);
	}

    public static void setDisabledSkills(List<String> disabled) {
        try{McMMOListener.setDisabledSkills(disabled);}catch(Exception e){Log.printStackTrace(e);}
    }
}
