package mc.alk.arena.util.plugins;

import mc.alk.arena.util.DisguiseUtil;
import mc.alk.arena.util.Log;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LibsDisguiseUtil implements DisguiseUtil{
    static boolean enabled = false;

    public static DisguiseUtil setPlugin(Plugin plugin){
		enabled = plugin != null && plugin instanceof LibsDisguises;
        return enabled ? new LibsDisguiseUtil() : null;
    }

    public static boolean enabled(){
        return enabled;
    }

    @Override
    public void undisguise(Player player) {
        DisguiseAPI.undisguiseToAll(player);
    }

    @Override
    public void disguisePlayer(Player player, String disguise) {
        try {
            Disguise dis;
            DisguiseType d;
            try {
                d = DisguiseType.valueOf(disguise.toUpperCase());
            } catch (Exception e) {
                d = null;
            }
            if (d != null) {
                dis = new MobDisguise(d,false);
            } else {
                dis = new PlayerDisguise(disguise);
            }

            DisguiseAPI.disguiseToAll(player,dis);
        }catch(Exception e){
            Log.printStackTrace(e);
        }
    }

}
