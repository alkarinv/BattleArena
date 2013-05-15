package mc.alk.arena.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mc.alk.arena.Defaults;
import mc.alk.arena.util.compat.IInventoryHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil {
	static final String version = "BA InventoryUtil 2.1.6";
	static final boolean DEBUG = false;
	static IInventoryHelper handler = null;

	static {
		Class<?>[] args = {};
		try {
			final String pkg = Bukkit.getServer().getClass().getPackage().getName();
			String version = pkg.substring(pkg.lastIndexOf('.') + 1);
			final Class<?> clazz;
			if (version.equalsIgnoreCase("craftbukkit")){
				clazz = Class.forName("mc.alk.arena.util.compat.pre.InventoryHelper");
			} else{
				clazz = Class.forName("mc.alk.arena.util.compat.post.InventoryHelper");
			}

			handler = (IInventoryHelper) clazz.getConstructor(args).newInstance((Object[])args);
		} catch (Exception e) {
			try{
				final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.pre.InventoryHelper");
				handler = (IInventoryHelper) clazz.getConstructor(args).newInstance((Object[])args);
			} catch (Exception e2){
				e2.printStackTrace();
			}
			e.printStackTrace();
		}
	}

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
		@Override
		public String toString(){return  (e !=null?e.getName():"null")+":" + lvl;}
	}

	public static class PInv {
		public ItemStack[] contents;
		public ItemStack[] armor;
		public PInv() {}
		public PInv(PlayerInventory inventory) {
			contents = inventory.getContents();
			setArmor(inventory);
		}
		public PInv(List<ItemStack> items){
			contents = items.toArray(new ItemStack[items.size()]);
			armor = new ItemStack[0];
		}
		public void setArmor(PlayerInventory inventory){
			this.armor=new ItemStack[4];
			this.armor[ArmorType.HELM.ordinal()] = inventory.getHelmet();
			this.armor[ArmorType.CHEST.ordinal()] = inventory.getChestplate();
			this.armor[ArmorType.LEGGINGS.ordinal()] = inventory.getLeggings();
			this.armor[ArmorType.BOOTS.ordinal()] = inventory.getBoots();
		}
	}

	public enum ArmorLevel{WOOL,LEATHER,IRON,GOLD,CHAINMAIL,DIAMOND};
	public enum ArmorType{BOOTS,LEGGINGS,CHEST,HELM};

	public static Enchantment getEnchantmentByCommonName(String iname){
		iname = iname.toLowerCase();
		if (iname.contains("smite")) return Enchantment.DAMAGE_UNDEAD;
		if (iname.contains("sharp")) return Enchantment.DAMAGE_ALL;
		if (iname.contains("sharp")) return Enchantment.DAMAGE_ARTHROPODS;
		if (iname.contains("fire") && iname.contains("prot")) return Enchantment.PROTECTION_FIRE;
		if (iname.contains("fire")) return Enchantment.FIRE_ASPECT;
		if (iname.contains("exp") && iname.contains("prot")) return Enchantment.PROTECTION_EXPLOSIONS;
		if (iname.contains("blast") && iname.contains("prot")) return Enchantment.PROTECTION_EXPLOSIONS;
		if (iname.contains("arrow") && iname.contains("prot")) return Enchantment.PROTECTION_PROJECTILE;
		if (iname.contains("proj") && iname.contains("prot")) return Enchantment.PROTECTION_PROJECTILE;
		if (iname.contains("respiration")) return Enchantment.OXYGEN;
		if (iname.contains("fall")) return Enchantment.PROTECTION_FALL;
		if (iname.contains("prot")) return Enchantment.PROTECTION_ENVIRONMENTAL;
		if (iname.contains("respiration")) return Enchantment.OXYGEN;
		if (iname.contains("oxygen")) return Enchantment.OXYGEN;
		if (iname.contains("aqua")) return Enchantment.WATER_WORKER;
		if (iname.contains("arth")) return Enchantment.DAMAGE_ARTHROPODS;
		if (iname.contains("knockback")) return Enchantment.KNOCKBACK;
		if (iname.contains("loot")) return Enchantment.LOOT_BONUS_MOBS;
		if (iname.contains("lootmobs")) return Enchantment.LOOT_BONUS_MOBS;
		if (iname.contains("fortune")) return Enchantment.LOOT_BONUS_BLOCKS;;
		if (iname.contains("lootblocks")) return Enchantment.LOOT_BONUS_BLOCKS;
		if (iname.contains("dig")) return Enchantment.DIG_SPEED;
		if (iname.contains("eff")) return Enchantment.DIG_SPEED;
		if (iname.contains("silk")) return Enchantment.SILK_TOUCH;
		if (iname.contains("flame")) return Enchantment.ARROW_FIRE;
		if (iname.contains("power")) return Enchantment.ARROW_DAMAGE;
		if (iname.contains("punch")) return Enchantment.ARROW_KNOCKBACK;
		if (iname.contains("inf")) return Enchantment.ARROW_INFINITE;
		if (iname.contains("unbreaking")) return Enchantment.DURABILITY;
		if (iname.contains("dura")) return Enchantment.DURABILITY;
		if (iname.contains("thorn")) return Enchantment.THORNS;
		return null;
	}

	public static String getCommonNameByEnchantment(Enchantment enc){
		if (enc.getId() == Enchantment.PROTECTION_ENVIRONMENTAL.getId()){return "Protection";}
		else if (enc.getId() == Enchantment.PROTECTION_FIRE.getId()){return "Fire Protection";}
		else if (enc.getId() == Enchantment.PROTECTION_FALL.getId()){return "Fall Protection";}
		else if (enc.getId() == Enchantment.PROTECTION_EXPLOSIONS.getId()){return "Blast Protection";}
		else if (enc.getId() == Enchantment.PROTECTION_PROJECTILE.getId()){return "Projectile Protection";}
		else if (enc.getId() == Enchantment.OXYGEN.getId()){return "Respiration";}
		else if (enc.getId() == Enchantment.WATER_WORKER.getId()){return "Aqua Affinity";}
		else if (enc.getId() == Enchantment.DAMAGE_ALL.getId()){return "Sharp";}
		else if (enc.getId() == Enchantment.DAMAGE_UNDEAD.getId()){return "Smite";}
		else if (enc.getId() == Enchantment.DAMAGE_ARTHROPODS.getId()){return "Bane of Arthropods";}
		else if (enc.getId() == Enchantment.KNOCKBACK.getId()){return "Knockback";}
		else if (enc.getId() == Enchantment.FIRE_ASPECT.getId()){return "Fire Aspect";}
		else if (enc.getId() == Enchantment.LOOT_BONUS_MOBS.getId()){return "Looting";}
		else if (enc.getId() == Enchantment.DIG_SPEED.getId()){return "Efficiency";}
		else if (enc.getId() == Enchantment.SILK_TOUCH.getId()){return "Silk Touch";}
		else if (enc.getId() == Enchantment.DURABILITY.getId()){return "Unbreaking";}
		else if (enc.getId() == Enchantment.LOOT_BONUS_BLOCKS.getId()){return "Fortune";}
		else if (enc.getId() == Enchantment.ARROW_DAMAGE.getId()){return "Power";}
		else if (enc.getId() == Enchantment.ARROW_KNOCKBACK.getId()){return "Punch";}
		else if (enc.getId() == Enchantment.ARROW_FIRE.getId()){return "Flame";}
		else if (enc.getId() == Enchantment.ARROW_INFINITE.getId()){return "Infinity";}
		else if (enc.getId() == Enchantment.THORNS.getId()){return "Thorns";}
		else return enc.getName();
	}

	static final Map<Material,Armor> armor;
	static {
		armor = new EnumMap<Material,Armor>(Material.class);
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

	public static boolean isArmor(ItemStack is) {
		return armor.get(is.getType()) != null;
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
	 * @param itemStr
	 * @return
	 */
	public static ItemStack getItemStack(String itemStr) {
		if (itemStr == null || itemStr.isEmpty())
			return null;
		itemStr = itemStr.replace(" ", "_");
		itemStr = itemStr.replace(";", ":");
		itemStr = itemStr.toLowerCase();

		String split[] = itemStr.split(":");
		short dataValue = 0;
		if (split.length > 1){
			if (isInt(split[1])){
				int i = Integer.valueOf(split[1]);
				dataValue = (short) i;
				itemStr = split[0];
			}
		}
		Material mat = Material.matchMaterial(itemStr);
		if (DEBUG) System.out.println(mat +"   " + itemStr +"   " + dataValue);
		if (mat != null && mat != Material.AIR) {
			return new ItemStack(mat.getId(), 1, dataValue);
		}
		itemStr = itemStr.toUpperCase();
		for (Material m : Material.values()){
			String itemName = m.name();
			//        		ItemStack item = commonToStack.get(itemName);
			int index = itemName.indexOf(itemStr,0);
			//			if (DEBUG) System.out.println(index +"   " + itemName +"   " + m);
			if (index != -1 && index == 0){
				if (DEBUG) System.out.println(m +"   " + itemStr +"   " + dataValue);
				return new ItemStack(m.getId(), 1, dataValue);
			}
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
				return true;}
		}
		for (ItemStack is : inv.getArmorContents()){
			if (is != null && is.getType() != Material.AIR){
				return true;}
		}
		return false;
	}

	public static void addItemToInventory(Player player, ItemStack itemStack) {
		addItemToInventory(player,itemStack,itemStack.getAmount(),true,false,null);
	}

	public static void addItemToInventory(Player player, ItemStack itemStack, int stockAmount, boolean update) {
		addItemToInventory(player,itemStack,stockAmount,update,false,null);
	}

	public static void addItemsToInventory(Player p, List<ItemStack> items, boolean ignoreCustomHelmet) {
		addItemsToInventory(p,items, ignoreCustomHelmet,null);
	}

	@SuppressWarnings("deprecation")
	public static void addItemsToInventory(Player p, List<ItemStack> items, boolean ignoreCustomHelmet, Color color) {
		if (items == null)
			return;
		for (ItemStack is : items){
			InventoryUtil.addItemToInventory(p, is.clone(), is.getAmount(), false, ignoreCustomHelmet, color);
		}
		try { p.updateInventory(); } catch (Exception e){}
	}

	public static void addItemToInventory(Player player, ItemStack itemStack, int stockAmount,
			boolean update, boolean ignoreCustomHelmet) {
		addItemToInventory(player,itemStack,stockAmount,update,ignoreCustomHelmet,null);
	}

	@SuppressWarnings("deprecation")
	public static void addItemToInventory(Player player, ItemStack itemStack, int stockAmount,
			boolean update, boolean ignoreCustomHelmet, Color color) {
		PlayerInventory inv = player.getInventory();
		Material itemType =itemStack.getType();
		if (armor.containsKey(itemType)){
			addArmorToInventory(inv,itemStack,stockAmount,ignoreCustomHelmet, color);
		} else {
			addItemToInventory(inv, itemStack,stockAmount);
		}
		if (update)
			try { player.updateInventory(); } catch (Exception e){}
	}


	private static void addArmorToInventory(PlayerInventory inv,
			ItemStack itemStack, int stockAmount, boolean ignoreCustomHelmet, Color color) {
		Material itemType =itemStack.getType();
		final boolean isHelmet = armor.get(itemType).type == ArmorType.HELM;
		/// no item: add to armor slot
		/// item better: add old to inventory, new to armor slot
		/// item notbetter: add to inventory
		Armor a = armor.get(itemType);
		final ItemStack oldArmor = getArmorSlot(inv,a.type);
		boolean empty = (oldArmor == null || oldArmor.getType() == Material.AIR);
		boolean better = empty ? true : armorSlotBetter(armor.get(oldArmor.getType()),a);

		if (color != null && a.level == ArmorLevel.LEATHER){
			handler.setItemColor(itemStack,color);
		}
		if (empty || better){
			switch (armor.get(itemType).type){
			case HELM:
				if (empty || (better && !ignoreCustomHelmet))
					inv.setHelmet(itemStack);
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
	}

	public static int first(Inventory inv, ItemStack is1) {
		if (is1 == null) {
			return -1;
		}
		ItemStack[] inventory = inv.getContents();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack is2 = inventory[i];
			if (is2 == null) continue;
			if (is1.getTypeId() == is2.getTypeId() && is1.getDurability() == is2.getDurability()) {
				return i;
			}
		}
		return -1;
	}

	public static HashMap<Integer, ItemStack> removeItems(Inventory inv, List<ItemStack> items) {
		return removeItem(inv, items.toArray(new ItemStack[items.size()]));
	}

	public static HashMap<Integer, ItemStack> removeItems(PlayerInventory inv, ItemStack... items) {
		HashMap<Integer,ItemStack> leftover = removeItem(inv,items);
		if (leftover.isEmpty())
			return leftover;
		for (ItemStack is1: items){
			ItemStack is2 = inv.getBoots();
			if (is2 != null && is1.getTypeId() == is2.getTypeId() && is1.getDurability() == is2.getDurability()){
				inv.setBoots(null);
				continue;
			}
			is2 = inv.getLeggings();
			if (is2 != null && is1.getTypeId() == is2.getTypeId() && is1.getDurability() == is2.getDurability()){
				inv.setLeggings(null);
				continue;
			}
			is2 = inv.getChestplate();
			if (is2 != null && is1.getTypeId() == is2.getTypeId() && is1.getDurability() == is2.getDurability()){
				inv.setChestplate(null);
				continue;
			}
			is2 = inv.getHelmet();
			if (is2 != null && is1.getTypeId() == is2.getTypeId() && is1.getDurability() == is2.getDurability()){
				inv.setHelmet(null);
				continue;
			}
		}
		/// TODO technically this is not correct as removing the armor slots should also decrease the leftover
		return leftover;
	}

	/**
	 * This is nearly a direct copy of the removeItem from CraftBukkit
	 * The difference is my ItemStack == ItemStack comparison (found in first())
	 * there I change it to go by itemid and datavalue
	 * as opposed to itemid and quantity
	 * @param inv
	 * @param items
	 * @return
	 */
	public static HashMap<Integer, ItemStack> removeItem(Inventory inv, ItemStack... items) {
		HashMap<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			int toDelete = item.getAmount();

			while (true) {
				//            	System.out.println("inv= " + inv + "   " + items.length   + "    item=" + item);
				int first = first(inv, item);
				//            	System.out.println("first= " + first);

				// Drat! we don't have this type in the inventory
				if (first == -1) {
					item.setAmount(toDelete);
					leftover.put(i, item);
					break;
				} else {
					ItemStack itemStack = inv.getItem(first);
					int amount = itemStack.getAmount();

					if (amount <= toDelete) {
						toDelete -= amount;
						// clear the slot, all used up
						inv.setItem(first, null);
					} else {
						// split the stack and store
						itemStack.setAmount(amount - toDelete);
						inv.setItem(first, itemStack);
						toDelete = 0;
					}
				}

				// Bail when done
				if (toDelete <= 0) {
					break;
				}
			}
		}
		return leftover;
	}
	private static boolean armorSlotBetter(Armor oldArmor, Armor newArmor) {
		if (oldArmor == null || newArmor == null) /// technically we could throw an exception.. but nah
			return false;
		return oldArmor.level.ordinal() < newArmor.level.ordinal();
	}

	private static ItemStack getArmorSlot(PlayerInventory inv, ArmorType armorType) {
		switch (armorType){
		case HELM: return inv.getHelmet();
		case CHEST: return inv.getChestplate();
		case LEGGINGS: return inv.getLeggings();
		case BOOTS:return inv.getBoots();
		}
		return null;
	}

	///Adds item to inventory
	public static void addItemToInventory(Inventory inv, ItemStack is, int left){
		if (is == null || is.getType() == Material.AIR)
			return;
		if (Defaults.ITEMS_IGNORE_STACKSIZE){
			inv.addItem(is);
			return;
		}
		int maxStackSize = is.getType().getMaxStackSize();
		ItemStack is2 = is.clone(); /// we are modifying the amounts, so lets clone
		if(left <= maxStackSize){
			is2.setAmount(left);
			inv.addItem(is2);
			return;
		}
		if(maxStackSize != 64){
			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
			for (int i = 0; i < Math.ceil((double)left / maxStackSize); i++) {
				if (left < maxStackSize) {
					is2.setAmount(left);
					items.add(is2);
					return;
				}else{
					is2.setAmount(maxStackSize);
					items.add(is2);
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
		try{
			p.closeInventory();
		}catch(Exception closeInventoryError){
			/// This almost always throws an NPE, but does its job so ignore
		}
	}

	@SuppressWarnings("deprecation")
	public static void clearInventory(Player p) {
		if(Defaults.DEBUG_STORAGE) Log.info("Clearing inventory of " + p.getName() +" o=" +
				p.isOnline() +", d="+ p.isDead() +"   inv=" + p.getInventory());
		try{
			PlayerInventory inv = p.getInventory();
			closeInventory(p);
			if (inv != null){
				inv.clear();
				inv.setArmorContents(null);
				inv.setItemInHand(null);
			}
			p.updateInventory();
		} catch(Exception ee){
			ee.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void clearInventory(Player p, boolean skipHead) {
		if (!skipHead){
			clearInventory(p);
			return;
		}
		if(Defaults.DEBUG_STORAGE) Log.info("Clearing inventory of " + p.getName());
		try{
			PlayerInventory inv = p.getInventory();
			closeInventory(p);
			if (inv != null){
				inv.clear();
				inv.setBoots(null);
				inv.setChestplate(null);
				inv.setLeggings(null);
				inv.setItemInHand(null);
			}
			p.updateInventory();
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
		try {
			return parseItem(str) != null;
		} catch (Exception e) {
			return false;
		}
	}

	//Netherfoam start
	private static final Pattern LORE_PATTERN = Pattern.compile("lore= ?\"(.*)\""); //The pattern for matching lore
	private static final Pattern COLOR_PATTERN = Pattern.compile("color= ?([0-9]+),([0-9]+),([0-9]+)"); //The pattern for matching lore

	public static ItemStack parseItem(String str) throws Exception{
		/// items in yaml get stored like this {leather_chest=fireprot:5 1}
		/// so need to remove the {} and the first '='
		if (str.contains("{"))
			str = str.replaceFirst("=", " ");
		str = str.replaceAll("[}{]", "");

		if (DEBUG) System.out.println("item=" + str);

		/// Parse Lore (thanks to Netherfoam)
		List<String> lore = parseLore(str);
		if(lore != null){ //We have lore, so strip it.
			str = LORE_PATTERN.matcher(str).replaceFirst("");}
		Color c = parseColor(str);
		if (c != null){ /// we have color, so strip it
			str = COLOR_PATTERN.matcher(str).replaceFirst("");}

		ItemStack is =null;
		try{
			String split[] = str.split(" ");
			is = InventoryUtil.getItemStack(split[0].trim());
			if (is == null)
				return null;
			int amt;
			if (split.length > 1){
				try {
					amt = Integer.valueOf(split[split.length-1]);
				} catch (Exception e){
					amt = 1;
				}
			} else {
				amt = 1;
			}
			is.setAmount(amt);
			if (lore != null && !lore.isEmpty())
				handler.setLore(is,lore);
			if (c!=null)
				handler.setItemColor(is, c);

			for (int i = 1; i < split.length-1;i++){
				EnchantmentWithLevel ewl = getEnchantment(split[i].trim());
				if (ewl == null){
					throw new IllegalArgumentException(" enchantment " + split[i].trim() +" does not exist");
				}
				try {
					is.addUnsafeEnchantment(ewl.e, ewl.lvl);
				} catch (IllegalArgumentException iae){
					Log.warn(ewl+" can not be applied to the item " + str);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			Log.err(e.getMessage());
			throw new Exception("[BA error] Coudln't parse item="+str);
		}
		return is;
	}

	public static Color parseColor(String str){
		Matcher m = COLOR_PATTERN.matcher(str);
		if (!m.find())
			return null;
		return new Color(Integer.valueOf(m.group(1)),Integer.valueOf(m.group(2)),Integer.valueOf(m.group(3)));
	}

	//Netherfoam start
	public static LinkedList<String> parseLore(String str){
		try{
			Matcher matcher = LORE_PATTERN.matcher(str);
			if(matcher.find()){
				int start = matcher.start(); //This only takes the first match
				int end = matcher.end();

				//Remove the "Lore: " part
				//Remove the quotes
				//Possible issue: If you want quotes in your lore...?
				String part = str.substring(start, end).replaceFirst("lore[:=] ?", "").replaceAll("\"", ""); //Strip Lore: and quotes.
				//Replace color codes
				part = ChatColor.translateAlternateColorCodes('&', matcher.group(1));
				//Now we can split it.
				String[] lines = part.split("[;\\n]");
				//DEBUG
				if(DEBUG) System.out.println(Arrays.toString(lines));
				//Create a new list
				LinkedList<String> lore = new LinkedList<String>();
				//Add all the sections to the list
				for(String s : lines) lore.add(s);
				//Success!
				return lore;
			}
		}
		catch(Exception e){
			e.printStackTrace(); //Damn.
		}
		return null;
	}
	//Netherfoam end

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
		if (!Defaults.ITEMS_UNSAFE_ENCHANTMENTS &&
				lvl > e.getMaxLevel()){lvl = e.getMaxLevel();}
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
			is.addUnsafeEnchantment(e, enchantments.get(e));
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
		if (is.getDurability() > 0){
			sb.append(is.getType().toString() +":"+is.getDurability()+" ");
		} else {
			sb.append(is.getType().toString() +" ");
		}

		Map<Enchantment,Integer> encs = is.getEnchantments();
		for (Enchantment enc : encs.keySet()){
			sb.append(enc.getName() + ":" + encs.get(enc)+" ");
		}
		sb.append(is.getAmount());
		return sb.toString();
	}

	public static boolean isColorable(ItemStack item){
		Armor a = armor.get(item.getType());
		return a != null && a.level == ArmorLevel.LEATHER;
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
			Integer i2 = arg1.getTypeId();
			if (i== Material.AIR.getId() && i2 == Material.AIR.getId()) return 0;
			if (i == Material.AIR.getId()) return 1;
			if (i2 == Material.AIR.getId()) return -1;

			int c = i.compareTo(i2);
			if (c!= 0)
				return c;
			Short s= arg0.getDurability();
			c = s.compareTo(arg1.getDurability());
			if (c!= 0)
				return c;
			i = arg0.getAmount();
			c = i.compareTo(arg1.getAmount());
			if (c!= 0)
				return c;
			Map<Enchantment, Integer> e1 = arg0.getEnchantments();
			Map<Enchantment, Integer> e2 = arg0.getEnchantments();
			i = e1.size();
			c = i.compareTo(e2.size());
			if (c!=0)
				return c;
			for (Enchantment e: e1.keySet()){
				if (!e2.containsKey(e))
					return -1;
				i = e1.get(e);
				i2 = e2.get(e);
				c = i.compareTo(i2);
				if (c != 0)
					return c;
			}
			return 0;
		}
	}

	public static boolean sameItems(List<ItemStack> items, PlayerInventory inv, boolean woolTeams) {
		ItemStack[] contents =inv.getContents();
		ItemStack[] armor = inv.getArmorContents();
		/// This is a basic check to make sure we have the same number of items, and same total durability
		/// Even with the 3 loops b/c there is no creation or sorting this is orders of magnitude faster
		/// and takes almost no time.
		int nitems1 =0, nitems2=0;
		int dura1=0, dura2=0;

		for (ItemStack is: items){
			if (is == null || is.getType() == Material.AIR)
				continue;
			nitems1 += is.getAmount();
			dura1 += is.getDurability()*is.getAmount();
		}
		for (ItemStack is: contents){
			if (is == null || is.getType() == Material.AIR)
				continue;
			nitems2 += is.getAmount();
			dura2 += is.getDurability()*is.getAmount();
		}
		if (!woolTeams){
			for (ItemStack is: armor){
				if (is == null || is.getType() == Material.AIR)
					continue;
				nitems2 += is.getAmount();
				dura2 += is.getDurability()*is.getAmount();
			}
		} else {
			ItemStack is = inv.getBoots();
			if (is != null && is.getType() != Material.AIR){
				nitems2 += is.getAmount();
				dura2 += is.getDurability()*is.getAmount();
			}
			is = inv.getLeggings();
			if (is != null && is.getType() != Material.AIR){
				nitems2 += is.getAmount();
				dura2 += is.getDurability()*is.getAmount();
			}
			is = inv.getChestplate();
			if (is != null && is.getType() != Material.AIR){
				nitems2 += is.getAmount();
				dura2 += is.getDurability()*is.getAmount();
			}
		}
		//				System.out.println("nitems1  " + nitems1 +":" + nitems2+"      " + dura1 +"  : " + dura2);
		if (nitems1 != nitems2 || dura1 != dura2)
			return false;

		/// Now that the basic check is over, the more intensive one starts
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
			//			System.out.println(" iss   " + is.getAmount() +"   " + is.getMaxStackSize() +"    " + is);
			if (is.getAmount() > is.getMaxStackSize()){
				is = is.clone();
				while (is.getAmount() > is.getMaxStackSize()){
					is.setAmount(is.getAmount() - is.getMaxStackSize());
					ItemStack is2 = new ItemStack(is);
					is2.setAmount(is2.getMaxStackSize());
					stacks.add(is2);
				}
			}
			stacks.add(is);
		}
		items = stacks;
		Collections.sort(items, new ItemComparator());
		Collections.sort(pitems, new ItemComparator());
		int idx = 0;
		ItemStack is1, is2;

		while (idx< items.size() && idx<pitems.size()){
			is1 = items.get(idx);
			is2 = pitems.get(idx);
			//						System.out.println(idx  +" : " + is1 +"  " + is2);
			if ((is1==null || is1.getType() == Material.AIR) && (is2 == null || is2.getType() == Material.AIR))
				return true;
			if ((is1==null || is1.getType() == Material.AIR) || (is2 == null || is2.getType() == Material.AIR))
				return false;
			/// Alright, now that we dont have to worry about null or air
			if (!is1.equals(is2))
				return false;
			idx++;
		}
		/// Arrays are similar up until the smallest array
		/// If any array has more elements that are not null, then they are not equal
		for (int i=idx;i<items.size();i++){
			is1 = items.get(i);
			if (is1 != null && is1.getType() != Material.AIR)
				return false;
		}
		for (int i=idx;i<pitems.size();i++){
			is2 = pitems.get(i);
			if (is2 != null && is2.getType() != Material.AIR)
				return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static void addToInventory(Player p, PInv pinv) {
		try{
			PlayerInventory inv = p.getPlayer().getInventory();
			for (ArmorType armor : ArmorType.values()){
				final int ord = armor.ordinal();
				if (ord >= pinv.armor.length)
					break;
				final ItemStack newArmor = pinv.armor[ord];
				if (newArmor == null || newArmor.getType()== Material.AIR)
					continue;
				final ItemStack oldArmor = getArmorSlot(inv,armor);
				boolean empty = (oldArmor == null || oldArmor.getType() == Material.AIR);
				if (empty) {
					switch(armor){
					case HELM: inv.setHelmet(newArmor); break;
					case CHEST: inv.setChestplate(newArmor); break;
					case LEGGINGS: inv.setLeggings(newArmor); break;
					case BOOTS: inv.setBoots(newArmor); break;
					}
				} else {
					addItemToInventory(p, newArmor, newArmor.getAmount(),false,false);
				}
			}
			inv.setContents(pinv.contents);
		} catch(Exception e){
			e.printStackTrace();
		}
		try {p.getPlayer().updateInventory(); } catch (Exception e){} /// Yes this can throw errors
	}



}
