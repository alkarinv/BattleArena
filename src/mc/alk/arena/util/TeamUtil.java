package mc.alk.arena.util;

import java.util.HashMap;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TeamUtil {
	static final int NTEAMS = 35;
	static final HashMap<String,Integer> map = new HashMap<String,Integer>();
	static {
		map.put("RED",0);
		map.put("BLUE",1);
		map.put("WHITE",2);
		map.put("BLACK",3);
		map.put("GOLD",4);
		map.put("GREEN",5);
		map.put("PINK",6);
		map.put("AQUA",7);
		map.put("GRAY",8);
		map.put("YELLOW",9);
		map.put("LIME",10);
		map.put("SILVER",11);
		map.put("CYAN",12);
		map.put("PURPLE",13);
		map.put("BROWN",14);
		map.put("MAGENTA",15);
		map.put("ICE",16);
		map.put("IRON",17);
		map.put("DIAMOND",18);
		map.put("BRICK",19);
		map.put("GLASS",20);
		map.put("SAND",21);
		map.put("LEAF",22);
		map.put("NETHER",23);
		map.put("OBBY",24);
		map.put("GRAVEL",25);
		map.put("LAPIS",26);
		map.put("MOSSY",27);
		map.put("PUMPKIN",28);
		map.put("SNOW",29);
		map.put("COAL",30);
		map.put("COBBLESTONE",31);
		map.put("WOOD",32);
		map.put("TNT",33);
		map.put("SPONGE",34);
	}
	public static class TeamHead {
		final ItemStack is;
		final String name;
		public TeamHead(ItemStack is, String name){
			this.is = is; this.name = name;
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
		index = index % NTEAMS;
		switch(index){
		case 0: return new ItemStack(Material.WOOL, 1, (short)DyeColor.RED.ordinal());
		case 1: return new ItemStack(Material.WOOL, 1, (short)DyeColor.BLUE.ordinal());
		case 2: return new ItemStack(Material.WOOL, 1, (short)DyeColor.WHITE.ordinal());
		case 3: return new ItemStack(Material.WOOL, 1, (short)DyeColor.BLACK.ordinal());
		case 4: return new ItemStack(Material.WOOL, 1, (short)DyeColor.ORANGE.ordinal());
		case 5: return new ItemStack(Material.WOOL, 1, (short)DyeColor.GREEN.ordinal());
		case 6: return new ItemStack(Material.WOOL, 1, (short)DyeColor.PINK.ordinal());
		case 7: return new ItemStack(Material.WOOL, 1, (short)DyeColor.LIGHT_BLUE.ordinal());
		case 8: return new ItemStack(Material.WOOL, 1, (short)DyeColor.GRAY.ordinal());
		case 9: return new ItemStack(Material.WOOL, 1, (short)DyeColor.YELLOW.ordinal());
		case 10: return new ItemStack(Material.WOOL, 1, (short)DyeColor.LIME.ordinal());
		case 11: return new ItemStack(Material.WOOL, 1, (short)DyeColor.SILVER.ordinal());
		case 12: return new ItemStack(Material.WOOL, 1, (short)DyeColor.CYAN.ordinal());
		case 13: return new ItemStack(Material.WOOL, 1, (short)DyeColor.PURPLE.ordinal());
		case 14: return new ItemStack(Material.WOOL, 1, (short)DyeColor.BROWN.ordinal());
		case 15: return new ItemStack(Material.WOOL, 1, (short)DyeColor.MAGENTA.ordinal());
		case 16: return new ItemStack(Material.ICE, 1);
		case 17: return new ItemStack(Material.IRON_BLOCK, 1);
		case 18: return new ItemStack(Material.DIAMOND_BLOCK, 1);
		case 19: return new ItemStack(Material.BRICK, 1);
		case 20: return new ItemStack(Material.GLASS, 1);
		case 21: return new ItemStack(Material.SAND, 1);
		case 22: return new ItemStack(Material.LEAVES, 1);
		case 23: return new ItemStack(Material.NETHERRACK, 1);
		case 24: return new ItemStack(Material.OBSIDIAN, 1);
		case 25: return new ItemStack(Material.GRAVEL, 1);
		case 26: return new ItemStack(Material.LAPIS_BLOCK, 1);
		case 27: return new ItemStack(Material.MOSSY_COBBLESTONE, 1);
		case 28: return new ItemStack(Material.PUMPKIN, 1);
		case 29: return new ItemStack(Material.SNOW, 1);
		case 30: return new ItemStack(Material.COAL_ORE, 1);
		case 31: return new ItemStack(Material.COBBLESTONE, 1);
		case 32: return new ItemStack(Material.WOOD, 1);
		case 33: return new ItemStack(Material.TNT, 1);
		case 34: return new ItemStack(Material.SPONGE, 1);
		default: return null;
		}
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
		switch(index){
		case 0: return "&cRed" ; // &4 hard to see
		case 1: return "&bBlue"; // &2 was hard to see
		case 2: return "&fWhite";
		case 3: return "&0Black";
		case 4: return "&6Gold";
		case 5: return "&2Green";
		case 6: return "&dPink";
		case 7: return "&bAqua";
		case 8: return "&8Gray";
		case 9: return "&eYellow";
		case 10: return "&aLime";
		case 11: return "&7Silver";
		case 12: return "&3Cyan";
		case 13: return "&5Purple";
		case 14: return "&8Brown";
		case 15: return "&9Magenta";
		case 16: return "&9Ice";
		case 17: return "&7Iron";
		case 18: return "&bDiamond";
		case 19: return "&cBrick";
		case 20: return "&fGlass";
		case 21: return "&fSand";
		case 22: return "&2Leaf";
		case 23: return "&4Nether";
		case 24: return "&0Obby";
		case 25: return "&8Gravel";
		case 26: return "&1Lapis";
		case 27: return "&2Mossy";
		case 28: return "&6Pumpkin";
		case 29: return "&fSnow";
		case 30: return "&7Coal";
		case 31: return "&7Cobblestone";
		case 32: return "&8Wood";
		case 33: return "&cTNT";
		case 34: return "&eSponge";
		default: 
			return "Team" + index;
		}
	}

	public static Integer getTeamIndex(String op) {
		return map.get(op.toUpperCase());
	}

}
