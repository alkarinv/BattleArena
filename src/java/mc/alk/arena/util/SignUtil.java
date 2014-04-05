package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.signs.ArenaCommandSign;
import mc.alk.arena.objects.signs.ArenaStatusSign;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.Collection;

public class SignUtil {

	public static ArenaCommandSign getArenaCommandSign(Sign sign, String[] lines) {
		if (lines.length < 2)
			return null;
		String param = MessageUtil.decolorChat(lines[0]).replaceAll("[\\[\\" + Defaults.SIGN_PREFIX + "\\]]", "").trim();
        MatchParams mp = ParamController.getMatchParams(param);
        if (mp == null){
			Collection<MatchParams> params = ParamController.getAllParams();
			for (MatchParams p: params){
				if (p.getName().equalsIgnoreCase(param) ||
                        p.getCommand().equalsIgnoreCase(param) ||
                        p.getSignDisplayName()!=null &&
                        MessageUtil.decolorChat(p.getSignDisplayName().replaceAll("[\\[\\"+Defaults.SIGN_PREFIX+"\\]]","").trim()).
                                equalsIgnoreCase(param)){
					mp = p;
					break;
				}
			}
			if (mp == null){
				return null;}
		}

		try {
            return ArenaCommandSign.create(sign.getLocation(),mp,lines);
        } catch (Exception e){
			return null;
		}
	}

	public static ArenaClass getArenaClassSign(String[] lines) {
        return ArenaClassController.getClass(
                MessageUtil.decolorChat(lines[0]).replaceAll("[\\[\\"+Defaults.SIGN_PREFIX+"\\]]", ""));
	}

	public static ArenaStatusSign getArenaStatusSign(String[] lines) {
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

	public static Sign getSign(World w, int x, int y, int z) {
		Block b = w.getBlockAt(x, y, z);
		Material t = b.getType();
		return t == Material.SIGN || t == Material.SIGN_POST || t==Material.WALL_SIGN ? (Sign)b.getState(): null;
	}

	public static Sign getSign(Location l) {
		if (l == null)
			return null;
		Material t = l.getBlock().getType();
		return t == Material.SIGN || t == Material.SIGN_POST || t==Material.WALL_SIGN ? (Sign)l.getBlock().getState(): null;
	}

}
