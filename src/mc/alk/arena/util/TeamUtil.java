package mc.alk.arena.util;

import java.util.HashMap;

import mc.alk.arena.objects.teams.Team;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.alk.controllers.MC;

public class TeamUtil {

	public static class TeamHead {
		final ItemStack is;
		final String name;
		public TeamHead(ItemStack is, String name){
			this.is = is; this.name = name;
		}
	}
	static public final HashMap<Integer, TeamHead> teamHeads = new HashMap<Integer, TeamHead>(); 
	static{
		teamHeads.put(0, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.RED.ordinal()), "&4Red"));
		teamHeads.put(1, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.BLUE.ordinal()), "&1Blue"));
		teamHeads.put(2, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.WHITE.ordinal()), "&fWhite"));
		teamHeads.put(3, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.BLACK.ordinal()), "&0Black"));
		teamHeads.put(4, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.ORANGE.ordinal()), "&6Gold"));
		teamHeads.put(5, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.GREEN.ordinal()), "&2Green"));
		teamHeads.put(6, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.PINK.ordinal()), "&dPink"));
		teamHeads.put(6, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.LIGHT_BLUE.ordinal()), "&bAqua"));
		teamHeads.put(7, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.GRAY.ordinal()), "&8Gray"));
		teamHeads.put(8, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.YELLOW.ordinal()), "&eYellow"));
		teamHeads.put(9, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.LIME.ordinal()), "&aLime"));
		teamHeads.put(10, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.SILVER.ordinal()), "&7Silver"));
		teamHeads.put(11, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.CYAN.ordinal()), "&3Cyan"));
		teamHeads.put(12, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.PURPLE.ordinal()), "&5Purple"));
		teamHeads.put(13, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.BROWN.ordinal()), "&8Brown"));
		teamHeads.put(14, new TeamHead(new ItemStack(Material.WOOL, 1,(short)DyeColor.MAGENTA.ordinal()), "&9Magenta"));
		teamHeads.put(15, new TeamHead(new ItemStack(Material.ICE, 1) , "&9Ice"));
		teamHeads.put(16, new TeamHead(new ItemStack(Material.IRON_BLOCK, 1), "&7Iron"));
		teamHeads.put(18, new TeamHead(new ItemStack(Material.DIAMOND_BLOCK, 1), "&bDiamond"));
		teamHeads.put(19, new TeamHead(new ItemStack(Material.BRICK, 1), "&cBrick"));
		teamHeads.put(20, new TeamHead(new ItemStack(Material.GLASS, 1), "&fGlass"));
		teamHeads.put(21, new TeamHead(new ItemStack(Material.SAND, 1), "&fSand"));
		teamHeads.put(22, new TeamHead(new ItemStack(Material.LEAVES, 1), "&2Leaf"));
		teamHeads.put(23, new TeamHead(new ItemStack(Material.NETHERRACK, 1), "&4Nether"));
		teamHeads.put(24, new TeamHead(new ItemStack(Material.OBSIDIAN, 1), "&0Obby"));
		teamHeads.put(25, new TeamHead(new ItemStack(Material.GRAVEL, 1), "&8Gravel"));
		teamHeads.put(26, new TeamHead(new ItemStack(Material.LAPIS_BLOCK, 1), "&1Lapis"));
		teamHeads.put(27, new TeamHead(new ItemStack(Material.MOSSY_COBBLESTONE, 1), "&2Mossy"));
		teamHeads.put(28, new TeamHead(new ItemStack(Material.PUMPKIN, 1), "&6Pumpkin"));
		teamHeads.put(29, new TeamHead(new ItemStack(Material.SNOW, 1), "&fSnow"));
		teamHeads.put(30, new TeamHead(new ItemStack(Material.COAL_ORE, 1), "&7Coal"));
		teamHeads.put(31, new TeamHead(new ItemStack(Material.COBBLESTONE, 1), "&7Cobblestone"));
		teamHeads.put(32, new TeamHead(new ItemStack(Material.WOOD, 1), "&8Wood"));
		teamHeads.put(33, new TeamHead(new ItemStack(Material.TNT, 1), "&cTNT"));
		teamHeads.put(34, new TeamHead(new ItemStack(Material.SPONGE, 1), "&eSponge"));
		for (Integer i: teamHeads.keySet()){
			TeamHead th = teamHeads.get(i);
			teamHeads.put(i, new TeamHead(th.is, MC.colorChat(th.name)) );
		}
	}


	public static void removeTeamHead(final int color, Player p) {
		ItemStack item = getTeamHead(color);
		final PlayerInventory inv = p.getInventory();
		if (inv.getHelmet().getType() == item.getType()){
			inv.setHelmet(new ItemStack(Material.AIR));
		}
	}
	
	public static void setTeamHead(final int color, Team team) {
		for (Player p: team.getPlayers()){
			setTeamHead(color,p);
		}
	}
	public static ItemStack getTeamHead(final int index){
		if (index >= 0 && index < teamHeads.size()){
			return teamHeads.get(index).is;}
		else{ /// really ??? thats a lot of teams, umm.. give them something
			return teamHeads.get(index % teamHeads.size()).is;}
	}
	@SuppressWarnings("deprecation")
	public static void setTeamHead(final int index, Player p) {
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
		if (index >= 0 && index < teamHeads.size()){
			return teamHeads.get(index).name;}
		else{ 
			return "Team" + index;}
	}

}
