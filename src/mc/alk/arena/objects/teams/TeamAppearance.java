package mc.alk.arena.objects.teams;

import java.awt.Color;

import mc.alk.arena.util.MessageUtil;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class TeamAppearance {
	final String name;
	final ItemStack headItem;
	final ChatColor chatColor;
	final Color color;

	public TeamAppearance(ItemStack is, String name, Color color){
		this.headItem = is;
		this.name = name;
		this.chatColor = MessageUtil.getFirstColor(name);
		this.color = color;
	}
	public String getName(){
		return name;
	}
	public ItemStack getItem(){
		return headItem;
	}
	public ChatColor getChatColor(){
		return chatColor;
	}
	public Color getColor(){
		return color;
	}
}