package mc.alk.arena.executors;

import java.util.Collection;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.BAEventController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BattleArenaExecutor extends CustomCommandExecutor{

	@MCCommand(cmds={"listInv"}, admin=true)
	public boolean listSaves(CommandSender sender, OfflinePlayer p) {
		Collection<String> dates = InventorySerializer.getDates(p.getName());
		if (dates == null){
			return sendMessage(sender, "There are no inventory saves for this player");
		}
		int i=0;
		sendMessage(sender, "Most recent inventory saves");
		for (String date: dates){
			sendMessage(sender, ++i +" : " + date);
		}
		return true;
	}

	@MCCommand(cmds={"listInv"}, admin=true)
	public boolean listSave(CommandSender sender, OfflinePlayer p, Integer index) {
		if (index < 0 || index > Defaults.NUM_INV_SAVES){
			return sendMessage(sender,"&c index must be between 1-"+Defaults.NUM_INV_SAVES);}
		PInv pinv = InventorySerializer.getInventory(p.getName(), index-1);
		if (pinv == null)
			return sendMessage(sender, "&cThis index doesn't have an inventory!");
		sendMessage(sender, "&6" + p.getName() +" inventory at save " + index);
		boolean has = false;
		for (ItemStack is: pinv.armor){
			if (is == null || is.getType() == Material.AIR) continue;
			sendMessage(sender, "&a armor: &6" + InventoryUtil.getItemString(is));
			has = true;
		}
		for (ItemStack is: pinv.contents){
			if (is == null || is.getType() == Material.AIR) continue;
			sendMessage(sender, "&b inv: &6" + InventoryUtil.getItemString(is));
			has = true;
		}
		if (!has){
			sendMessage(sender, "&cThis index doesn't have any items");}
		return true;
	}

	@MCCommand(cmds={"restoreInv"}, admin=true)
	public boolean restoreInv(CommandSender sender, ArenaPlayer p, Integer index) {
		if (index < 0 || index > Defaults.NUM_INV_SAVES){
			return sendMessage(sender,"&c index must be between 1-"+Defaults.NUM_INV_SAVES);}
		if (InventorySerializer.restoreInventory(p,index-1)){
			return sendMessage(sender, "&2Player inventory restored");
		} else {
			return sendMessage(sender, "&cPlayer inventory could not be restored");
		}
	}

	@MCCommand(cmds={"giveAdminPerms"}, op=true)
	public boolean giveAdminPerms(CommandSender sender, Player player, Boolean enable) {
		if (!PermissionsUtil.giveAdminPerms(player,enable)){
			return sendMessage(sender,"&cCouldn't change the admin perms of &6"+player.getName());}
		if (enable){
			return sendMessage(sender,"&2 "+player.getName()+" &6now has&2 admin perms");
		} else {
			return sendMessage(sender,"&2 "+player.getName()+" &4no longer has&2 admin perms");
		}
	}

	@MCCommand(cmds={"version"}, admin=true)
	public boolean showVersion(CommandSender sender) {
		sendMessage(sender, BattleArena.getNameAndVersion());
		for (ArenaType at : ArenaType.getTypes()){
			String name = at.getPlugin().getName();
			String version = at.getPlugin().getDescription().getVersion();
			sendMessage(sender, at.getName() +"  " + name +"  " + version);
		}
		return true;
	}

	@MCCommand(cmds={"reload"}, admin=true, perm="arena.reload")
	public boolean arenaReload(CommandSender sender) {
		BAEventController baec = BattleArena.getBAEventController();
		if (ac.hasRunningMatches() || !ac.isQueueEmpty() || baec.hasOpenEvent()){
			sendMessage(sender, "&cYou can't reload the config while matches are running or people are waiting in the queue");
			return sendMessage(sender, "&cYou can use &6/arena cancel all&c to cancel all matches and clear queues");
		}

		ac.stop();
		/// Get rid of any current players
		PlayerController.clearArenaPlayers();

		BattleArena.getSelf().reloadConfig();
		BattleArena.getSelf().reloadCompetitions();

		ac.resume();
		return sendMessage(sender, "&6BattleArena&e configuration reloaded");
	}


}
