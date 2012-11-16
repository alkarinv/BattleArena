package mc.alk.arena.util;

import org.bukkit.entity.Player;

import pgDev.bukkit.DisguiseCraft.Disguise;
import pgDev.bukkit.DisguiseCraft.Disguise.MobType;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

public class DisguiseInterface {

	public static DisguiseCraftAPI disguiseInterface;

	public static void undisguise(Player player) {
		if (disguiseInterface.isDisguised(player)){
			disguiseInterface.undisguisePlayer(player);}
	}

	public static void disguisePlayer(Player player, String disguiseAllAs) {
		MobType type = MobType.fromString(disguiseAllAs);
		if (type == null){
			return ;}
		Disguise oldD = disguiseInterface.getDisguise(player);
		if (oldD.mob == type){
			return;}
		Disguise d = new Disguise(disguiseInterface.newEntityID(), type);
		if (disguiseInterface.isDisguised(player)){
			disguiseInterface.changePlayerDisguise(player, d);
		} else {
			disguiseInterface.disguisePlayer(player, d);
		}
	}

	public static boolean enabled() {
		return disguiseInterface != null;
	}

}
