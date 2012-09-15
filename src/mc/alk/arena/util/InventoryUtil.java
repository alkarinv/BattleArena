package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.Container;
import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil {
	static final String version = "InventoryUtil 2.1.3.4";
	static final boolean DEBUG = false;

	public static class Armor{
		final public ArmorLevel level;
		final public ArmorType type;
		Armor(ArmorType at, ArmorLevel al){this.level = al; this.type = at;}
	}
	public static class EnchantmentWithLevel{
		public EnchantmentWithLevel(){}
		public EnchantmentWithLevel(boolean all){this.all = all;}
		public Enchantment e;
		public Integer lvl;
		boolean all = false;
		public String toString(){return  (e !=null?e.getName():"null")+":" + lvl;}
	}

	public enum ArmorLevel{WOOL,LEATHER,IRON,GOLD,CHAINMAIL,DIAMOND};
	public enum ArmorType{HELM,CHEST,LEGGINGS,BOOTS};

	public static Enchantment getEnchantmentByCommonName(String iname){
		iname = iname.toLowerCase();
		if (iname.contains("smite")) return Enchantment.getByName("DAMAGE_UNDEAD"); 
		if (iname.contains("sharp")) return Enchantment.getByName("DAMAGE_ALL"); 
		if (iname.contains("fire") && iname.contains("prot")) return Enchantment.getByName("PROTECTION_FIRE"); 
		if (iname.contains("fire")) return Enchantment.getByName("FIRE_ASPECT"); 
		if (iname.contains("exp") && iname.contains("prot")) return Enchantment.getByName("PROTECTION_EXPLOSIONS"); 
		if (iname.contains("blast") && iname.contains("prot")) return Enchantment.getByName("PROTECTION_EXPLOSIONS"); 
		if (iname.contains("arrow") && iname.contains("prot")) return Enchantment.getByName("PROTECTION_PROJECTILE"); 
		if (iname.contains("proj") && iname.contains("prot")) return Enchantment.getByName("PROTECTION_PROJECTILE"); 
		if (iname.contains("respiration")) return Enchantment.getByName("OXYGEN"); 
		if (iname.contains("fall")) return Enchantment.getByName("PROTECTION_FALL"); 
		if (iname.contains("prot")) return Enchantment.getByName("PROTECTION_ENVIRONMENTAL"); 
		if (iname.contains("oxygen")) return Enchantment.getByName("OXYGEN"); 
		if (iname.contains("aqua")) return Enchantment.getByName("WATER_WORKER"); 
		if (iname.contains("arth")) return Enchantment.getByName("DAMAGE_ARTHROPODS"); 
		if (iname.contains("knockback")) return Enchantment.getByName("KNOCKBACK"); 
		if (iname.contains("loot")) return Enchantment.getByName("LOOT_BONUS_MOBS"); 
		if (iname.contains("dig")) return Enchantment.getByName("DIG_SPEED"); 
		if (iname.contains("silk")) return Enchantment.getByName("SILK_TOUCH"); 
		if (iname.contains("unbreaking")) return Enchantment.getByName("DURABILITY"); 
		if (iname.contains("dura")) return Enchantment.getByName("DURABILITY"); 
		if (iname.contains("inf")) return Enchantment.getByName("ARROW_INFINITE"); 
		if (iname.contains("flame")) return Enchantment.getByName("ARROW_FIRE"); 
		if (iname.contains("power")) return Enchantment.getByName("ARROW_DAMAGE"); 
		if (iname.contains("punch")) return Enchantment.getByName("ARROW_KNOCKBACK"); 
		return null;
	}


	static final HashMap<Material,Armor> armor;
	static {
		armor = new HashMap<Material,Armor>();
		armor.put(Material.WOOL,new Armor(ArmorType.HELM, ArmorLevel.WOOL));
		armor.put(Material.LEATHER_HELMET,new Armor(ArmorType.HELM, ArmorLevel.LEATHER));
		armor.put(Material.IRON_HELMET,new Armor(ArmorType.HELM, ArmorLevel.IRON));
		armor.put(Material.GOLD_HELMET,new Armor(ArmorType.HELM, ArmorLevel.GOLD));
		armor.put(Material.DIAMOND_HELMET,new Armor(ArmorType.HELM, ArmorLevel.DIAMOND));
		armor.put(Material.CHAINMAIL_HELMET,new Armor(ArmorType.HELM, ArmorLevel.CHAINMAIL));

		armor.put(Material.LEATHER_CHESTPLATE,new Armor(ArmorType.CHEST,ArmorLevel.LEATHER));
		armor.put(Material.IRON_CHESTPLATE,new Armor(ArmorType.CHEST,ArmorLevel.IRON));
		armor.put(Material.GOLD_CHESTPLATE,new Armor(ArmorType.CHEST,ArmorLevel.GOLD));
		armor.put(Material.DIAMOND_CHESTPLATE,new Armor(ArmorType.CHEST,ArmorLevel.DIAMOND));
		armor.put(Material.CHAINMAIL_CHESTPLATE,new Armor(ArmorType.CHEST,ArmorLevel.CHAINMAIL));

		armor.put(Material.LEATHER_LEGGINGS,new Armor(ArmorType.LEGGINGS,ArmorLevel.LEATHER));
		armor.put(Material.IRON_LEGGINGS,new Armor(ArmorType.LEGGINGS,ArmorLevel.IRON));
		armor.put(Material.GOLD_LEGGINGS,new Armor(ArmorType.LEGGINGS,ArmorLevel.GOLD));
		armor.put(Material.DIAMOND_LEGGINGS,new Armor(ArmorType.LEGGINGS,ArmorLevel.DIAMOND));
		armor.put(Material.CHAINMAIL_LEGGINGS,new Armor(ArmorType.LEGGINGS,ArmorLevel.CHAINMAIL));

		armor.put(Material.LEATHER_BOOTS,new Armor(ArmorType.BOOTS,ArmorLevel.LEATHER));
		armor.put(Material.IRON_BOOTS,new Armor(ArmorType.BOOTS,ArmorLevel.IRON));
		armor.put(Material.GOLD_BOOTS,new Armor(ArmorType.BOOTS,ArmorLevel.GOLD));
		armor.put(Material.DIAMOND_BOOTS,new Armor(ArmorType.BOOTS,ArmorLevel.DIAMOND));
		armor.put(Material.CHAINMAIL_BOOTS,new Armor(ArmorType.BOOTS,ArmorLevel.CHAINMAIL));
	}

	public static int arrowCount(Player p) {
		return getItemAmount(p.getInventory().getContents(), new ItemStack(Material.ARROW,1));
	}

	public static int getItemAmountFromInventory(Inventory inv, ItemStack is) {
		return getItemAmount(inv.getContents(), is);
	}

	public static boolean hasArmor(Player p) {
		PlayerInventory pi= p.getInventory();
		return(	(pi.getBoots() != null && pi.getBoots().getType() != Material.AIR) && 
				(pi.getHelmet() != null && pi.getBoots().getType() != Material.AIR) && 
				(pi.getLeggings() != null && pi.getBoots().getType() != Material.AIR) && 
				(pi.getChestplate() != null && pi.getBoots().getType() != Material.AIR) );
	}

	public static ArmorLevel hasArmorSet(List<ItemStack> inv) {
		ArmorLevel armorSet[] = new ArmorLevel[4];
		for (ItemStack is: inv){
			Armor a = armor.get(is.getType());
			if (a == null)
				continue;
			switch (a.type){
			case BOOTS: armorSet[0] = a.level; break;
			case LEGGINGS: armorSet[1] = a.level; break;
			case CHEST: armorSet[2] = a.level; break;
			case HELM: armorSet[3] = a.level; break;
			}
		}
		ArmorLevel lvl = null;
		for (ArmorLevel a: armorSet){
			if (lvl == null)
				lvl = a;
			else if (lvl != a)
				return null;
		}
		return lvl;
	}
	//
	//	private static Material getMaterial(ArmorLevel level) {
	//		switch(level){
	//		case WOOL: return Material.WOOL;
	//		case LEATHER : return Material.LEATHER;
	//		case IRON: return Material.IRON_BOOTS;
	//		case GOLD : return Material.GOLD_BOOTS;
	//		case CHAINMAIL: return Material.CHAINMAIL_BOOTS;
	//		case DIAMOND: return Material.DIAMOND;
	//		default : return null;
	//		}
	//	}

	public static int getItemAmount(ItemStack[] items, ItemStack is){
		int count = 0;
		for (ItemStack item : items) {
			if (item == null) {
				continue;}
			if (item.getType() == is.getType() && ((item.getDurability() == is.getDurability() || item.getDurability() == -1) )) {
				count += item.getAmount();
			}
		}
		return count;
	}

	/**
	 * Return a item stack from a given string
	 * @param name
	 * @return
	 */
	public static ItemStack getItemStack(String name) {
		if (name == null || name.isEmpty())
			return null;
		name = name.replace(" ", "_");
		name = name.replace(":", ";");

		int dataIndex = name.indexOf(';');
		dataIndex = (dataIndex != -1 ? dataIndex : -1);
		int dataValue = 0;
		if (dataIndex != -1){
			dataValue = (isInt(name.substring(dataIndex + 1)) ? Integer.parseInt(name.substring(dataIndex + 1)) : 0);
			name = name.substring(0,dataIndex);
		}

		dataValue = dataValue < 0 ? 0 : dataValue;
		Material mat = getMat(name);

		if (mat != null && mat != Material.AIR) {
			return new ItemStack(mat, 0, (short) dataValue);
		}
		return null;
	}

	public static boolean isInt(String i) {try {Integer.parseInt(i);return true;} catch (Exception e) {return false;}}
	public static boolean isFloat(String i){try{Float.parseFloat(i);return true;} catch (Exception e){return false;}}

	/// Get the Material
	public static Material getMat(String name) {
		Integer id =null;
		try{ id = Integer.parseInt(name);}catch(Exception e){}
		if (id == null){
			id = getMaterialID(name);}
		return id != null && id >= 0 ? Material.getMaterial(id) : null;
	}

	/// This allows for abbreviations to work, useful for sign etc
	public static int getMaterialID(String name) {
		name = name.toUpperCase();
		/// First try just getting it from the Material Name
		Material mat = Material.getMaterial(name);
		if (mat != null)
			return mat.getId();
		/// Might be an abbreviation, or a more complicated
		int temp = Integer.MAX_VALUE;
		mat = null;
		name = name.replaceAll("\\s+", "").replaceAll("_", "");
		for (Material m : Material.values()) {
			if (m.name().replaceAll("_", "").startsWith(name)) {
				if (m.name().length() < temp) {
					mat = m;
					temp = m.name().length();
				}
			}
		}
		return mat != null ? mat.getId() : -1;
	}

	public static boolean hasItem(Player p, ItemStack itemType) {
		PlayerInventory inv = p.getInventory();
		for (ItemStack is : inv.getContents()){
			if (is != null && is.getType() == itemType.getType()){
				return true;}
		}
		for (ItemStack is : inv.getArmorContents()){
			if (is != null && is.getType() == itemType.getType()){
				return true;}
		}		
		return false;
	}

	public static boolean hasAnyItem(Player p) {
		PlayerInventory inv = p.getInventory();
		for (ItemStack is : inv.getContents()){
			if (is != null && is.getType() != Material.AIR){
				//				System.out.println("item=" + is);
				return true;}
		}
		for (ItemStack is : inv.getArmorContents()){
			if (is != null && is.getType() != Material.AIR){
				//				System.out.println("item=" + is);
				return true;}
		}

		EntityHuman eh = ((CraftPlayer)p).getHandle();
		try { 
			/// check crafting square
			ContainerPlayer cp = (ContainerPlayer) eh.defaultContainer;
			for (net.minecraft.server.ItemStack is: cp.craftInventory.getContents()){
				if (is != null && is.id != 0)
					return true;
			}
			/// Check for a workbench
			Container container = (Container) eh.activeContainer;
			final int size = container.b.size();
			for (int i=0;i< size;i++){
				net.minecraft.server.ItemStack is = container.getSlot(i).getItem();
				if (is != null && is.id != 0)
					return true;
			}

		} catch (Exception e){}
		return false;
	}

	public static void addItemToInventory(Player player, ItemStack itemStack) {
		addItemToInventory(player,itemStack,itemStack.getAmount(),true,false);
	}

	public static void addItemToInventory(Player player, ItemStack itemStack, int stockAmount, boolean update) {
		addItemToInventory(player,itemStack,stockAmount,update,false);
	}

	@SuppressWarnings("deprecation")
	public static void addItemsToInventory(Player p, List<ItemStack> items, final boolean ignoreCustomHelmet) {
		for (ItemStack is : items){
			InventoryUtil.addItemToInventory(p, is.clone(), is.getAmount(), false, ignoreCustomHelmet);
		}
		try { p.updateInventory(); } catch (Exception e){}
	}

	@SuppressWarnings("deprecation")
	public static void addItemToInventory(Player player, ItemStack itemStack, int stockAmount, boolean update, boolean ignoreCustomHelmet) {
		PlayerInventory inv = player.getInventory();
		Material itemType =itemStack.getType();
		if (armor.containsKey(itemType)){
			final boolean isHelmet = armor.get(itemType).type == ArmorType.HELM;
			/// no item: add to armor slot
			/// item better: add old to inventory, new to armor slot
			/// item notbetter: add to inventory
			ItemStack oldArmor = getArmorSlot(inv,armor.get(itemType));
			boolean empty = (oldArmor == null || oldArmor.getType() == Material.AIR);
			boolean better = empty ? false : armorSlotBetter(armor.get(oldArmor.getType()),armor.get(itemType));
			if (empty || better){
				switch (armor.get(itemType).type){
				case HELM: 
					if (!empty && ignoreCustomHelmet){

					} else{
						inv.setHelmet(itemStack);
					}
					break;
				case CHEST: inv.setChestplate(itemStack); break;
				case LEGGINGS: inv.setLeggings(itemStack); break;
				case BOOTS: inv.setBoots(itemStack); break;
				}
			} 
			if (!empty){
				if (better && !(isHelmet && ignoreCustomHelmet)){
					addItemToInventory(inv, oldArmor,oldArmor.getAmount());
				} else {
					addItemToInventory(inv, itemStack,stockAmount);			
				}
			}			
		} else {
			addItemToInventory(inv, itemStack,stockAmount);			
		}
		if (update)
			try { player.updateInventory(); } catch (Exception e){}
	}

	private static boolean armorSlotBetter(Armor oldArmor, Armor newArmor) {
		if (oldArmor == null || newArmor == null) /// technically we could throw an exception.. but nah
			return false;
		return oldArmor.level.ordinal() < newArmor.level.ordinal();
	}

	private static ItemStack getArmorSlot(PlayerInventory inv, Armor armor) {
		switch (armor.type){
		case HELM: return inv.getHelmet();
		case CHEST: return inv.getChestplate();
		case LEGGINGS: return inv.getLeggings();
		case BOOTS:return inv.getBoots();
		}
		return null;
	}

	///Adds item to inventory
	public static void addItemToInventory(Inventory inv, ItemStack is, int left){
		int maxStackSize = is.getType().getMaxStackSize();
		if(left <= maxStackSize){
			is.setAmount(left);
			inv.addItem(is);
			return;
		}

		if(maxStackSize != 64){
			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
			for (int i = 0; i < Math.ceil(left / maxStackSize); i++) {
				if (left < maxStackSize) {
					is.setAmount(left);
					items.add(is);
					return;
				}else{
					is.setAmount(maxStackSize);
					items.add(is);
				}
			}
			Object[] iArray = items.toArray();
			for(Object o : iArray){
				inv.addItem((ItemStack) o);
			}
		}else{
			inv.addItem(is);
		}
	}

	public static void closeInventory(Player p) {
		EntityHuman eh = ((CraftPlayer)p).getHandle();
		try{
			if ((eh instanceof EntityPlayer)){
				EntityPlayer ep = (EntityPlayer) eh;
				ep.closeInventory();
			}
		}catch(Exception closeInventoryError){
			/// This almost always throws an NPE, but does its job so ignore
		}
	}

	public static void clearInventory(Player p) {
		//		System.out.println("Clearing inventory of " + p.getName());
		try{
			PlayerInventory inv = p.getInventory();
			closeInventory(p);
			if (inv != null){
				inv.clear();
				inv.setArmorContents(null);
				inv.setItemInHand(null);
			}
		} catch(Exception ee){
			ee.printStackTrace();
		}
	}

	public static Object getCommonName(ItemStack is) {
		int id = is.getTypeId();
		int datavalue = is.getDurability();
		if (datavalue > 0){
			return Material.getMaterial(id).toString() + ":" + datavalue;
		}
		return Material.getMaterial(id).toString();
	}


	public static boolean isItem(String str){
		//		System.out.println("string = " + str);
		str = str.replaceAll("[}{]", "");
		str = str.replaceAll("=", " ");
		if (DEBUG) System.out.println("item=" + str);
		ItemStack is =null;
		try{
			String split[] = str.split(" ");
			is = InventoryUtil.getItemStack(split[0].trim());
			is.setAmount(Integer.valueOf(split[split.length -1]));
			for (int i = 1; i < split.length-1;i++){
				EnchantmentWithLevel ewl = getEnchantment(split[i].trim());
				try {
					is.addEnchantment(ewl.e, ewl.lvl);
				} catch (IllegalArgumentException iae){
					return false;
				}
			}
		} catch(Exception e){
			return false;
		}
		return true;
	}
	public static ItemStack parseItem(String str) throws Exception{
		str = str.replaceAll("[}{]", "");
		str = str.replaceAll("=", " ");
		if (DEBUG) System.out.println("item=" + str);
		ItemStack is =null;
		try{
			String split[] = str.split(" ");
			is = InventoryUtil.getItemStack(split[0].trim());
			//			System.out.println(" split = " + split);
			final int amt = split.length > 1 ? Integer.valueOf(split[split.length -1]) : 1; 
			is.setAmount(amt);
			for (int i = 1; i < split.length-1;i++){
				EnchantmentWithLevel ewl = getEnchantment(split[i].trim());
				try {
					is.addEnchantment(ewl.e, ewl.lvl);
				} catch (IllegalArgumentException iae){
					Logger.getLogger("minecraft").warning(ewl+" can not be applied to the item " + str);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			Logger.getLogger("minecraft").severe("Couldnt parse item=" + str);
			throw new Exception("parse item was bad");
		}
		return is;
	}

	public static EnchantmentWithLevel getEnchantment(String str) {
		if (str.equalsIgnoreCase("all")){
			return new EnchantmentWithLevel(true);
		}
		Enchantment e = null;
		str = str.replaceAll(",", ":");
		int index = str.indexOf(':');
		index = (index != -1 ? index : -1);
		int lvl = -1;
		if (index != -1){
			try {lvl = Integer.parseInt(str.substring(index + 1)); } catch (Exception err){}
			str = str.substring(0,index);
		} 

		//        System.out.println("String = <" + str +">   " + lvl);
		try {e = Enchantment.getById(Integer.valueOf(str));} catch (Exception err){}
		if (e == null)
			e = Enchantment.getByName(str);
		if (e == null)
			e = getEnchantmentByCommonName(str);
		if (e == null)
			return null;
		EnchantmentWithLevel ewl = new EnchantmentWithLevel();
		ewl.e = e;
		if (lvl < e.getStartLevel()){lvl = e.getStartLevel();}
		if (lvl > e.getMaxLevel()){lvl = e.getMaxLevel();}
		ewl.lvl = lvl;
		return ewl;
	}

	public boolean addEnchantments(ItemStack is, String[] args) {
		Map<Enchantment,Integer> encs = new HashMap<Enchantment,Integer>();
		for (String s : args){
			EnchantmentWithLevel ewl = getEnchantment(s);
			if (ewl != null){
				if (ewl.all){
					return addAllEnchantments(is);}					
				encs.put(ewl.e, ewl.lvl);
			}
		}
		addEnchantments(is,encs);
		return true;
	}

	public void addEnchantments(ItemStack is,Map<Enchantment, Integer> enchantments) {
		for (Enchantment e: enchantments.keySet()){
			if (e.canEnchantItem(is)){
				is.addUnsafeEnchantment(e, enchantments.get(e));
			}
		}
	}

	public boolean addAllEnchantments(ItemStack is) {
		for (Enchantment enc : Enchantment.values()){
			if (enc.canEnchantItem(is)){
				is.addUnsafeEnchantment(enc, enc.getMaxLevel());
			}
		}
		return true;
	}

	/**
	 * For Serializing an item or printing
	 * @param is
	 * @return
	 */
	public static String getItemString(ItemStack is) {
		StringBuilder sb = new StringBuilder();
		sb.append(is.getType().toString() +":"+(byte)is.getDurability()+" ");
		Map<Enchantment,Integer> encs = is.getEnchantments();
		for (Enchantment enc : encs.keySet()){
			sb.append(enc.getName() + ":" + encs.get(enc)+" ");
		}
		sb.append(is.getAmount());
		return sb.toString();
	}

	public static boolean hasEnchantedItem(Player p) {
		PlayerInventory inv = p.getInventory();
		for (ItemStack is : inv.getContents()){
			if (is != null && is.getType() != Material.AIR ){
				Map<Enchantment,Integer> enc = is.getEnchantments();
				if (enc != null && !enc.isEmpty())
					return true;
			}
		}
		for (ItemStack is : inv.getArmorContents()){
			if (is != null && is.getType() != Material.AIR){
				Map<Enchantment,Integer> enc = is.getEnchantments();
				if (enc != null && !enc.isEmpty())
					return true;
			}
		}
		return false;
	}

	public static boolean sameMaterial(ArmorLevel lvl, ItemStack is) {
		Armor a = armor.get(is.getType());
		if (a == null)
			return false;
		return a.level == lvl;
	}

	public static ItemStack getWool(int color) {
		return new ItemStack(Material.WOOL,1,(short) color);
	}

	public static void printInventory(Player p) {
		PlayerInventory pi = p.getInventory();
		System.out.println("Inventory of "+p.getName());
		for (ItemStack is: pi.getContents()){
			if (is != null && is.getType() != Material.AIR){
				System.out.println(getCommonName(is) +"  " + is.getAmount());}
		}
		for (ItemStack is: pi.getArmorContents()){
			if (is != null && is.getType() != Material.AIR){
				System.out.println(getCommonName(is) +"  " + is.getAmount());}
		}
	}

	public static class ItemComparator implements Comparator<ItemStack>{
		@Override
		public int compare(ItemStack arg0, ItemStack arg1) {
			if (arg0 == null && arg1 == null)
				return 0;
			if (arg0 == null)
				return 1;
			if (arg1 == null)
				return -1;
			Integer i = arg0.getTypeId();
			int c = i.compareTo(arg1.getTypeId());
			if (c!= 0)
				return c;
			Short s= arg0.getDurability();
			c = s.compareTo(arg1.getDurability());
			if (c!= 0)
				return c;
			i = arg0.getAmount();
			return i.compareTo(arg1.getAmount());
		}		
	}

	public static boolean sameItems(List<ItemStack> items, PlayerInventory inv, boolean woolTeams) {
		ItemStack[] contents =inv.getContents();
		ItemStack[] armor = inv.getArmorContents();
		//// I could check size right now if it werent for "air" and null blocks in inventories
		List<ItemStack> pitems = new ArrayList<ItemStack>();
		pitems.addAll(Arrays.asList(contents));
		if (woolTeams){ /// ignore helmet
			if (inv.getBoots() != null) pitems.add(inv.getBoots());
			if (inv.getLeggings() != null) pitems.add(inv.getLeggings());
			if (inv.getChestplate() != null) pitems.add(inv.getChestplate());
		} else {
			pitems.addAll(Arrays.asList(armor));			
		}
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		for (ItemStack is: items){
			if (is == null || is.getType() == Material.AIR)
				continue;
			while (is.getAmount() > is.getMaxStackSize()){
				is.setAmount(is.getAmount() - is.getMaxStackSize());
				ItemStack is2 = new ItemStack(is);
				is.setAmount(is2.getMaxStackSize());
				stacks.add(is2);
			}	
		}
		items.addAll(stacks);

		Collections.sort(items, new ItemComparator());
		Collections.sort(pitems, new ItemComparator());
		int i1 = 0, i2 = 0;
		ItemStack is1, is2;
		int nullSize1 = 0, nullSize2 = 0; /// amount of air and null
//		int similar = 0;
//		for (ItemStack is: items){
//			if (is == null || is.getType() == Material.AIR){
//				continue;}
//			Log.debug("ITEM1  " + is);
//
//		}
//		for (ItemStack is: pitems){
//			if (is == null || is.getType() == Material.AIR){
//				continue;}
//			Log.debug("ITEM2  " + is);
//
//		}

		while (i1< items.size() && i2<pitems.size()){
			is1 = items.get(i1);
			is2 = pitems.get(i2);
//			System.out.println("item1 = " + is1 +" ************************  " + is2);
			if (is1 == null || is1.getType() == Material.AIR){
				nullSize1++;
				continue;
			}
			if (is2 == null || is2.getType() == Material.AIR){
				nullSize2++;
				continue;
			}
			/// Alright, now that we dont have to worry about null or air
			if (is1.getTypeId() != is2.getTypeId() || is1.getDurability() != is2.getDurability() ||
					is1.getAmount() != is2.getAmount()){
				return false;
			}
			else {
//				similar++;
			}
			i1++; 
			i2++;
		}
		for (int i=i1;i<items.size();i++){
			is1 = items.get(i);
			if (is1 == null || is1.getType() == Material.AIR){
				nullSize1++;
				continue;
			}			
		}
		for (int i=i2;i<pitems.size();i++){
			is2 = pitems.get(i);
			if (is2 == null || is2.getType() == Material.AIR){
				nullSize2++;
				continue;
			}			
		}
		return items.size() - nullSize1 == pitems.size() - nullSize2;
	}

}
