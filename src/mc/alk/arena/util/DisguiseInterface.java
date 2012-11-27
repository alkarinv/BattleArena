package mc.alk.arena.util;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.Disguise;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

public class DisguiseInterface {
	public static final int DEFAULT = Integer.MAX_VALUE;

	public static DisguiseCraftAPI disguiseInterface;

	public static void undisguise(Player player) {
		if (disguiseInterface.isDisguised(player)){
			disguiseInterface.undisguisePlayer(player);}
	}

	public static void disguisePlayer(Player player, String disguise) {
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
			e.printStackTrace();
		}
	}

	public static boolean enabled() {
		return disguiseInterface != null;
	}

}
