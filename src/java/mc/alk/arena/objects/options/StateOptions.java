package mc.alk.arena.objects.options;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.plugins.MobArenaInterface;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.CommandLineString;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.PVPState;
import mc.alk.arena.objects.StateGraph;
import mc.alk.arena.objects.StateOption;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.ArmorLevel;
import mc.alk.arena.util.MinMax;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static mc.alk.arena.objects.options.TransitionOption.*;


@SuppressWarnings("unchecked")
public class StateOptions {

    Map<StateOption,Object> options;

    public StateOptions() {}
    public StateOptions(StateOptions o) {
        if (o == null)
            return;
        if (o.options != null) this.options = new HashMap<StateOption,Object>(o.options);
    }

    public void addOptions(StateOptions optionSet) {
        if (optionSet.options == null)
            return;
        addOptions(optionSet.options);
    }

    public void addOptions(Map<StateOption,Object> options) {
        if (this.options==null)
            this.options = new HashMap<StateOption,Object>(options);
        else
            this.options.putAll(options);
    }

    public void setOptions(Set<String> options) {
        this.options =new HashMap<StateOption,Object>();
        for (String s: options){
            StateOption so = valueOf(s);
            if (so==null)
                continue;
            this.options.put(so,null);
        }
    }

    public void setOptions(Map<StateOption,Object> options) {
        this.options =new HashMap<StateOption,Object>(options);
    }

    public List<ItemStack> getGiveItems() {
        Object o = options.get(GIVEITEMS);
        return o == null ? null : (List<ItemStack>) o;
    }

    public List<ItemStack> getNeedItems() {
        Object o = options.get(NEEDITEMS);
        return o == null ? null : (List<ItemStack>) o;
    }

    public List<ItemStack> getTakeItems() {
        Object o = options.get(TAKEITEMS);
        return o == null ? null : (List<ItemStack>) o;
    }

    public List<PotionEffect> getEffects(){
        Object o = options.get(ENCHANTS);
        return o == null ? null : (List<PotionEffect>) o;
    }
    private boolean hasEffects() {return getEffects() != null;}
    public boolean clearInventory() {return options.containsKey(CLEARINVENTORY);}
    public boolean needsArmor() {return options.containsKey(NEEDARMOR);}
    public boolean needsItems() {return options.containsKey(NEEDITEMS);}
    public boolean hasItems() {
        return options.containsKey(NEEDITEMS) ||
                options.containsKey(GIVEITEMS);}
    public boolean hasGiveItems() {
        return  options.containsKey(GIVEITEMS);}

    public boolean shouldTeleportLobby() {
        return options.containsKey(TELEPORTLOBBY) ||
                options.containsKey(TELEPORTMAINLOBBY);
    }
    public boolean shouldTeleportWaitRoom() {
        return options.containsKey(TELEPORTWAITROOM) ||
                options.containsKey(TELEPORTMAINWAITROOM);
    }

    public boolean shouldTeleportSpectate() {
        return options.containsKey(TELEPORTSPECTATE);
    }

    public boolean shouldTeleportIn() {return options.containsKey(TELEPORTIN);}
    public boolean teleportsIn() {return shouldTeleportIn() || shouldTeleportWaitRoom();}

    public boolean shouldTeleportOut() {
        return options.containsKey(TELEPORTOUT) ||
                options.containsKey(TELEPORTTO);
    }

    public boolean blockBreakOff() {return options.containsKey(BLOCKBREAKOFF);}
    public boolean blockPlaceOff() {return options.containsKey(BLOCKPLACEOFF);}

    public Double getHealth() {return getDouble(HEALTH);}
    public Double getHealthP() {return getDouble(HEALTHP);}
    public Integer getHunger() {return getInt(HUNGER);}
    public Integer getMagic() { return getInt(MAGIC);}
    public Integer getMagicP() { return getInt(MAGICP);}
    public Double getWithinDistance() {return getDouble(WITHINDISTANCE);}
    public GameMode getGameMode() {return getGameMode(GAMEMODE);}
    public List<CommandLineString> getDoCommands() {
        final Object o = options.get(DOCOMMANDS);
        return o == null ? null : (List<CommandLineString>) o;
    }

    public Integer getInt(StateOption option){
        final Object o = options.get(option);
        return o == null ? null : (Integer) o;
    }

    public Double getDouble(StateOption option){
        final Object o = options.get(option);
        return o == null ? null : (Double) o;
    }

    public Float getFloat(StateOption option){
        final Object o = options.get(option);
        return o == null ? null : (Float) o;
    }

    public String getString(StateOption option){
        final Object o = options.get(option);
        return o == null ? null : (String) o;
    }

    public GameMode getGameMode(StateOption option){
        final Object o = options.get(option);
        return o == null ? null : (GameMode) o;
    }

    public Double getMoney(){return getDouble(MONEY);}
    public boolean hasMoney(){
        Double d = getDouble(MONEY);
        return d != null && d > 0;
    }
    public Float getFlightSpeed(){return getFloat(FLIGHTSPEED);}
    public Integer getInvulnerable(){return getInt(INVULNERABLE);}
    public Integer getRespawnTime(){return getInt(RESPAWNTIME);}
    public Integer getExperience(){return getInt(EXPERIENCE);}
    public boolean hasExperience(){return options.containsKey(EXPERIENCE);}

    public String getDisguiseAllAs() {return getString(DISGUISEALLAS);}
    public Boolean undisguise() {return options.containsKey(UNDISGUISE);}

    public boolean playerReady(ArenaPlayer p, World w) {
        if (p==null || !p.isOnline() || p.isDead() || p.getPlayer().isSleeping())
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
        if (options.containsKey(GAMEMODE)){
            GameMode gm = getGameMode();
            if (p.getPlayer().getGameMode() != gm){
                return false;}
        }

        if (options.containsKey(NOINVENTORY)){
            if (InventoryUtil.hasAnyItem(p.getPlayer()))
                return false;
        }
        if (options.containsKey(SAMEWORLD) && w!=null){
            if (p.getLocation().getWorld().getUID() != w.getUID())
                return false;
        }
        if (needsArmor()){
            if (!InventoryUtil.hasArmor(p.getPlayer()))
                return false;
        }
        if (options.containsKey(LEVELRANGE)){
            MinMax mm = (MinMax) options.get(LEVELRANGE);
            if (!mm.contains(p.getLevel()))
                return false;
        }
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
                sb.append("&5 - &6").append(is.getAmount()).append(" ").append(is.getData());
            }
        }
        if (options.containsKey(NOINVENTORY)){
            hasSomething = true;
            sb.append("&5 - &6Clear Inventory");
        }
        if (options.containsKey(GAMEMODE)){
            hasSomething = true;
            GameMode gm = getGameMode();
            sb.append("&5 - &6GameMode=").append(gm.toString());
        }
        if (needsArmor()){
            hasSomething = true;
            sb.append("&5 - &6Armor");
        }
        if (options.containsKey(LEVELRANGE)){
            MinMax mm = (MinMax) options.get(LEVELRANGE);
            sb.append("&a - lvl=").append(mm.toString());
        }
        return hasSomething ? sb.toString() : null;
    }

    public String getNotReadyMsg(ArenaPlayer p, World w, String headerMsg) {
        StringBuilder sb = new StringBuilder(headerMsg);
        boolean isReady = true;
        if (needsItems()){
            Inventory inv = p.getInventory();
            List<ItemStack> items = getNeedItems();
            for (ItemStack is : items){
                int amountInInventory =InventoryUtil.getItemAmountFromInventory(inv, is);
                if (amountInInventory < is.getAmount()){
                    int needed = amountInInventory - is.getAmount();
                    sb.append("&5 - &e").append(needed).append(" ").append(is.getType()).append("\n");
                    isReady = false;
                }
            }
        }
        if (options.containsKey(GAMEMODE)){
            GameMode gm = getGameMode();
            if (p.getPlayer().getGameMode() != gm){
                sb.append("&5 -&e a &6You need to be in &c").append(gm).append("&e mode \n");
                isReady = false;
            }
        }
        if (options.containsKey(NOINVENTORY)){
            if (InventoryUtil.hasAnyItem(p.getPlayer())){
                sb.append("&5 -&e a &6Clear Inventory\n");
                isReady = false;
            }
        }
        if (options.containsKey(SAMEWORLD) && w!=null){
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

        if (options.containsKey(LEVELRANGE)){
            MinMax mm = (MinMax) options.get(LEVELRANGE);
            if (!mm.contains(p.getLevel())){
                sb.append("&a - lvl=").append(mm.toString());
                isReady = false;
            }
        }
        return isReady? null : sb.toString();
    }

    public String getPrizeMsg(String header) {
        return getPrizeMsg(header,null);
    }
    public String getPrizeMsg(String header, Double poolMoney) {
        StringBuilder sb = new StringBuilder();
        boolean hasSomething = false;
        if (header != null){
            sb.append(header);
            hasSomething = true;
        }
        if (hasExperience()){
            hasSomething = true;
            sb.append("&5 - &2").append(getExperience()).append(" experience");
        }
        if (hasMoney()){
            hasSomething = true;
            sb.append("&5 - &6").append(getMoney()).append(" ").append(Defaults.MONEY_STR);
        }
        if (poolMoney != null){
            hasSomething = true;
            sb.append("&5 - &6").append(poolMoney).append(" ").append(Defaults.MONEY_STR);
        }

        if (getGiveItems() != null){
            hasSomething = true;
            List<ItemStack> items = getGiveItems();
            ArmorLevel lvl = InventoryUtil.hasArmorSet(items);
            if (lvl != null){
                sb.append("&5 - &a").append(lvl.toString()).append(" ARMOR");
            }
            for (ItemStack is : items){
                if (lvl != null && InventoryUtil.sameMaterial(lvl,is))
                    continue;
                String enchanted = !is.getEnchantments().isEmpty() ? " &4Enchanted ": "";
                sb.append("&5 - &a").append(is.getAmount()).append(enchanted).append(is.getType().toString());
            }
        }
        if (hasEffects()){
            hasSomething = true;
            for (PotionEffect ewa : getEffects()){
                if (ewa != null)
                    sb.append("&5 - &b").append(EffectUtil.getCommonName(ewa));
            }
        }

        return hasSomething? sb.toString() : null;
    }

    public PVPState getPVP() {
        if (options.containsKey(PVPON)){
            return PVPState.ON;
        } else if (options.containsKey(PVPOFF)){
            return PVPState.OFF;
        } else if (options.containsKey(INVINCIBLE)){
            return PVPState.INVINCIBLE;
        }
        return null;
    }

    public boolean respawn() {
        return options.containsKey(RESPAWN);
    }
    public boolean randomRespawn() {
        return options.containsKey(RANDOMRESPAWN) ||
                options.containsKey(RANDOMSPAWN);
    }

    public boolean deEnchant() {
        return options.containsKey(DEENCHANT);
    }

    public boolean woolTeams() {
        return options.containsKey(WOOLTEAMS) || options.containsKey(ALWAYSWOOLTEAMS);
    }
    public Map<StateOption,Object> getOptions() {
        return options;
    }
    public boolean shouldClearRegion() {
        return options.containsKey(WGCLEARREGION);
    }

    public void addOption(StateOption option) throws InvalidOptionException {
        if (option.hasValue()) throw new InvalidOptionException("StateOption needs a value!");
        addOption(option, null);
    }

    public void addOption(StateOption option, Object value) throws InvalidOptionException {
        if (option.hasValue() && value==null) throw new InvalidOptionException("StateOption needs a value!");
        if (options==null){
            options = new HashMap<StateOption,Object>();}
        options.put(option,value);
    }

    public boolean hasOption(StateOption op) {
        return options != null && options.containsKey(op);
    }

    public boolean hasAnyOption(StateOption... ops) {
        if (options == null)
            return false;
        for (StateOption op: ops){
            if (options.containsKey(op))
                return true;
        }
        return false;
    }

    public boolean containsAll(StateOptions tops) {
        if (tops.options==null && options != null)
            return false;
        if (tops.options == null)
            return true;
        for (StateOption op: tops.options.keySet()){
            if (!options.containsKey(op)){
                return false;
            }
            if (op.hasValue() && !options.get(op).equals(tops.options.get(op)))
                return false;
        }
        return true;
    }

    public Object removeOption(StateOption op) {
        return options != null ? options.remove(op) : null;
    }

    public static String getInfo(MatchParams sq, String name) {
        StringBuilder sb = new StringBuilder();
        StateGraph at = sq.getStateOptions();
        String required = at.getRequiredString(null);
        String prestart = at.getGiveString(MatchState.ONPRESTART);
        String start = at.getGiveString(MatchState.ONSTART);
        String onspawn = at.getGiveString(MatchState.ONSPAWN);
        String prizes = at.getGiveString(MatchState.WINNERS);
        String firstPlacePrizes = at.getGiveString(MatchState.FIRSTPLACE);
        String participantPrizes = at.getGiveString(MatchState.PARTICIPANTS);
        boolean rated = sq.isRated();
        String teamSizes = ArenaSize.rangeString(sq.getMinTeamSize(), sq.getMaxTeamSize());
        sb.append("&eThis is ").append(rated ? "a &4Rated" : "an &aUnrated").
                append("&e ").append(name).append(". ");
        sb.append("&eTeam size=&6").append(teamSizes);
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
            sb.append("\n&ePrize for &6participation:&e ").append(participantPrizes);}
        sb.append(prizes==null? "&aNone" : prizes);
        if (firstPlacePrizes != null){
            sb.append("\n&ePrize for getting &b1st &eplace:");
            sb.append(firstPlacePrizes);
        }

        return sb.toString();
    }

    public Map<Integer, ArenaClass> getClasses(){
        Object o = options.get(GIVECLASS);
        return o == null ? null : (Map<Integer, ArenaClass>) o;
    }

    public Map<Integer, String> getDisguises(){
        Object o = options.get(GIVEDISGUISE);
        return o == null ? null : (Map<Integer, String>) o;
    }


    public List<String> getAddPerms() {
        final Object o = options.get(ADDPERMS);
        return o == null ? null : (List<String>) o;
    }

    /// TODO, not sure this will work properly, I really want to remove those perms that were added in another section!!
    public List<String> getRemovePerms() {
        final Object o = options.get(ADDPERMS);
        return o == null ? null : (List<String>) o;
    }

    public Location getTeleportToLoc() {return returnLoc(TELEPORTTO);}

    private Location returnLoc(StateOption to){
        final Object o = options.get(to);
        return o == null ? null : (Location) o;
    }

    private ChatColor getColor(StateOption v, StateOptions so) {
        return so !=null && so.options.containsKey(v) ? ChatColor.WHITE : ChatColor.GOLD;
    }
    public String getOptionString(StateOptions so) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Entry<StateOption,Object> entry: options.entrySet()){
            if (!first) sb.append("&2, " );
            first = false;
            sb.append(getColor(entry.getKey(), so).toString());
            sb.append(entry.getKey());
            Object value = so !=null && so.options.containsKey(entry.getKey()) ?
                    so.options.get(entry.getKey()) :
                    entry.getValue();
            if (value != null){
                StateOption i = entry.getKey();
                if (i.equals(TransitionOption.GIVECLASS) ||
                        i.equals(TransitionOption.ENCHANTS) ||
                        i.equals(TransitionOption.GIVEDISGUISE)) {
                    continue;
                }
                sb.append(":").append(value);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString(){
        return getOptionString(null);
    }
}
