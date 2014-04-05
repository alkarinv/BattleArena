package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import mc.alk.plugin.updater.Version;
import mc.alk.virtualPlayer.VirtualPlayers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.UUID;


public class ServerUtil {

    /**
     * The safe method to specify we want a player who is currently online
     * @param name
     * @return
     */
    public static Player findOnlinePlayer(String name) {
        return findPlayer(name);
    }

    public static Player findPlayer(UUID id) {
        if (id == null)
            return null;
        try {
            Player player = Bukkit.getPlayer(id);
            if (player != null)
                return player;
            if (Defaults.DEBUG_VIRTUAL) {
                return VirtualPlayers.getPlayer(id);
            }
        } catch (Throwable e){
            /* do nothing, craftbukkit version is not great enough */
        }
        return null;
    }

    @Deprecated
	public static Player findPlayerExact(String name) {
		if (name == null)
			return null;
		Player player = Bukkit.getPlayerExact(name);
		if (player != null)
			return player;
		if (Defaults.DEBUG_VIRTUAL){return VirtualPlayers.getPlayer(name);}
		return null;
	}

    @Deprecated
	public static Player findPlayer(String name) {
		if (name == null)
			return null;
		Player foundPlayer = Bukkit.getPlayer(name);
        if (foundPlayer != null) {
            return foundPlayer;}
        if (Defaults.DEBUG_VIRTUAL){foundPlayer = VirtualPlayers.getPlayer(name);}
        if (foundPlayer != null) {
            return foundPlayer;}
        Player[] online = Bukkit.getOnlinePlayers();
		if (Defaults.DEBUG_VIRTUAL){online = VirtualPlayers.getOnlinePlayers();}

		for (Player player : online) {
			String playerName = player.getName();

			if (playerName.equalsIgnoreCase(name)) {
				foundPlayer = player;
				break;
			}
			if (playerName.toLowerCase().indexOf(name.toLowerCase(),0) != -1) {
				if (foundPlayer != null) {
					return null;}

				foundPlayer = player;
			}
		}

		return foundPlayer;
	}

    @Deprecated
	public static OfflinePlayer findOfflinePlayer(String name) {
		OfflinePlayer p = findPlayer(name);
		if (p != null){
			return p;
		} else{
			/// Iterate over the worlds to see if a player.dat file exists
			for (World w : Bukkit.getWorlds()){
				File f = new File(w.getName()+"/players/"+name+".dat");
				if (f.exists()){
					return Bukkit.getOfflinePlayer(name);
				}
			}
			return null;
		}
	}

	public static Player[] getOnlinePlayers() {
		if (Defaults.DEBUG_VIRTUAL){
			return VirtualPlayers.getOnlinePlayers();
		} else {
			return Bukkit.getOnlinePlayers();
		}
	}

	public static void findOnlinePlayers(Set<String> names, Set<Player> foundplayers, Set<String> unfoundplayers) {
		Player[] online = getOnlinePlayers();
		for (String name : names){
			Player lastPlayer = null;
			for (Player player : online) {
				String playerName = player.getName();
				if (playerName.equalsIgnoreCase(name)) {
					lastPlayer = player;
					break;
				}

				if (playerName.toLowerCase().indexOf(name.toLowerCase(),0) != -1) { /// many names match the one given
					if (lastPlayer != null) {
						lastPlayer = null;
						break;
					}
					lastPlayer = player;
				}
			}
			if (lastPlayer != null){
				foundplayers.add(lastPlayer);
			} else{
				unfoundplayers.add(name);
			}
		}
	}

	public static Version getBukkitVersion(){
		final String pkg = Bukkit.getServer().getClass().getPackage().getName();
		String version = pkg.substring(pkg.lastIndexOf('.') + 1);
		if (version.equalsIgnoreCase("craftbukkit")){
			return new Version("v1_4_5-");
		} else{
			return new Version(version);
		}
	}


}
