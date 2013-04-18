package mc.alk.arena.objects.options;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.MobArenaInterface;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CommandLineString;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.PVPState;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.ArmorLevel;
import mc.alk.arena.util.MinMax;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;


@SuppressWarnings("unchecked")
public class TransitionOptions {

	Map<TransitionOption,Object> options = null;

	public TransitionOptions() {}
	public TransitionOptions(TransitionOptions o) {
		if (o == null)
			return;
		if (o.options != null) this.options = new EnumMap<TransitionOption,Object>(o.options);
	}

	public void addOptions(TransitionOptions optionSet) {
		if (optionSet.options == null)
			return;
		addOptions(optionSet.options);
	}

	public void addOptions(Map<TransitionOption,Object> options) {
		if (this.options==null)
			this.options = new EnumMap<TransitionOption,Object>(options);
		else
			this.options.putAll(options);
	}

	public void setOptions(Set<String> options) {
		this.options =new EnumMap<TransitionOption,Object>(TransitionOption.class);
		for (String s: options){
			this.options.put(TransitionOption.valueOf(s),null);
		}
	}

	public void setOptions(Map<TransitionOption,Object> options) {
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
	public boolean hasItems() {
		return options.containsKey(TransitionOption.NEEDITEMS) ||
				options.containsKey(TransitionOption.GIVEITEMS);}
	public boolean shouldTeleportLobby() {return options.containsKey(TransitionOption.TELEPORTLOBBY);}
	public boolean shouldTeleportWaitRoom() {return options.containsKey(TransitionOption.TELEPORTWAITROOM);}
	public boolean shouldTeleportIn() {return options.containsKey(TransitionOption.TELEPORTIN);}
	public boolean teleportsIn() {return shouldTeleportIn() || shouldTeleportWaitRoom();}

	public boolean shouldTeleportOut() {
		return options.containsKey(TransitionOption.TELEPORTOUT) ||
				options.containsKey(TransitionOption.TELEPORTTO);
	}

	public boolean blockBreakOff() {return options.containsKey(TransitionOption.BLOCKBREAKOFF);}
	public boolean blockPlaceOff() {return options.containsKey(TransitionOption.BLOCKPLACEOFF);}

	public Integer getHealth() {return getInt(TransitionOption.HEALTH);}
	public Integer getHealthP() {return getInt(TransitionOption.HEALTHP);}
	public Integer getHunger() {return getInt(TransitionOption.HUNGER);}
	public Integer getMagic() { return getInt(TransitionOption.MAGIC);}
	public Integer getMagicP() { return getInt(TransitionOption.MAGICP);}
	public Integer getWithinDistance() {return getInt(TransitionOption.WITHINDISTANCE);}
	public GameMode getGameMode() {return getGameMode(TransitionOption.GAMEMODE);}
	public List<CommandLineString> getDoCommands() {
		final Object o = options.get(TransitionOption.DOCOMMANDS);
		return o == null ? null : (List<CommandLineString>) o;
	}

	public Integer getInt(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (Integer) o;
	}

	public Double getDouble(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (Double) o;
	}

	public Float getFloat(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (Float) o;
	}

	public String getString(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (String) o;
	}

	public GameMode getGameMode(TransitionOption option){
		final Object o = options.get(option);
		return o == null ? null : (GameMode) o;
	}

	public Double getMoney(){return getDouble(TransitionOption.MONEY);}
	public boolean hasMoney(){
		Double d = getDouble(TransitionOption.MONEY);
		return d != null && d > 0;
	}
	public Float getFlightSpeed(){return getFloat(TransitionOption.FLIGHTSPEED);}
	public Integer getInvulnerable(){return getInt(TransitionOption.INVULNERABLE);}
	public Integer getExperience(){return getInt(TransitionOption.EXPERIENCE);}
	public boolean hasExperience(){return options.containsKey(TransitionOption.EXPERIENCE);}

	public String getDisguiseAllAs() {return getString(TransitionOption.DISGUISEALLAS);}
	public Boolean undisguise() {return options.containsKey(TransitionOption.UNDISGUISE);}

	public boolean playerReady(ArenaPlayer p, World w) {
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
		/// Inside MobArena?
		if (MobArenaInterface.hasMobArena() && MobArenaInterface.insideMobArena(p)){
			return false;
		}
		if (options.containsKey(TransitionOption.GAMEMODE)){
			GameMode gm = getGameMode();
			if (p.getPlayer().getGameMode() != gm){
				return false;}
		}

		if (options.containsKey(TransitionOption.NOINVENTORY)){
			if (InventoryUtil.hasAnyItem(p.getPlayer()))
				return false;
		}
		if (options.containsKey(TransitionOption.SAMEWORLD) && w!=null){
			if (p.getLocation().getWorld().getUID() != w.getUID())
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
		if (options.containsKey(TransitionOption.GAMEMODE)){
			hasSomething = true;
			GameMode gm = getGameMode();
			sb.append("&5 - &6GameMode="+gm.toString());
		}
		if (needsArmor()){
			hasSomething = true;
			sb.append("&5 - &6Armor");
		}
		if (options.containsKey(TransitionOption.LEVELRANGE)){
			MinMax mm = (MinMax) options.get(TransitionOption.LEVELRANGE);
			sb.append("&a - lvl="+mm.toString());
		}
		return hasSomething ? sb.toString() : null;
	}

	public String getNotReadyMsg(ArenaPlayer p, World w, String headerMsg) {
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
		if (options.containsKey(TransitionOption.GAMEMODE)){
			GameMode gm = getGameMode();
			if (p.getPlayer().getGameMode() != gm){
				sb.append("&5 -&e a &6You need to be in &c"+gm+"&e mode \n");
				isReady = false;
			}
		}
		if (options.containsKey(TransitionOption.NOINVENTORY)){
			if (InventoryUtil.hasAnyItem(p.getPlayer())){
				sb.append("&5 -&e a &6Clear Inventory\n");
				isReady = false;
			}
		}
		if (options.containsKey(TransitionOption.SAMEWORLD) && w!=null){
			if (p.getLocation().getWorld().getUID() != w.getUID()){
				sb.append("&5 -&c Not in same world\n");
				isReady = false;
			}
		}
		/// Inside MobArena?
		if (MobArenaInterface.hasMobArena() && MobArenaInterface.insideMobArena(p)){
			isReady = false;
			sb.append("&5 - &4You are Inside Mob Arena");
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
		if (getGiveItems() != null){
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
		return options.containsKey(TransitionOption.RANDOMRESPAWN) ||
				options.containsKey(TransitionOption.RANDOMSPAWN);
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

	public Object removeOption(TransitionOption op) {
		return options != null ? options.remove(op) : null;
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
		String teamSizes = MinMax.getStr(sq.getMinTeamSize(),sq.getMaxTeamSize());
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

	public Map<Integer, String> getDisguises(){
		Object o = options.get(TransitionOption.GIVEDISGUISE);
		return o == null ? null : (Map<Integer, String>) o;
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

	private Location returnLoc(TransitionOption to){
		final Object o = options.get(to);
		return o == null ? null : (Location) o;
	}

}
