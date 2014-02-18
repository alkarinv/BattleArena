package mc.alk.arena.util.plugins;

import mc.alk.arena.util.DisguiseUtil;
import mc.alk.arena.util.Log;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

public class DisguiseCraftUtil implements DisguiseUtil{
	public static DisguiseCraftAPI disguiseInterface;


	public static DisguiseUtil setPlugin(Plugin plugin){
		disguiseInterface = DisguiseCraft.getAPI();
        return new DisguiseCraftUtil();
    }

	public static boolean enabled(){
		return disguiseInterface != null;
	}

    @Override
	public void undisguise(Player player) {
		if (disguiseInterface.isDisguised(player)){
			disguiseInterface.undisguisePlayer(player);}
	}

    @Override
	public void disguisePlayer(Player player, String disguise) {
		try{
			DisguiseType type = DisguiseType.fromString(disguise);
			if (type == null){
				return ;}
			Disguise oldD = disguiseInterface.getDisguise(player);
			if (oldD.type == type){
				return;}
			Disguise d = new Disguise(disguiseInterface.newEntityID(), type);
			if (disguiseInterface.isDisguised(player)){
				disguiseInterface.changePlayerDisguise(player, d);
			} else {
				disguiseInterface.disguisePlayer(player, d);
			}
		}catch(Exception e){
			Log.printStackTrace(e);
		}
	}

}
