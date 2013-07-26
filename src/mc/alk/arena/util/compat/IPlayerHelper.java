package mc.alk.arena.util.compat;

import org.bukkit.entity.Player;

public interface IPlayerHelper {

	void setHealth(Player player, Double health, boolean skipHeroes);

	double getHealth(Player player);

}
