package mc.alk.arena.util.compat;

import org.bukkit.entity.Player;

public interface IPlayerHelper {

	void setHealth(Player player, double health, boolean skipHeroes);

	double getHealth(Player player);

	double getMaxHealth(Player player);

    Object getScoreboard(Player player);

    void setScoreboard(Player player, Object scoreboard);
}
