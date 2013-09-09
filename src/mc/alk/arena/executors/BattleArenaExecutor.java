package mc.alk.arena.executors;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.BAEventController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
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


	@MCCommand(cmds={"version"}, admin=true)
	public boolean showVersion(CommandSender sender, String[] args) {
		sendMessage(sender, "&6"+BattleArena.getNameAndVersion());
		if (args.length > 1 && args[1].equalsIgnoreCase("all")){
			for (ArenaType at : ArenaType.getTypes()){
				String name = at.getPlugin().getName();
				String version = at.getPlugin().getDescription().getVersion();
				sendMessage(sender, at.getName() +"  " + name +"  " + version);
			}
		} else {
			sendMessage(sender, "&2For all game type versions, type &6/ba version all");
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

	@MCCommand(cmds={"listClasses"}, admin=true)
	public boolean listArenaClasses(CommandSender sender) {
		Set<ArenaClass> classes = ArenaClassController.getClasses();
		sendMessage(sender, "&2Registered classes");
		for (ArenaClass ac: classes){
			sendMessage(sender, "&6"+ac.getName()+"&2 : " + ac.getDisplayName());
		}
		return true;
	}

	@MCCommand(cmds={"kick"}, admin=true, perm="arena.kick")
	public boolean arenaKick(CommandSender sender, ArenaPlayer player) {
		ArenaPlayerLeaveEvent event = new ArenaPlayerLeaveEvent(player, player.getTeam(),
				ArenaPlayerLeaveEvent.QuitReason.KICKED);
		event.callEvent();
		if (event.getMessages() != null && !event.getMessages().isEmpty()) {
			MessageUtil.sendMessage(event.getPlayer(), event.getMessages());
		}
		return sendMessage(sender,"&2You have kicked &6"+player.getName());
	}

}
