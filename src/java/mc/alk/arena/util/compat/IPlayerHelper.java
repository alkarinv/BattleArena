package mc.alk.arena.util.compat;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IPlayerHelper {

	void setHealth(Player player, double health, boolean skipHeroes);

	double getHealth(Player player);

	double getMaxHealth(Player player);

    Object getScoreboard(Player player);

    void setScoreboard(Player player, Object scoreboard);

    UUID getID(OfflinePlayer player);
}
