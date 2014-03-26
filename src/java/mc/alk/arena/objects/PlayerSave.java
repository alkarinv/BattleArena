package mc.alk.arena.objects;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.plugins.EssentialsController;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.listeners.BAPlayerListener;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author alkarin
 */
public class PlayerSave {
    final ArenaPlayer player;

    Integer experience;

    Double health;
    Double healthp;
    Integer hunger;
    Integer magic;
    Double magicp;
    PInv items;
    PInv matchItems;
    GameMode gamemode;

    Boolean godmode;

    Location location;

    Collection<PotionEffect> effects;

    Boolean flight;
    String arenaClass;
    String oldTeam;
    private Object scoreboard;
    Double money;

    public PlayerSave(ArenaPlayer player) {
        this.player = player;
    }

    public String getName() {
        return player.getName();
    }

    public Integer getExp() {
        return experience;
    }

    public void setExp(Integer exp) {
        this.experience = exp;
    }

    public Double getHealth() {
        return health;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    public Double getHealthp() {
        return healthp;
    }

    public void setHealthp(Double healthp) {
        this.healthp = healthp;
    }

    public Integer getHunger() {
        return hunger;
    }

    public void setHunger(Integer hunger) {
        this.hunger = hunger;
    }

    public Integer getMagic() {
        return magic;
    }

    public void setMagic(Integer magic) {
        this.magic = magic;
    }

    public Double getMagicp() {
        return magicp;
    }

    public void setMagicp(Double magicp) {
        this.magicp = magicp;
    }

    public PInv getItems() {
        return items;
    }

    public void setItems(PInv items) {
        this.items = items;
    }

    public GameMode getGamemode() {
        return gamemode;
    }

    public void setGamemode(GameMode gamemode) {
        this.gamemode = gamemode;
    }

    public Boolean getGodmode() {
        return godmode;
    }

    public void setGodmode(Boolean godmode) {
        this.godmode = godmode;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Collection<PotionEffect> getEffects() {
        return effects;
    }

    public void setEffects(Collection<PotionEffect> effects) {
        this.effects = effects;
    }

    public Boolean getFlight() {
        return flight;
    }

    public void setFlight(Boolean flight) {
        this.flight = flight;
    }

    public String getArenaClass() {
        return arenaClass;
    }

    public void setArenaClass(String arenaClass) {
        this.arenaClass = arenaClass;
    }

    public String getOldTeam() {
        return oldTeam;
    }

    public void setOldTeam(String oldTeam) {
        this.oldTeam = oldTeam;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }


    public PInv getMatchItems() {
        return matchItems;
    }

    public int storeExperience() {
        Player p = player.getPlayer();
        int exp = ExpUtil.getTotalExperience(p);
        if (exp == 0)
            return 0;
        experience = experience == null ? exp : experience + exp;
        ExpUtil.setTotalExperience(p, 0);
        try {
            p.updateInventory();
        } catch (Exception e) {/* do nothing */}
        return exp;
    }

    public void restoreExperience() {
        if (experience==null)
            return;
        Player p = player.getPlayer();
        ExpUtil.setTotalExperience(p.getPlayer(), experience);
        experience=null;
    }

    public Integer removeExperience() {
        Integer exp = experience;
        experience = null;
        return exp;
    }

    public void storeHealth() {
        if (health!=null)
            return;

        health = player.getHealth();
        if (Defaults.DEBUG_STORAGE) Log.info("storing health=" + health + " for player=" + player.getName());
    }

    public void restoreHealth() {
        if (health == null || health <= 0)
            return;
        if (Defaults.DEBUG_STORAGE) Log.info("restoring health=" + health+" for player=" + player.getName());
        PlayerUtil.setHealth(player.getPlayer(),health);
        health=null;
    }

    public Double removeHealth() {
        Double rhealth = health;
        health = null;
        return rhealth;
    }

    public void storeHunger() {
        if (hunger !=null)
            return;
        hunger = player.getFoodLevel();
    }

    public void restoreHunger() {
        if (hunger == null || hunger <= 0)
            return;
        PlayerUtil.setHunger(player.getPlayer(), hunger);
        hunger = null;
    }
    public Integer removeHunger(){
        Integer ret = hunger;
        hunger = null;
        return ret;
    }

    public void storeEffects() {
        if (effects !=null)
            return;
        effects = new ArrayList<PotionEffect>(player.getPlayer().getActivePotionEffects());
    }

    public void restoreEffects() {
        if (effects == null)
            return;
        EffectUtil.enchantPlayer(player.getPlayer(), effects);
        effects = null;
    }

    public Collection<PotionEffect> removeEffects() {
        Collection<PotionEffect> ret = effects;
        effects = null;
        return ret;
    }

    public void storeMagic() {
        if (!HeroesController.enabled() || magic != null)
            return;
        magic = HeroesController.getMagicLevel(player.getPlayer());
    }

    public void restoreMagic() {
        if (!HeroesController.enabled() || magic ==null)
            return;
        HeroesController.setMagicLevel(player.getPlayer(), magic);
        magic = null;
    }

    public Integer removeMagic() {
        Integer ret = magic;
        magic = null;
        return ret;
    }

    public void storeItems() {
        if (items != null)
            return;
//        if (Defaults.DEBUG_STORAGE) Log.info("storing items for = " + name +" contains=" + itemmap.containsKey(name));
        InventoryUtil.closeInventory(player.getPlayer());
        items = new PInv(player.getInventory());
        InventorySerializer.saveInventory(player.getName(), items);
    }

    public void restoreItems() {
        //        if (Defaults.DEBUG_STORAGE)  Log.info("   "+p.getName()+" psc contains=" + itemmap.containsKey(p.getName()) +"  dead=" + p.isDead()+" online=" + p.isOnline());
        if (items ==null)
            return;
        InventoryUtil.addToInventory(player.getPlayer(), items);
        items = null;
    }

    public PInv removeItems() {
        PInv ret = items;
        items = null;
        return ret;
    }

    public void storeMatchItems() {
        final String name= player.getName();
//        if (Defaults.DEBUG_STORAGE) Log.info("storing in match items for = " + name +" contains=" + matchitemmap.containsKey(name));
        InventoryUtil.closeInventory(player.getPlayer());
        final PInv pinv = new PInv(player.getInventory());
        if (matchItems == null){
            /// on the first entry, lets log that to disk
            InventorySerializer.saveInventory(name,pinv);
        }
        matchItems = pinv;
        BAPlayerListener.restoreMatchItemsOnReenter(name, pinv);
    }

    public void restoreMatchItems() {
        if (matchItems==null)
            return;
        InventoryUtil.addToInventory(player.getPlayer(), matchItems);
        matchItems = null;
    }

    public PInv removeMatchItems() {
        PInv ret = matchItems;
        matchItems = null;
        return ret;
    }

    public void storeGamemode() {
//        if (Defaults.DEBUG_STORAGE)  Log.info("storing gamemode " + p.getName() +" " + p.getPlayer().getGameMode());
        if (gamemode !=null)
            return;
        PermissionsUtil.givePlayerInventoryPerms(player.getPlayer());
        gamemode = player.getPlayer().getGameMode();
    }


    public void storeFlight() {
        if (!EssentialsController.enabled() || flight != null){
            return;}
//        if (Defaults.DEBUG_STORAGE)  Log.info("storing flight " + p.getName() +" " + p.getPlayer().getGameMode());
        Boolean b = EssentialsController.isFlying(player);
        if (b)
            flight = true;
    }

    public void restoreFlight() {
        if (flight == null)
            return;
        EssentialsController.setFlight(player.getPlayer(), flight);
        flight = null;
    }


    public void storeGodmode() {
        if (!EssentialsController.enabled() || godmode != null){
            return;
        }
//        if (Defaults.DEBUG_STORAGE)  Log.info("storing godmode " + p.getName() +" " + p.getPlayer().getGameMode());
        Boolean b = EssentialsController.isGod(player);
        if (b)
            godmode = true;
    }

    public void restoreGodmode() {
        if (godmode == null)
            return;
        EssentialsController.setGod(player.getPlayer(), godmode);
        godmode = null;
    }

    public void restoreGamemode() {
        if (gamemode == null)
            return;
        PlayerUtil.setGameMode(player.getPlayer(), gamemode);
        gamemode = null;
    }

    public GameMode removeGamemode() {
        GameMode ret = gamemode;
        gamemode = null;
        return ret;
    }

    public void storeArenaClass() {
        if (!HeroesController.enabled() || arenaClass != null)
            return;
        arenaClass = HeroesController.getHeroClassName(player.getPlayer());
    }

    public void restoreArenaClass() {
        if (!HeroesController.enabled() || arenaClass==null)
            return;
        HeroesController.setHeroClass(player.getPlayer(), arenaClass);
    }

    public void storeScoreboard() {
        if (scoreboard != null)
            return;
        scoreboard = PlayerUtil.getScoreboard(player.getPlayer());
    }

    public Object getScoreboard() {
        return scoreboard;
    }

    public void restoreScoreboard() {
        if (scoreboard==null)
            return;
        PlayerUtil.setScoreboard(player.getPlayer(), scoreboard);
    }

    public void restoreMoney() {
        if (money == null)
            return;
        MoneyController.add(player.getName(), money);
        money = null;
    }
}
