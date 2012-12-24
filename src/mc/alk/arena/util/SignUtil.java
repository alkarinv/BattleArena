package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.signs.ArenaCommandSign;
import mc.alk.arena.objects.signs.ArenaCommandSign.ARENA_COMMAND;
import mc.alk.arena.objects.signs.ArenaStatusSign;

public class SignUtil {

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

	public static ArenaClass getArenaClassSign(String[] lines) {
		ArenaClass ac = ArenaClassController.getClass(
				MessageUtil.decolorChat(lines[0]).replaceAll("\\"+Defaults.SIGN_PREFIX, ""));
		return ac;
	}

	public static ArenaStatusSign ArenaStatusSign(String[] lines) {
		if (lines.length < 2)
			return null;
		String param = MessageUtil.decolorChat(lines[0]).replaceAll("\\"+Defaults.SIGN_PREFIX, "").trim();
		MatchParams mp = ParamController.getMatchParamCopy(param);
		if (mp == null)
			return null;
		if (lines[1].contains("status"))
			return new ArenaStatusSign(mp);

		return null;
	}

}
