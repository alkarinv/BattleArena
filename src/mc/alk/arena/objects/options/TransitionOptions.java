package mc.alk.arena.objects.options;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.PVPState;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.ArmorLevel;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;


@SuppressWarnings("unchecked")
public class TransitionOptions {
	public static enum TransitionOption{
		TELEPORTWAITROOM("teleportWaitRoom",false),TELEPORTIN ("teleportIn",false), TELEPORTOUT("teleportOut",false),
		TELEPORTTO("teleportTo", true), TELEPORTONARENAEXIT("teleportOnArenaExit",true),
		TELEPORTWINNER("teleportWinner",true), TELEPORTLOSER("teleportLoser", true),
		TELEPORTBACK("teleportBack",false),
		NOTELEPORT("noTeleport", false), NOWORLDCHANGE("noWorldChange",false),
		RESPAWN ("respawn",false), RANDOMRESPAWN ("randomRespawn",false), RESPAWNWITHCLASS("respawnWithClass", false),
		CLEARINVENTORY ("clearInventory",false), NEEDARMOR ("needArmor",false), NOINVENTORY("noInventory",false),
		CLEARINVENTORYONFIRSTENTER ("clearInventoryOnFirstEnter",false),
		NEEDITEMS ("needItems",false), GIVEITEMS("giveItems",false), GIVECLASS("giveClass",false),
		LEVELRANGE("levelRange",true),
		HEALTH("health",true), HUNGER("hunger",true),
		MONEY("money",true), EXPERIENCE("experience",true),
		PVPON("pvpOn",false), PVPOFF("pvpOff",false),INVINCIBLE("invincible",false),
		BLOCKBREAKOFF("blockBreakOff",false), BLOCKBREAKON("blockBreakOn",false),
		BLOCKPLACEOFF("blockPlaceOff",false), BLOCKPLACEON("blockPlaceOn",false),
		DROPITEMOFF("dropItemOff",false),
		DISGUISEALLAS("disguiseAllAs",true), UNDISGUISE("undisguise",false),
		ENCHANTS("enchants",false), DEENCHANT("deEnchant",false),
		STOREALL("storeAll",false), RESTOREALL("restoreAll", false),
		STOREEXPERIENCE("storeExperience",false), RESTOREEXPERIENCE("restoreExperience",false),
		STOREGAMEMODE("storeGamemode",false), RESTOREGAMEMODE("restoreGamemode",false),
		STOREITEMS("storeItems",false), RESTOREITEMS("restoreItems",false),
		STOREHEROCLASS("storeHeroClass",false), RESTOREHEROCLASS("restoreHeroClass",false),
		WGCLEARREGION("wgClearRegion",false),  WGRESETREGION("wgResetRegion",false),
		WGNOLEAVE("wgNoLeave",false), WGNOENTER("wgNoEnter", false),
		WOOLTEAMS("woolTeams",false), ALWAYSWOOLTEAMS("alwaysWoolTeams", false),
		ALWAYSTEAMNAMES("alwaysTeamNames", false),
		ADDPERMS("addPerms", false), REMOVEPERMS("removePerms", false),
		SAMEWORLD("sameWorld",false), WITHINDISTANCE("withinDistance",true),
		MAGIC("magic",true)
		;
		String name;
		boolean hasValue = false;
		TransitionOption(String name,Boolean hasValue){this.name= name;this.hasValue = hasValue;}
		@Override
		public String toString(){return name;}
		public boolean hasValue(){return hasValue;}
	};

	Map<TransitionOption,Object> options = null;
//	List<PotionEffect> effects = null;
//	Double money =null;
//	String disguiseAllAs = null;
//	Location teleportWinner = null;

	public TransitionOptions() {}
	public TransitionOptions(TransitionOptions o) {
		if (o == null)
			return;
		if (o.options != null) this.options = new EnumMap<TransitionOption,Object>(o.options);
	}

	public void setOptions(Set<String> options) {
		this.options =new EnumMap<TransitionOption,Object>(TransitionOption.class);
		for (String s: options){
			this.options.put(TransitionOption.valueOf(s),null);
		}
	}

	public void setMatchOptions(Map<TransitionOption,Object> options) {
		this.options =new EnumMap<TransitionOption,Object>(options);
	}

	public List<ItemStack> getGiveItems() {
		Object o = options.get(TransitionOption.GIVEITEMS);
		return o == null ? null : (List<ItemStack>) o;
	}

	public List<ItemStack> getNeedItems() {
		Object o = options.get(TransitionOption.NEEDITEMS);
		return o == null ? null : (List<ItemStack>) o;
	}

	public List<PotionEffect> getEffects(){
		Object o = options.get(TransitionOption.ENCHANTS);
		return o == null ? null : (List<PotionEffect>) o;
	}
	private boolean hasEffects() {return getEffects() != null;}
	public boolean clearInventory() {return options.containsKey(TransitionOption.CLEARINVENTORY);}
	public boolean needsArmor() {return options.containsKey(TransitionOption.NEEDARMOR);}
	public boolean needsItems() {return options.containsKey(TransitionOption.NEEDITEMS);}
	public boolean hasItems() {return options.containsKey(TransitionOption.NEEDITEMS) || options.containsKey(TransitionOption.GIVEITEMS);}
	public boolean shouldTeleportWaitRoom() {return options.containsKey(TransitionOption.TELEPORTWAITROOM);}
	public boolean shouldTeleportIn() {return options.containsKey(TransitionOption.TELEPORTIN);}
	public boolean teleportsIn() {return shouldTeleportIn() || shouldTeleportWaitRoom();}

	public boolean shouldTeleportOut() {
		return options.containsKey(TransitionOption.TELEPORTOUT) || options.containsKey(TransitionOption.TELEPORTBACK) ||
				options.containsKey(TransitionOption.TELEPORTTO);
	}

	public boolean blockBreakOff() {return options.containsKey(TransitionOption.BLOCKBREAKOFF);}
	public boolean blockPlaceOff() {return options.containsKey(TransitionOption.BLOCKPLACEOFF);}

	public Integer getHealth() {return getInt(TransitionOption.HEALTH);}
	public Integer getHunger() {return getInt(TransitionOption.HUNGER);}
	public Integer getMagic() { return getInt(TransitionOption.MAGIC);}
	public Integer getWithinDistance() {return getInt(TransitionOption.WITHINDISTANCE);}

	public Integer getInt(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (Integer) o;
	}

	public String getString(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (String) o;
	}

	public Double getDouble(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (Double) o;
	}

	public Double getMoney(){return getDouble(TransitionOption.MONEY);}
	public boolean hasMoney(){
		Double d = getDouble(TransitionOption.MONEY);
		return d != null && d > 0;
	}

	public Integer getExperience(){return getInt(TransitionOption.EXPERIENCE);}
	public boolean hasExperience(){return options.containsKey(TransitionOption.EXPERIENCE);}

	public String getDisguiseAllAs() {return getString(TransitionOption.DISGUISEALLAS);}
	public Boolean undisguise() {return options.containsKey(TransitionOption.UNDISGUISE);}

	public boolean playerReady(ArenaPlayer p) {
		if (p==null || !p.isOnline() || p.isDead())
			return false;
		if (needsItems()){
			List<ItemStack> items = getNeedItems();
			Inventory inv = p.getInventory();
			for (ItemStack is : items){
				if (InventoryUtil.getItemAmountFromInventory(inv, is) < is.getAmount())
					return false;
			}
		}
		if (options.containsKey(TransitionOption.NOINVENTORY)){
			if (InventoryUtil.hasAnyItem(p.getPlayer()))
				return false;
		}
		if (needsArmor()){
			if (!InventoryUtil.hasArmor(p.getPlayer()))
				return false;
		}
		if (options.containsKey(TransitionOption.LEVELRANGE)){
			MinMax mm = (MinMax) options.get(TransitionOption.LEVELRANGE);
			if (!mm.contains(p.getLevel()))
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
			List<ItemStack> items = getNeedItems();
			hasSomething = true;
			for (ItemStack is : items){
				sb.append("&5 - &6"+ is.getAmount() +" " + is.getData());
			}
		}
		if (options.containsKey(TransitionOption.NOINVENTORY)){
			hasSomething = true;
			sb.append("&5 - &6Clear Inventory");
		}
		if (needsArmor()){
			hasSomething = true;
			sb.append("&5 - &6Armor");
		}
		if (options.containsKey(TransitionOption.LEVELRANGE)){
			MinMax mm = (MinMax) options.get(TransitionOption.LEVELRANGE);
			sb.append("&a - lvl="+mm.toString());
		}

		sb.append("&5 - &6Armor");
		return hasSomething ? sb.toString() : null;
	}

	public String getNotReadyMsg(ArenaPlayer p, String headerMsg) {
		//		System.out.println(" Here in getNot ready msg with " + p.getName());
		StringBuilder sb = new StringBuilder(headerMsg);
		boolean isReady = true;
		if (needsItems()){
			Inventory inv = p.getInventory();
			List<ItemStack> items = getNeedItems();
			for (ItemStack is : items){
				int amountInInventory =InventoryUtil.getItemAmountFromInventory(inv, is);
				if (amountInInventory < is.getAmount()){
					int needed = amountInInventory - is.getAmount();
					sb.append("&5 - &e"+needed +" " + is.getType() + "\n");
					isReady = false;
				}
			}
		}
		if (options.containsKey(TransitionOption.NOINVENTORY)){
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
		if (options.containsKey(TransitionOption.LEVELRANGE)){
			MinMax mm = (MinMax) options.get(TransitionOption.LEVELRANGE);
			if (!mm.contains(p.getLevel())){
				sb.append("&a - lvl="+mm.toString());
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
			sb.append("&5 - &6" + getMoney()+" " + Defaults.MONEY_STR);
		}
		if (hasItems()){
			hasSomething = true;
			List<ItemStack> items = getGiveItems();
			ArmorLevel lvl = InventoryUtil.hasArmorSet(items);
			if (lvl != null){
				sb.append("&5 - &a"+ lvl.toString() +" ARMOR");
			}
			for (ItemStack is : items){
				if (lvl != null && InventoryUtil.sameMaterial(lvl,is))
					continue;
				String enchanted = !is.getEnchantments().isEmpty() ? " &4Enchanted ": "";
				sb.append("&5 - &a"+ is.getAmount() +enchanted + is.getType().toString() );
			}
		}
		if (hasEffects()){
			hasSomething = true;
			for (PotionEffect ewa : getEffects()){
				if (ewa != null)
					sb.append("&5 - &b"+ EffectUtil.getCommonName(ewa));
			}
		}

		return hasSomething? sb.toString() : null;
	}

	public PVPState getPVP() {
		if (options.containsKey(TransitionOption.PVPON)){
			return PVPState.ON;
		} else if (options.containsKey(TransitionOption.PVPOFF)){
			return PVPState.OFF;
		} else if (options.containsKey(TransitionOption.INVINCIBLE)){
			return PVPState.INVINCIBLE;
		}
		return null;
	}

	public boolean respawn() {
		return options.containsKey(TransitionOption.RESPAWN);
	}
	public boolean randomRespawn() {
		return options.containsKey(TransitionOption.RANDOMRESPAWN);
	}

	public Boolean deEnchant() {
		return options.containsKey(TransitionOption.DEENCHANT);
	}

	public boolean woolTeams() {
		return options.containsKey(TransitionOption.WOOLTEAMS) || options.containsKey(TransitionOption.ALWAYSWOOLTEAMS);
	}
	public Map<TransitionOption,Object> getOptions() {
		return options;
	}
	public boolean shouldClearRegion() {
		return options.containsKey(TransitionOption.WGCLEARREGION);
	}

	public void addOption(TransitionOption option) throws InvalidOptionException {
		if (option.hasValue()) throw new InvalidOptionException("TransitionOption needs a value!");
		addOption(option, null);
	}

	public void addOption(TransitionOption option, Object value) throws InvalidOptionException {
		if (option.hasValue() && value==null) throw new InvalidOptionException("TransitionOption needs a value!");
		if (options==null){
			options = new EnumMap<TransitionOption,Object>(TransitionOption.class);}
		options.put(option,value);
	}

	public boolean hasOption(TransitionOption op) {
		return options != null && options.containsKey(op);
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
		String participantPrizes = at.getGiveString(MatchState.PARTICIPANTS);
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
		if (participantPrizes != null){
			sb.append("\n&ePrize for &6participation:&e "+participantPrizes);}
		sb.append(prizes==null? "&aNone" : prizes);
		if (firstPlacePrizes != null){
			sb.append("\n&ePrize for getting &b1st &eplace:");
			sb.append(firstPlacePrizes);
		}

		return sb.toString();
	}

	public Map<Integer, ArenaClass> getClasses(){
		Object o = options.get(TransitionOption.GIVECLASS);
		return o == null ? null : (Map<Integer, ArenaClass>) o;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("[MatchOptions=");
		boolean first = true;
		for (TransitionOption op: options.keySet()){
			if (!first) sb.append(", " );
			sb.append(op.toString());
			Object value = options.get(op);
			if (value != null){
				sb.append(":true");
			}
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}

	public List<String> getAddPerms() {
		final Object o = options.get(TransitionOption.ADDPERMS);
		return o == null ? null : (List<String>) o;
	}

	/// TODO, not sure this will work properly, I really want to remove those perms that were added in another section!!
	public List<String> getRemovePerms() {
		final Object o = options.get(TransitionOption.ADDPERMS);
		return o == null ? null : (List<String>) o;
	}

	public Location getTeleportToLoc() {return returnLoc(TransitionOption.TELEPORTTO);}
	public Location getTeleportWinnerLoc() {return returnLoc(TransitionOption.TELEPORTWINNER);}
	public Location getTeleportLoserLoc() {return returnLoc(TransitionOption.TELEPORTLOSER);}
	public Location getTeleportOnArenaExit() {return returnLoc(TransitionOption.TELEPORTONARENAEXIT);}

	private Location returnLoc(TransitionOption to){
		final Object o = options.get(to);
		return o == null ? null : (Location) o;
	}
}
