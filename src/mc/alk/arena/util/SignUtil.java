package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaCommandSign;
import mc.alk.arena.objects.MatchParams;

public class SignUtil {
	public static enum ARENA_COMMAND{
		JOIN, LEAVE, START;
	}
	
	public static ArenaCommandSign getArenaCommandSign(String[] lines) {
		if (lines.length < 2)
			return null;
		String param = MessageUtil.decolorChat(lines[0]).replaceAll("\\"+Defaults.SIGN_PREFIX, "").trim();
		MatchParams mp = ParamController.getMatchParamCopy(param);
		if (mp == null)
			return null;
		param = MessageUtil.decolorChat(lines[1]).toUpperCase().trim();
		ARENA_COMMAND cmd = null;
		try {
			cmd = ARENA_COMMAND.valueOf(param);
		} catch (Exception e){
			return null;
		}
		ArenaCommandSign acs = new ArenaCommandSign(mp, cmd, lines[2], lines[3]);
		return acs;
	}

}
