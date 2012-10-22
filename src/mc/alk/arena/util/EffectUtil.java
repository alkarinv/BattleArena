package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mc.alk.arena.Defaults;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MobEffect;
import net.minecraft.server.MobEffectList;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class EffectUtil {
	public static final String version ="1.1";
	public static class EffectWithArgs {
		public MobEffectList mel;
		public Integer strength = null;
		public Integer time = null;


		public String getCommonName() {
			return EffectUtil.getCommonName(this);
		}
		public String toString(){
			return mel.id + " " + strength +" " + time;
		}

	}

	static final HashMap<MobEffectList,String> effectToName = new HashMap<MobEffectList,String>();
	static final HashMap<String,MobEffectList> nameToEffect= new HashMap<String,MobEffectList>();
	static{
		effectToName.put(MobEffectList.FASTER_MOVEMENT, "speed");
		effectToName.put(MobEffectList.SLOWER_MOVEMENT, "slowness");
		effectToName.put(MobEffectList.FASTER_DIG, "haste");
		effectToName.put(MobEffectList.SLOWER_DIG,"slownewss");
		effectToName.put(MobEffectList.INCREASE_DAMAGE, "strength");
		effectToName.put(MobEffectList.HEAL, "heal");
		effectToName.put(MobEffectList.HARM, "harm");
		effectToName.put(MobEffectList.JUMP, "jump");
		effectToName.put(MobEffectList.CONFUSION, "confusion");
		effectToName.put(MobEffectList.REGENERATION, "regen");
		effectToName.put(MobEffectList.RESISTANCE, "resistance");
		effectToName.put(MobEffectList.FIRE_RESISTANCE, "fireresistance");
		effectToName.put(MobEffectList.WATER_BREATHING, "waterbreathing");
		effectToName.put(MobEffectList.INVISIBILITY, "invisibility");
		effectToName.put(MobEffectList.BLINDNESS, "blindness");
		effectToName.put(MobEffectList.NIGHT_VISION,"night vision");
		effectToName.put(MobEffectList.HUNGER, "hunger");
		effectToName.put(MobEffectList.WEAKNESS, "weakness");
		effectToName.put(MobEffectList.POISON, "poison");
		nameToEffect.put("speed", MobEffectList.FASTER_MOVEMENT);
		nameToEffect.put("slowness", MobEffectList.SLOWER_MOVEMENT);
		nameToEffect.put("haste", MobEffectList.FASTER_DIG);
		nameToEffect.put("slowdig", MobEffectList.SLOWER_DIG);
		nameToEffect.put("strength", MobEffectList.INCREASE_DAMAGE);
		nameToEffect.put("heal", MobEffectList.HEAL);
		nameToEffect.put("harm", MobEffectList.HARM);
		nameToEffect.put("jump", MobEffectList.JUMP);
		nameToEffect.put("confusion", MobEffectList.CONFUSION);
		nameToEffect.put("regeneration", MobEffectList.REGENERATION);
		nameToEffect.put("resistance", MobEffectList.RESISTANCE);
		nameToEffect.put("fireresistance", MobEffectList.FIRE_RESISTANCE);
		nameToEffect.put("waterbreathing", MobEffectList.WATER_BREATHING);
		nameToEffect.put("invisibility", MobEffectList.INVISIBILITY);
		nameToEffect.put("blindness", MobEffectList.BLINDNESS);
		nameToEffect.put("nightvision", MobEffectList.NIGHT_VISION);
		nameToEffect.put("hunger", MobEffectList.HUNGER);
		nameToEffect.put("weakness", MobEffectList.WEAKNESS);
		nameToEffect.put("poison", MobEffectList.POISON);
	}

	public static MobEffectList getEffect(String buffName){
		buffName = buffName.toLowerCase();
		if (nameToEffect.containsKey(buffName))
			return nameToEffect.get(buffName);
		if (buffName.contains("slow")) return MobEffectList.SLOWER_MOVEMENT;
		else if (buffName.contains("fastdig")) return MobEffectList.FASTER_DIG;
		else if (buffName.contains("fasterdig")) return MobEffectList.FASTER_DIG;
		else if (buffName.contains("regen")) return MobEffectList.REGENERATION;
		else if (buffName.contains("resis")) return MobEffectList.RESISTANCE;
		else if (buffName.contains("waterb")) return MobEffectList.WATER_BREATHING;
		else if (buffName.contains("invis")) return MobEffectList.INVISIBILITY;
		else if (buffName.contains("blind")) return MobEffectList.BLINDNESS;
		return null;
	}


	public static void doEffect(EntityHuman player, MobEffectList mel, int time, int strength) {
		doEffect((EntityLiving)player,mel,time,strength);
	}

	private static void doEffect(EntityLiving el, MobEffectList mel, int time, int strength) {
		try{
			el.addEffect(new MobEffect(mel.id, time * 20, strength));
		} catch(Exception e){
			if (!Defaults.DEBUG_VIRTUAL) e.printStackTrace();
		}
	}

	public static void enchantPlayer(Player player, List<EffectWithArgs> ewas){
		if (Defaults.DEBUG_VIRTUAL) return;
		try{
			EntityHuman eh = ((CraftPlayer)player).getHandle();
			for (EffectWithArgs ewa : ewas){
				doEffect(eh, ewa.mel,ewa.time,ewa.strength);
			}
		} catch (Exception e){
			if (!Defaults.DEBUG_VIRTUAL) e.printStackTrace();
		}
	}

	public static String getEnchantString(List<EffectWithArgs> ewas){
		try{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (EffectWithArgs ewa : ewas){
				if (!first) sb.append(",");
				String commonName = EffectUtil.getCommonName(ewa);
				sb.append(commonName);
				first = false;
			}
			String enchants = sb.toString();
			return enchants;
		} catch (Exception e){
			if (!Defaults.DEBUG_VIRTUAL) e.printStackTrace();
		}
		return null;
	}

	public static List<EffectWithArgs> parseArgs(String arg, int strength, int time) {
		String args[] = arg.split(" ");
		List<EffectWithArgs> ewas = new ArrayList<EffectWithArgs>();
		for (String effect: args){
			EffectWithArgs ewa = EffectUtil.parseArg(effect,strength, time);
			if (ewa != null)
				ewas.add(ewa);
		}
		return ewas;
	}

	public static EffectWithArgs parseArg(String arg, int strength, int time) {
		arg = arg.replaceAll(",", ":");
		String split[] = arg.split(":");
		EffectWithArgs ewa = new EffectWithArgs();
		try {
			MobEffectList mel = getEffect(split[0].trim());
			if (mel == null)
				return null;
			ewa.mel = mel;
			if (split.length > 1){try{ewa.strength = Integer.valueOf(split[1]) -1;} catch (Exception e){}}
			else { ewa.strength = strength;}

			if (split.length > 2){try{ewa.time = Integer.valueOf(split[2]);} catch (Exception e){}}
			else {ewa.time = time;}
		} catch (Exception e){
			return null;
		}
		return ewa;
	}

	public static void unenchantAll(Player p) {
		if (Defaults.DEBUG_VIRTUAL) return;
		try{
			EntityHuman eh = ((CraftPlayer)p).getHandle();

			for (MobEffectList mel : EffectUtil.effectToName.keySet()){
				if(eh.hasEffect(mel)){
					int mod = eh.getEffect(mel).getAmplifier();
					eh.addEffect(new MobEffect(mel.id, -1, mod+1));
				}			
			}	
		} catch(Exception e){
			if (!Defaults.DEBUG_VIRTUAL) e.printStackTrace();
		}
	}
	public static String getCommonName(EffectWithArgs ewa){
		return effectToName.get(ewa.mel);
	}

}
