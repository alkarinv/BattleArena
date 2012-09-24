package mc.alk.arena.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.util.EffectUtil.EffectWithArgs;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.ArmorLevel;
import mc.alk.arena.util.Util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class TransitionOptions {
	public static enum TransitionOption{
		TELEPORTWAITROOM("teleportWaitRoom",false),TELEPORTIN ("teleportIn",false), TELEPORTOUT("teleportOut",false),
		TELEPORTBACK("teleportBack",false),
		RESPAWN ("respawn",false), RANDOMRESPAWN ("randomRespawn",false),
		CLEARINVENTORY ("clearInventory",false), NEEDARMOR ("needArmor",false),
		CLEARINVENTORYONFIRSTENTER ("clearInventoryOnFirstEnter",false), 
		NEEDITEMS ("needItems",false), GIVEITEMS("giveItems",false), GIVECLASS("giveClass",false),
		HEALTH("health",true), HUNGER("hunger",true),
		MONEY("money",true), EXPERIENCE("experience",true),
		PVPON("pvpOn",false), PVPOFF("pvpOff",false),INVINCIBLE("invincible",false),
		BLOCKBREAKOFF("blockBreakOff",false), BLOCKBREAKON("blockBreakOn",false),
		BLOCKPLACEOFF("blockPlaceOff",false), BLOCKPLACEON("blockPlaceOn",false),
		DISGUISEALLAS("disguiseAllAs",true), UNDISGUISE("undisguise",false),
		ENCHANTS("enchants",false), DEENCHANT("deEnchant",false),
		STOREEXPERIENCE("storeExperience",false), RESTOREEXPERIENCE("restoreExperience",false),
		STOREGAMEMODE("storeGamemode",false), RESTOREGAMEMODE("restoreGamemode",false),
		STOREITEMS("storeItems",false), RESTOREITEMS("restoreItems",false),
		STORE("store",false), RESTORE("restore",false),
		WGCLEARREGION("wgClearRegion",false), WGNOLEAVE("wgNoLeave",false),
		WOOLTEAMS("woolTeams",false),
		SAMEWORLD("sameWorld",false), WITHINDISTANCE("withinDistance",true)
		;
		String name;
		boolean hasValue = false;
		TransitionOption(String name,Boolean hasValue){this.name= name;this.hasValue = hasValue;}
		public String toString(){return name;}
		public boolean hasValue(){return hasValue;}
	};
	Set<TransitionOption> options = null;
	List<ItemStack> items = null;
	Map<Integer,ArenaClass> classes = null;
	List<EffectWithArgs> effects = null;
	Double money =null; 
	Integer exp = null;
	Integer health = null;
	Integer hunger = null;
	String disguiseAllAs = null;
	Integer withinDistance = null;
	public TransitionOptions() {
	}
	public TransitionOptions(TransitionOptions o) {
		if (o == null)
			return;
		if (o.options != null) this.options = new HashSet<TransitionOption>(o.options);
		this.items = o.items;
		this.classes = o.classes;
		this.effects = o.effects;
		this.money = o.money;
		this.exp = o.exp;
		this.health= o.health;
		this.hunger = o.hunger;
		this.disguiseAllAs = o.disguiseAllAs;
		
	}
	public void setOptions(Set<String> options) {
		this.options =new HashSet<TransitionOption>();
		for (String s: options){
			this.options.add(TransitionOption.valueOf(s));
		}
	}
	public void setMatchOptions(Set<TransitionOption> options) {
		this.options =new HashSet<TransitionOption>(options);
	}

	public void setItems(List<ItemStack> items) {this.items = items;}
	public List<ItemStack> getItems() {return this.items;}

	public void setEffects(List<EffectWithArgs> effectList) {this.effects = effectList;}
	public List<EffectWithArgs> getEffects(){return effects;}
	private boolean hasEffects() {return effects != null;}
	public boolean clearInventory() {return options.contains(TransitionOption.CLEARINVENTORY);}
	public boolean needsArmor() {return options.contains(TransitionOption.NEEDARMOR);}
	public boolean needsItems() {return items != null && options.contains(TransitionOption.NEEDITEMS);}
	public boolean hasItems() {return (items != null && (options.contains(TransitionOption.NEEDITEMS) || options.contains(TransitionOption.GIVEITEMS)) );}
	public boolean shouldTeleportWaitRoom() {return options.contains(TransitionOption.TELEPORTWAITROOM);}
	public boolean shouldTeleportIn() {return options.contains(TransitionOption.TELEPORTIN);}
	public boolean shouldTeleportOut() {
		return options.contains(TransitionOption.TELEPORTOUT) || options.contains(TransitionOption.TELEPORTBACK);
	}

	public boolean blockBreakOff() {return options.contains(TransitionOption.BLOCKBREAKOFF);}
	public boolean blockPlaceOff() {return options.contains(TransitionOption.BLOCKPLACEOFF);}

	public Integer getHealth() {return health;}
	public Integer getHunger() {return hunger;}
	
	public void setMoney(double money) {this.money = money;}
	public Double getMoney(){return money;}
	public boolean hasMoney(){return money != null && money > 0;}

	public void setGiveExperience(int exp) {this.exp = exp;}
	public Integer getExperience(){return exp;}
	public boolean hasExperience(){return exp!= null && exp > 0;}

	public void setHealth(Integer h) {health = h;}
	public void setHunger(Integer h) {hunger = h;}

	public void setDisguiseAllAs(String str) {this.disguiseAllAs = str;}
	public String getDisguiseAllAs() {return disguiseAllAs;}
	public Boolean undisguise() {return options.contains(TransitionOption.UNDISGUISE);}
	
	public boolean playerReady(ArenaPlayer p) {
		if (p==null || !p.isOnline() || p.isDead())
			return false;
		if (needsItems()){
			Inventory inv = p.getInventory();
	        for (ItemStack is : items){
	        	if (InventoryUtil.getItemAmountFromInventory(inv, is) < is.getAmount())
	        		return false;
	        }
		}
//		if (clearInventory()){
////			System.out.println(" needs clearInventory  " + p.getInventory());
//			if (InventoryUtil.hasAnyItem(p))
//				return false;
//		}
		if (needsArmor()){
//			System.out.println(" needs armor  hasArmor=" + InventoryUtil.hasArmor(p));
			if (!InventoryUtil.hasArmor(p.getPlayer()))
				return false;
		}
//		System.out.println(" my options are " + options);
		return true;
	}

	public String getNotReadyMsg(String header) {
		StringBuilder sb = new StringBuilder();
		boolean hasSomething = false;
		if (header != null){
			sb.append(header);
			hasSomething = true;
		}
		if (needsItems()){
			hasSomething = true;
	        for (ItemStack is : items){
	        	sb.append("&5 - &6"+ is.getAmount() +" " + is.getData());
	        }
		}
		if (clearInventory()){
			hasSomething = true;
			sb.append("&5 - &6Clear Inventory");
		}
		if (needsArmor()){
			hasSomething = true;
			sb.append("&5 - &6Armor");
		}
		return hasSomething ? sb.toString() : null; 
	}

	public String getNotReadyMsg(ArenaPlayer p, String headerMsg) {
//		System.out.println(" Here in getNot ready msg with " + p.getName());
		StringBuilder sb = new StringBuilder(headerMsg);
		boolean isReady = true;
		if (needsItems()){
			Inventory inv = p.getInventory();
	        for (ItemStack is : items){
	        	int amountInInventory =InventoryUtil.getItemAmountFromInventory(inv, is); 
	        	if (amountInInventory < is.getAmount()){
	        		int needed = amountInInventory - is.getAmount();
	        		sb.append("&5 - &e"+needed +" " + is.getType() + "\n");
	        		isReady = false;
	        	}
	        }
		}
		if (clearInventory()){
			if (InventoryUtil.hasAnyItem(p.getPlayer())){
				sb.append("&5 -&e a &6Clear Inventory\n");
        		isReady = false;
			}
		}
		if (needsArmor()){
			if (!InventoryUtil.hasArmor(p.getPlayer())){
				sb.append("&&5 - &6Armor\n");
        		isReady = false;
			}
		}
//		System.out.println(" Here in getNot ready msg with " + p.getName() + " ------" + sb.toString());
		return isReady? null : sb.toString();
	}

	public String getPrizeMsg(String header) {
		StringBuilder sb = new StringBuilder();
		boolean hasSomething = false;
		if (header != null){
			sb.append(header);
			hasSomething = true;
		}
		if (hasExperience()){
			hasSomething = true;
			sb.append("&5 - &2" + getExperience()+" experience");
		}
		if (hasMoney()){
			hasSomething = true;
			sb.append("&5 - &6" + getMoney()+Defaults.MONEY_STR);
		}
		if (hasItems()){
			hasSomething = true;
			ArmorLevel lvl = InventoryUtil.hasArmorSet(getItems());
			if (lvl != null){
				sb.append("&5 - &a"+ lvl.toString() +" ARMOR");
			}
	        for (ItemStack is : getItems()){
	        	if (lvl != null && InventoryUtil.sameMaterial(lvl,is))
	        		continue;
	        	String enchanted = !is.getEnchantments().isEmpty() ? " &4Enchanted ": "";
	        	sb.append("&5 - &a"+ is.getAmount() +enchanted + is.getType().toString() );
	        }
		}
		if (hasEffects()){
			hasSomething = true;
	        for (EffectWithArgs ewa : getEffects()){
	        	if (ewa != null)
	        		sb.append("&5 - &b"+ ewa.getCommonName()+":"+(ewa.strength+1));
	        }
		}

		return hasSomething? sb.toString() : null;
	}

	public PVPState getPVP() {
		if (options.contains(TransitionOption.PVPON)){
			return PVPState.ON;
		} else if (options.contains(TransitionOption.PVPOFF)){
			return PVPState.OFF;
		} else if (options.contains(TransitionOption.INVINCIBLE)){
			return PVPState.INVINCIBLE;
		}
		return null;
	}

	public boolean respawn() {
		return options.contains(TransitionOption.RESPAWN);
	}
	public boolean randomRespawn() {
		return options.contains(TransitionOption.RANDOMRESPAWN);
	}

	public Boolean deEnchant() {
		return options.contains(TransitionOption.DEENCHANT);
	}

	public boolean storeExperience() {
		return options.contains(TransitionOption.STORE) || options.contains(TransitionOption.STOREEXPERIENCE);
	}
	public boolean restoreExperience() {
		return options.contains(TransitionOption.RESTORE) || options.contains(TransitionOption.RESTOREEXPERIENCE);
	}

	public boolean storeItems() {
		return options.contains(TransitionOption.STORE) || options.contains(TransitionOption.STOREITEMS);
	}
	public boolean restoreItems() {
		return options.contains(TransitionOption.RESTORE) || options.contains(TransitionOption.RESTOREITEMS);
	}

	public boolean woolTeams() {
		return options.contains(TransitionOption.WOOLTEAMS);
	}
	public Set<TransitionOption> getOptions() {
		return options;
	}
	public boolean shouldClearRegion() {
		return options.contains(TransitionOption.WGCLEARREGION);
	}
	public void addOption(TransitionOption option) {
		if (options==null){
			options = new HashSet<TransitionOption>();
		}
		options.add(option);
	}
	public boolean hasOption(TransitionOption op) {
		return options != null && options.contains(op);
	}

	public static String getInfo(MatchParams sq, String name) {
		StringBuilder sb = new StringBuilder();
		MatchTransitions at = sq.getTransitionOptions();
		String required = at.getRequiredString(null);
		String prestart = at.getGiveString(MatchState.ONPRESTART);
		String start = at.getGiveString(MatchState.ONSTART);
		String onspawn = at.getGiveString(MatchState.ONSPAWN);
		String prizes = at.getGiveString(MatchState.WINNER);
		String firstPlacePrizes = at.getGiveString(MatchState.FIRSTPLACE);
		boolean rated = sq.isRated();
		String teamSizes = Util.getStr(sq.getMinTeamSize(),sq.getMaxTeamSize());
		sb.append("&eThis is "+ (rated? "a &4Rated" : "an &aUnrated") +"&e "+name+". " );
		sb.append("&eTeam size=&6" + teamSizes);
		sb.append("\n&eRequirements to Join:");
		sb.append(required==null? "&aNone" : required);
		if (prestart != null || start !=null || onspawn != null){
			sb.append("\n&eYou are given:");
			if (prestart != null) sb.append(prestart);			
			if (start != null) sb.append(start);			
			if (onspawn != null) sb.append(onspawn);			
		}
		sb.append("\n&ePrize for &5winning&e a match:");
		sb.append(prizes==null? "&aNone" : prizes);
		if (firstPlacePrizes != null){			
			sb.append("\n&ePrize for getting &51st &eplace:");
			sb.append(firstPlacePrizes);
		}
		return sb.toString();
	}
	public void setClasses(HashMap<Integer, ArenaClass> classes) {
		this.classes = classes;
		addOption(TransitionOption.GIVECLASS);
	}
	public Map<Integer, ArenaClass> getClasses(){
		return classes;
	}
	public boolean hasClasses(){
		return classes != null;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder("[MatchOptions=");
		sb.append(options +"]");
		return sb.toString();
	}
	public void setWithinDistance(Integer value) {
		withinDistance = value;
	}
	public int getWithinDistance() {
		return withinDistance;
	}
	
}
