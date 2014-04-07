package mc.alk.arena.objects.teams;

import mc.alk.arena.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

import java.awt.*;

public class TeamAppearance {
	final String name;
	final ItemStack headItem;
    final ChatColor chatColor;
    final DyeColor dyeColor;
	final Color color;

	public TeamAppearance(ItemStack is, String name, Color color){
		this.headItem = is;
		this.name = name;
		this.chatColor = MessageUtil.getFirstColor(name);
		this.color = color;
        this.dyeColor = findDyeColor(color);
    }

    private DyeColor findDyeColor(Color color) {
        DyeColor closest = DyeColor.WHITE;
        double min = Float.MAX_VALUE;
        for (DyeColor dc : DyeColor.values()) {
            org.bukkit.Color c = dc.getColor();
            double dev = (Math.pow(Math.abs(c.getRed() - color.getRed()),2)) +
                    (Math.pow(Math.abs(c.getGreen() - color.getGreen()),2)) +
                            (Math.pow(Math.abs(c.getBlue() - color.getBlue()),2));
            if (dev < min) {
                min = dev;
                closest = dc;
            }
        }
        return closest;
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

    public DyeColor getDyeColor(){
        return dyeColor;
    }
}
