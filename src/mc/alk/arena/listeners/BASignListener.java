package mc.alk.arena.listeners;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.SignController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.signs.ArenaCommandSign;
import mc.alk.arena.objects.signs.ArenaStatusSign;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.SignUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BASignListener implements Listener{
	SignController signController;
	public BASignListener(SignController signController){
		this.signController = signController;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
//		if (event.isCancelled()) return;
		final Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return; /// This has happenned, minecraft is a strange beast
		final Material clickedMat = clickedBlock.getType();

		/// If this is an uninteresting block get out of here as quickly as we can
		if (!(clickedMat.equals(Material.SIGN) || clickedMat.equals(Material.SIGN_POST)
				|| 	clickedMat.equals(Material.WALL_SIGN))) {
			return;
		}
		Sign sign = null;
		try{
			sign = (Sign) clickedBlock.getState(); /// so yes, this has also sometimes not been a sign, despite checking above
		} catch (NullPointerException e){
			return;
		}
		String[] lines = sign.getLines();
		if (!lines[0].matches("^.[0-9a-fA-F]\\*.*$")){
			return;
		}

		ArenaCommandSign acs = SignUtil.getArenaCommandSign(lines);
		if (acs == null){
			return;}
		ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
		acs.performAction(ap);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event){
		if (Defaults.DEBUG_TRACE) System.out.println("onSignChange Event");
		final Block block = event.getBlock();
		final Material type = block.getType();

		if (!(type.equals(Material.SIGN) || type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN))) {
			return;}

		Player p = event.getPlayer();

		/// Is the sign a arena class sign?
		final boolean admin = PermissionsUtil.isAdmin(p);
		String lines[] = event.getLines();
		ArenaClass ac = SignUtil.getArenaClassSign(lines);
		if (ac != null){
			if (!admin){
				cancelSignPlace(event,block);
				return;
			}
			makeArenaClassSign(event, ac, lines);
			return;
		}
		/// is the sign a command sign
		ArenaCommandSign acs = SignUtil.getArenaCommandSign(lines);
		if (acs != null){
			if (!admin){
				cancelSignPlace(event,block);
				return;
			}
			makeArenaCommandSign(event, acs, lines);
			return;
		}
		/// is the sign a command sign
		ArenaStatusSign ass = SignUtil.getArenaStatusSign(lines);
		if (ass != null){
			if (!admin){
				cancelSignPlace(event,block);
				return;
			}
			makeArenaStatusSign(event, ass, lines);
			return;
		}
	}

	private void makeArenaClassSign(SignChangeEvent event, ArenaClass ac, String[] lines) {
		if (ac == null)
			return;
		final Block block = event.getBlock();
		for (int i=1;i<lines.length;i++){
			if (!lines[i].isEmpty()) /// other text, not our sign
				return;
		}

		try{
			event.setLine(0, MessageUtil.colorChat(ChatColor.GOLD+Defaults.SIGN_PREFIX+ac.getDisplayName()));
			MessageUtil.sendMessage(event.getPlayer(), "&2Arena class sign created");
		} catch (Exception e){
			MessageUtil.sendMessage(event.getPlayer(), "&cError creating Arena Class Sign");
			e.printStackTrace();
			cancelSignPlace(event,block);
			return;
		}
	}

	private void makeArenaCommandSign(SignChangeEvent event, ArenaCommandSign acs, String[] lines) {
		if (acs == null)
			return;
		final Block block = event.getBlock();
		for (int i=3;i<lines.length;i++){
			if (!lines[i].isEmpty()) /// other text, not our sign
				return;
		}

		try{
			String match = acs.getMatchParams().getName().toLowerCase();
			match = Character.toUpperCase(match.charAt(0)) + match.substring(1);
			event.setLine(0, MessageUtil.colorChat( ChatColor.GOLD+Defaults.SIGN_PREFIX+
					acs.getMatchParams().getColor()+match));
			String cmd = acs.getCommand().toString();
			cmd = Character.toUpperCase(cmd.charAt(0)) + cmd.substring(1);
			event.setLine(1, MessageUtil.colorChat(ChatColor.GREEN+cmd.toLowerCase()) );
			MessageUtil.sendMessage(event.getPlayer(), "&2Arena command sign created");
		} catch (Exception e){
			MessageUtil.sendMessage(event.getPlayer(), "&cError creating Arena Command Sign");
			e.printStackTrace();
			cancelSignPlace(event,block);
			return;
		}
	}

	private void makeArenaStatusSign(SignChangeEvent event, ArenaStatusSign acs, String[] lines) {
		if (acs == null)
			return;
		final Block block = event.getBlock();
		for (int i=3;i<lines.length;i++){
			if (!lines[i].isEmpty()) /// other text, not our sign
				return;
		}

		try{
			String match = acs.getType().toLowerCase();
			match = Character.toUpperCase(match.charAt(0)) + match.substring(1);
			event.setLine(0, MessageUtil.colorChat( ChatColor.GOLD+Defaults.SIGN_PREFIX+
					acs.getMatchParams().getColor()+match));
			event.setLine(1, MessageUtil.colorChat( ""));
			acs.setLocation(event.getBlock().getLocation());

			signController.addStatusSign(acs);
			MessageUtil.sendMessage(event.getPlayer(), "&2Arena status sign created");
		} catch (Exception e){
			MessageUtil.sendMessage(event.getPlayer(), "&cError creating Arena Status Sign");
			e.printStackTrace();
			cancelSignPlace(event,block);
			return;
		}
	}

	public static void cancelSignPlace(SignChangeEvent event, Block block){
		event.setCancelled(true);
		block.setType(Material.AIR);
		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
	}
}
