package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.TeamAppearance;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeamUtil {
	static final int NTEAMS = 35;
	static final List<TeamAppearance> teamHeads = new ArrayList<TeamAppearance>();
	static final HashMap<String,Integer> map = new HashMap<String,Integer>();

	public static void removeTeamHead(final int color, Player p) {
		ItemStack item = getTeamHead(color);
		final PlayerInventory inv = p.getInventory();
		if (inv != null && inv.getHelmet() != null && inv.getHelmet().getType() == item.getType()){
			inv.setHelmet(new ItemStack(Material.AIR));
		}
	}

	public static String getTeamName(int index) {
		return index < teamHeads.size() ? teamHeads.get(index).getName() : "Team" + index;
	}

	public static void setTeamHead(final int color, ArenaTeam team) {
		for (ArenaPlayer p: team.getPlayers()){
			setTeamHead(color,p);
		}
	}

	public static ItemStack getTeamHead(int index){
		return index < teamHeads.size() ? teamHeads.get(index).getItem() : new ItemStack(Material.DIRT);
	}

	public static ChatColor getTeamChatColor(int index){
		return index < teamHeads.size() ? teamHeads.get(index).getChatColor() : ChatColor.WHITE;
	}

	public static Color getTeamColor(Integer index){
		return index != null && index < teamHeads.size() ? teamHeads.get(index).getColor() : Color.WHITE;
	}

	public static void setTeamHead(final int index, ArenaPlayer player) {
		setTeamHead(getTeamHead(index),player);
	}

	public static void setTeamHead(final ItemStack item, ArenaPlayer player) {
		setTeamHead(item,player.getPlayer());
	}

	@SuppressWarnings("deprecation")
	public static void setTeamHead(ItemStack item, Player p) {
		if (p.isOnline() && !p.isDead()){
			ItemStack is = p.getInventory().getHelmet();
            try{
			p.getInventory().setHelmet(item);
			if (is != null && is.getType() != Material.AIR && is.getType()!= Material.WOOL){
				InventoryUtil.addItemToInventory(p, is.clone(), is.getAmount(),true, true);}
				p.updateInventory();
			}catch (Exception e){
                if (!Defaults.DEBUG_VIRTUAL)
                    Log.printStackTrace(e);
            }
		}
	}

	public static Integer getTeamIndex(String op) {
		if (map.containsKey(op.toUpperCase())){
			return map.get(op.toUpperCase());}
		try{
			return Integer.valueOf(op);
		} catch(Exception e){
			return null;
		}
	}

	public static void addTeamHead(String name, TeamAppearance th) {
		teamHeads.add(th);
		map.put(name.toUpperCase(), teamHeads.size()-1);
	}

	public static String formatName(ArenaTeam t){
		StringBuilder sb = new StringBuilder("&e " + t.getDisplayName());

		for (ArenaPlayer p: t.getPlayers()){
			Integer k = t.getNKills(p);
			Integer d = t.getNDeaths(p);
			if (k==null) k=0;
			if (d==null) d=0;
			sb.append("&e(&c").append(k).append("&e,&7").append(d).append("&e)");
		}
		return sb.toString();
	}

}
