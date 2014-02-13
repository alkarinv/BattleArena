package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.events.players.ArenaPlayerClassSelectedEvent;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.arena.util.plugins.DisguiseInterface;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaClassController {
	static HashMap<String,ArenaClass> classes = new HashMap<String,ArenaClass>();
    /** How much time since they last changed classes*/
    static Map<String, Long> userClassSwitchTime = new ConcurrentHashMap<String, Long>();

    static {
		classes.put(ArenaClass.CHOSEN_CLASS.getName().toUpperCase(), ArenaClass.CHOSEN_CLASS);
	}

	public static void addClass(ArenaClass ac){
		classes.put(ac.getName().toUpperCase(), ac);
		classes.put(MessageUtil.decolorChat(ac.getDisplayName()).toUpperCase(),ac);
		classes.put(MessageUtil.decolorChat(ac.getDisplayName().replaceAll("\\[\\]", "")).toUpperCase(),ac);
	}

	public static ArenaClass getClass(String name){
		return classes.get(name.toUpperCase());
	}

	public static void giveClass(ArenaPlayer player, ArenaClass ac) {
		giveClass(player,ac,null);
	}

	public static void giveClass(ArenaPlayer player, ArenaClass ac, Color color) {
		if (HeroesController.enabled())
			ac = giveHeroClass(player,ac);
		try{if (ac.getItems() != null)
            InventoryUtil.addItemsToInventory(player.getPlayer(), ac.getItems(),true, color);}
        catch (Exception e){/* do nothing, error would be reported inside InventoryUtil */}
		giveClassEnchants(player.getPlayer(),ac);
		if (ac.getDisguiseName()!=null && DisguiseInterface.enabled())
			DisguiseInterface.disguisePlayer(player.getPlayer(), ac.getDisguiseName());
		if (ac.getMobs() != null){
			try{
				List<SpawnInstance> mobs = new ArrayList<SpawnInstance>(ac.getMobs());
				player.setMobs(mobs);
				player.spawnMobs();
			} catch (Exception e){
				Log.printStackTrace(e);
			}
		}
		if (ac.getDoCommands() != null){
			PlayerUtil.doCommands(player.getPlayer(),ac.getDoCommands());
		}
		if (player.getPreferredClass() == null){
			player.setPreferredClass(ac);}
		player.setCurrentClass(ac);
	}

	private static ArenaClass giveHeroClass(ArenaPlayer player, ArenaClass ac){
		if (ac == ArenaClass.CHOSEN_CLASS){
			String className = HeroesController.getHeroClassName(player.getPlayer());
			if (className != null){
				ArenaClass ac2 = ArenaClassController.getClass(className);
				if (ac2 != null)
					return ac2;
			}
		}
		/// Set them to the appropriate heroes class if one exists with this name
		if (HeroesController.hasHeroClass(ac.getName())){
			HeroesController.setHeroClass(player.getPlayer(), ac.getName());
		}
		return ac;
	}

	public static void giveClassEnchants(Player player, ArenaClass ac) {
		try{if (ac.getEffects() != null) EffectUtil.enchantPlayer(player, ac.getEffects());}
        catch (Exception e){/* do nothing */}
	}

	public static Set<ArenaClass> getClasses() {
		return new HashSet<ArenaClass>(classes.values());
	}


    public static boolean changeClass(Player p, PlayerHolder am, final ArenaClass ac) {
        if (ac == null || !ac.valid()) /// Not a valid class
            return false;
        if (!p.hasPermission("arena.class.use."+ac.getName().toLowerCase())){
            MessageUtil.sendSystemMessage(p, "class_no_perms", ac.getDisplayName());
            return false;
        }

        final ArenaPlayer ap = BattleArena.toArenaPlayer(p);
        ArenaClass chosen = ap.getCurrentClass();
        if (chosen != null && chosen.getName().equals(ac.getName())){
            MessageUtil.sendSystemMessage(p, "class_you_are_already", ac.getDisplayName());
            return false;
        }
        String playerName = p.getName();
        if(userClassSwitchTime.containsKey(playerName)) {
            long t = (Defaults.TIME_BETWEEN_CLASS_CHANGE * 1000) - (System.currentTimeMillis() - userClassSwitchTime.get(playerName));
            if (t > 0){
                MessageUtil.sendSystemMessage(p, "class_wait_time", TimeUtil.convertMillisToString(t));
                return false;
            }
        }
        MatchParams mp = am.getParams();
        userClassSwitchTime.put(playerName, System.currentTimeMillis());
        /// check to see if they have a team head
        ArenaTeam at = ap.getTeam();
        boolean woolTeams = ((at != null && at.getIndex() != -1) && p.getInventory().getHelmet() != null &&
                InventoryUtil.sameItem(at.getHeadItem(), p.getInventory().getHelmet()));

//        boolean woolTeams = mp.hasAnyOption(TransitionOption.WOOLTEAMS);
        /// Have They have already selected a class this match, have they changed their inventory since then?
        /// If so, make sure they can't just select a class, drop the items, then choose another
        if (chosen != null){
            List<ItemStack> items = new ArrayList<ItemStack>();
            if (chosen.getItems()!=null)
                items.addAll(chosen.getItems());
            if (mp.hasOptionAt(MatchState.ONSPAWN, TransitionOption.GIVEITEMS) &&
                    mp.getGiveItems(MatchState.ONSPAWN) != null){
                items.addAll(mp.getGiveItems(MatchState.ONSPAWN));
            }
            if (Defaults.NEED_SAME_ITEMS_TO_CHANGE_CLASS && !InventoryUtil.sameItems(items, p.getInventory(), woolTeams)){
                MessageUtil.sendSystemMessage(p, "class_cant_switch_after_items");
                return false;
            }
        }
        am.callEvent(new ArenaPlayerClassSelectedEvent(ac));

        /// Clear their inventory first, then give them the class and whatever items were due to them from the config
        InventoryUtil.clearInventory(p, woolTeams);
        /// Also debuff them
        EffectUtil.deEnchantAll(p);

        MatchState state = am.getMatchState();
        boolean armorTeams = mp.hasAnyOption(TransitionOption.ARMORTEAMS);
        ArenaTeam team = am.getTeam(ap);
        int teamIndex = team == null ? -1 : team.getIndex();
        Color color = armorTeams && teamIndex != -1 ? TeamUtil.getTeamColor(teamIndex) : null;
        ap.despawnMobs();
        /// Regive class/items
        ArenaClassController.giveClass(ap, ac);
        ap.setPreferredClass(ac);
        List<ItemStack> items =mp.getGiveItems(state);
        if (items != null){
            try{ InventoryUtil.addItemsToInventory(p, items, true,color);} catch(Exception e){Log.printStackTrace(e);}}
        items =mp.getGiveItems(MatchState.ONSPAWN);
        if (items!=null){
            try{ InventoryUtil.addItemsToInventory(p, items, true,color);} catch(Exception e){Log.printStackTrace(e);}}

        /// Deal with effects/buffs
        List<PotionEffect> effects = mp.getEffects(state);
        if (effects!=null){
            EffectUtil.enchantPlayer(p, effects);}
        effects = mp.getEffects(MatchState.ONSPAWN);
        if (effects!=null){
            EffectUtil.enchantPlayer(p, effects);}

        MessageUtil.sendSystemMessage(p, "class_chosen", ac.getDisplayName());
        return true;
    }
}
