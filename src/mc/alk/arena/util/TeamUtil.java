package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TeamUtil {
	static final int NTEAMS = 35;
	static final List<TeamHead> teamHeads = new ArrayList<TeamHead>();
	static final HashMap<String,Integer> map = new HashMap<String,Integer>();

	public static class TeamHead {
		final ItemStack is;
		final String name;
		public TeamHead(ItemStack is, String name){
			this.is = is; this.name = name;
		}
		public String getName(){
			return name;
		}
		public ItemStack getItem(){
			return is;
		}
	}

	public static void removeTeamHead(final int color, Player p) {
		ItemStack item = getTeamHead(color);
		final PlayerInventory inv = p.getInventory();
		if (inv != null && inv.getHelmet() != null && inv.getHelmet().getType() == item.getType()){
			inv.setHelmet(new ItemStack(Material.AIR));
		}
	}

	public static void setTeamHead(final int color, Team team) {
		for (ArenaPlayer p: team.getPlayers()){
			setTeamHead(color,p);
		}
	}

	public static ItemStack getTeamHead(int index){
		return index < teamHeads.size() ? teamHeads.get(index).getItem() : new ItemStack(Material.DIRT);
	}

	@SuppressWarnings("deprecation")
	public static void setTeamHead(final int index, ArenaPlayer player) {
		Player p = player.getPlayer();
		if (p.isOnline() && !p.isDead()){
			ItemStack is = p.getInventory().getHelmet();
			ItemStack item = getTeamHead(index);
			p.getInventory().setHelmet(item);
			if (is != null && is.getType() != Material.AIR && is.getType()!= Material.WOOL){
				InventoryUtil.addItemToInventory(p, is.clone(), is.getAmount(), false);}

			try{
				p.updateInventory();
			}catch (Exception e){}
		}
	}

	public static String createTeamName(int index) {
		return index < teamHeads.size() ? teamHeads.get(index).getName() : "Team" + index;
	}

	public static Integer getTeamIndex(String op) {
		return map.get(op.toUpperCase());
	}

	public static void addTeamHead(String name, TeamHead th) {
		teamHeads.add(th);
		map.put(name.toUpperCase(), teamHeads.size()-1);
	}

}
