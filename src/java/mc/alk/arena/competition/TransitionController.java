package mc.alk.arena.competition;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.ArenaController;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.PlayerStoreController;
import mc.alk.arena.controllers.TeleportLocationController;
import mc.alk.arena.controllers.plugins.DisguiseInterface;
import mc.alk.arena.controllers.plugins.HeroesController;
import mc.alk.arena.controllers.plugins.PylamoController;
import mc.alk.arena.controllers.plugins.WorldGuardController;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.StateGraph;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.regions.WorldGuardRegion;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.TeamUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class TransitionController {

    /**
     * Perform a transition
     * @param am Match, which match to perform the transition on
     * @param transition: which transition are we doing
     * @param teams: which teams to affect
     * @param onlyInMatch: only perform the actions on people still in the arena match
     */
    public static void transition(PlayerHolder am, CompetitionState transition, Collection<ArenaTeam> teams, boolean onlyInMatch){
        if (teams == null)
            return;
        boolean first = true;
        for (ArenaTeam team: teams){
            transition(am,transition,team,onlyInMatch,first);
            first = false;
        }
    }

    public static boolean transition(PlayerHolder am, final CompetitionState transition, ArenaTeam team, boolean onlyInMatch) {
        try {
            return transition(am,transition,team,onlyInMatch,true);
        } catch (Exception e){
            Log.printStackTrace(e);
            return false;
        }
    }

    static boolean transition(PlayerHolder am, final CompetitionState transition, ArenaTeam team, boolean onlyInMatch,
                              boolean performOncePerTransitionOptions) {
        final StateOptions mo = am.getParams().getStateOptions(transition);
        if (mo == null)
            return true;
        if (performOncePerTransitionOptions && (am instanceof ArenaController)){
            ArenaController ac = (ArenaController) am;
            /// Options that don't affect players first
            if (WorldGuardController.hasWorldGuard() && ac.getArena() != null && ac.getArena().hasRegion()){
                WorldGuardRegion region = ac.getArena().getWorldGuardRegion();
                /// Clear the area
                if (mo.shouldClearRegion()){
                    WorldGuardController.clearRegion(region);}

                if (mo.hasOption(TransitionOption.WGRESETREGION)){
                    if (PylamoController.enabled() && ac.getArena().getPylamoRegion() != null){
                        PylamoController.resetRegion(ac.getArena().getPylamoRegion());
                    } else {
                        WorldGuardController.pasteSchematic(region);
                    }
                }
            }
        }
        for (ArenaPlayer p : team.getPlayers()){
            transition(am, transition,p,team, onlyInMatch);
        }
        return true;
    }

    public static boolean transition(final PlayerHolder am, final CompetitionState transition,
                                     final ArenaPlayer player, final ArenaTeam team, final boolean onlyInMatch) {
        if (team != null && team.getIndex() != -1) {
            MatchParams mp = am.getParams().getTeamParams(team.getIndex());
            if (mp != null){
                return transition(am, transition, player, team, onlyInMatch, mp.getStateGraph());}
        }
        return transition(am,transition,player,team,onlyInMatch,am.getParams().getStateGraph());

    }

    @SuppressWarnings("ConstantConditions")
    private static boolean transition(final PlayerHolder am, final CompetitionState transition,
                                      final ArenaPlayer player, final ArenaTeam team, final boolean onlyInMatch,
                                      StateGraph tops) {
        if (tops == null){
            return true;}
        final StateOptions mo = tops.getOptions(transition);
        if (mo == null){ /// no options
            return true;}
        if (Defaults.DEBUG_TRANSITIONS) Log.info("-- transition "+am.getClass().getSimpleName()+"  " + transition + " p= " +player.getName() +
                " ops="+am.getParams().getThisStateGraph().getOptions(transition)+" onlyInMatch="+onlyInMatch+
                " inArena="+am.isHandled(player) + " dead="+player.isDead()+":"+player.getHealth()+" online="+player.isOnline()+" clearInv=" +
                am.getParams().getThisStateGraph().hasOptionAt(transition, TransitionOption.CLEARINVENTORY));
        final boolean insideArena = am.isHandled(player);
        final boolean teleportIn = mo.shouldTeleportIn();
        final boolean teleportRoom = mo.shouldTeleportWaitRoom() || mo.shouldTeleportLobby() || mo.shouldTeleportSpectate();
        /// If the flag onlyInMatch is set, we should leave if the player isnt inside.  disregard if we are teleporting people in
        if (onlyInMatch && (!insideArena && !(teleportIn || teleportRoom) ||
                am instanceof Match && !((Match)am).isInMatch(player) &&
                        player.getCompetition()!=null && !player.getCompetition().equals(am) )  ){
            return true;}

        final boolean teleportOut = mo.shouldTeleportOut();
        final boolean wipeInventory = mo.clearInventory();

        List<PotionEffect> effects = mo.getEffects()!=null ? new ArrayList<PotionEffect>(mo.getEffects()) : null;
        final Integer hunger = mo.getHunger();

        final int teamIndex = team == null ? -1 : team.getIndex();
        boolean playerReady = player.isOnline();
        final boolean dead = !player.isOnline() || player.isDead();
        final Player p = player.getPlayer();

        /// Teleport In. only tpin, respawn tps happen elsewhere
        if ((teleportIn && transition != MatchState.ONSPAWN) || teleportRoom){
            if ((insideArena || am.checkReady(player, team, mo, true)) && !dead){
                TeleportLocationController.teleport(am, team, player,mo, teamIndex);
            } else {
                playerReady = false;
            }
        }

        final boolean storeAll = mo.hasOption(TransitionOption.STOREALL);
        PlayerStoreController psc = PlayerStoreController.getPlayerStoreController();
        final boolean armorTeams = tops.hasAnyOption(TransitionOption.ARMORTEAMS);
        final boolean woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS);

        /// Only do if player is online options
        if (playerReady && !dead){
            Double prizeMoney = null; /// kludge, take out when I find a better way to display messages
            if (storeAll || mo.hasOption(TransitionOption.STOREGAMEMODE)){psc.storeGamemode(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREEXPERIENCE)){ psc.storeExperience(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREITEMS)) { psc.storeItems(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREHEALTH)){ psc.storeHealth(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREHUNGER)){ psc.storeHunger(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREMAGIC)){ psc.storeMagic(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREHEROCLASS)){psc.storeHeroClass(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREGAMEMODE)){psc.storeGodmode(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREFLIGHT)){psc.storeFlight(player);}
            if (storeAll || mo.hasOption(TransitionOption.STOREENCHANTS)){psc.storeEffects(player);}
            if (wipeInventory){InventoryUtil.clearInventory(p);}
            if (mo.hasOption(TransitionOption.CLEAREXPERIENCE)){ ExpUtil.clearExperience(p);}
            if (mo.hasOption(TransitionOption.HEALTH)) { PlayerUtil.setHealth(p, mo.getHealth());}
            if (mo.hasOption(TransitionOption.HEALTHP)) { PlayerUtil.setHealthP(p, mo.getHealthP());}
            if (mo.hasOption(TransitionOption.MAGIC)) { setMagicLevel(p, mo.getMagic()); }
            if (mo.hasOption(TransitionOption.MAGICP)) { setMagicLevelP(p, mo.getMagicP()); }
            if (hunger != null) { PlayerUtil.setHunger(p, hunger); }
            if (mo.hasOption(TransitionOption.INVULNERABLE)) { PlayerUtil.setInvulnerable(p,mo.getInvulnerable()*20); }
            if (mo.hasOption(TransitionOption.GAMEMODE)) { PlayerUtil.setGameMode(p,mo.getGameMode()); }
            if (mo.hasOption(TransitionOption.FLIGHTOFF)) { PlayerUtil.setFlight(p,false); }
            if (mo.hasOption(TransitionOption.FLIGHTON)) { PlayerUtil.setFlight(p,true); }
            if (mo.hasOption(TransitionOption.FLIGHTSPEED)) { PlayerUtil.setFlightSpeed(p,mo.getFlightSpeed()); }
            if (mo.hasOption(TransitionOption.DOCOMMANDS)) { PlayerUtil.doCommands(p,mo.getDoCommands()); }
            if (mo.deEnchant()) { psc.deEnchant(p);}
            if (mo.undisguise() != null && mo.undisguise()) {DisguiseInterface.undisguise(p);}
            if (mo.getDisguiseAllAs() != null) {DisguiseInterface.disguisePlayer(p, mo.getDisguiseAllAs());}
            if (mo.getMoney() != null) {MoneyController.add(player.getName(), mo.getMoney());}
            if (mo.hasOption(TransitionOption.POOLMONEY) && am instanceof Match) {
                prizeMoney = ((Match)am).getPrizePoolMoney() * mo.getDouble(TransitionOption.POOLMONEY) /
                        team.size();
                if (prizeMoney >= 0){
                    MoneyController.add(player.getName(), prizeMoney);
                } else {
                    MoneyController.subtract(player.getName(), prizeMoney);
                }
            }
            if (mo.getExperience() != null) {ExpUtil.giveExperience(p, mo.getExperience());}
            if (mo.hasOption(TransitionOption.REMOVEPERMS)){ removePerms(player, mo.getRemovePerms());}
            if (mo.hasOption(TransitionOption.ADDPERMS)){ addPerms(player, mo.getAddPerms(), 0);}
            if (mo.hasOption(TransitionOption.GIVECLASS) && player.getCurrentClass() == null){
                final ArenaClass ac = getArenaClass(mo,teamIndex);
                if (ac != null && ac.valid()){ /// Give class items and effects
                    if (mo.woolTeams()) TeamUtil.setTeamHead(teamIndex, player); // give wool heads first
                    if (armorTeams){
                        ArenaClassController.giveClass(player, ac, TeamUtil.getTeamColor(teamIndex));
                    } else{
                        ArenaClassController.giveClass(player, ac);
                    }
                }
            }
            if (mo.hasOption(TransitionOption.CLASSENCHANTS)){
                ArenaClass ac = player.getCurrentClass();
                if (ac != null){
                    ArenaClassController.giveClassEnchants(p, ac);}
            }
            if (mo.hasOption(TransitionOption.GIVEDISGUISE) && DisguiseInterface.enabled()){
                final String disguise = getDisguise(mo,teamIndex);
                if (disguise != null){ /// Give class items and effects
                    DisguiseInterface.disguisePlayer(p, disguise);}
            }
            if (mo.hasOption(TransitionOption.GIVEITEMS)){
                Color color = armorTeams ? TeamUtil.getTeamColor(teamIndex) : null;
                giveItems(transition, player, mo.getGiveItems(),teamIndex, woolTeams, insideArena,color);
            }

            try{if (effects != null)
                EffectUtil.enchantPlayer(p, effects);
            } catch (Exception e){
                if (!Defaults.DEBUG_VIRTUAL)
                    Log.warn("BattleArena "+p.getName()+" was not enchanted");
            }
            if (Defaults.ANNOUNCE_GIVEN_ITEMS){
                String prizeMsg = mo.getPrizeMsg(null, prizeMoney);
                if (prizeMsg != null){
                    MessageUtil.sendMessage(player,"&eYou have been given \n"+prizeMsg);}
            }
            if (teleportIn){
                transition(am, MatchState.ONSPAWN, player, team, false);
            }
            /// else we have a subste of the options we should always do regardless if they are alive or not
        }  else if (teleportOut){
            if (mo.hasOption(TransitionOption.REMOVEPERMS)){ removePerms(player, mo.getRemovePerms());}
            if (mo.hasOption(TransitionOption.GAMEMODE)) { PlayerUtil.setGameMode(p,mo.getGameMode()); }
            if (mo.hasOption(TransitionOption.FLIGHTOFF)) { PlayerUtil.setFlight(p,false); }
            if (mo.deEnchant()) { psc.deEnchant(p);}


            if (wipeInventory) { InventoryUtil.clearInventory(p); }
        }

        /// Teleport out, need to do this at the end so that all the onCancel/onComplete options are completed first
        if (teleportOut ){ /// Lets not teleport people out who are already out(like dead ppl)
            TeleportLocationController.teleportOut(am, team, player,mo);
        }
        /// Restore their exp and items.. Has to happen AFTER teleport
        boolean restoreAll = mo.hasOption(TransitionOption.RESTOREALL);
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREGAMEMODE)){ psc.restoreGamemode(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREEXPERIENCE)) { psc.restoreExperience(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREITEMS)){
            if (woolTeams && teamIndex != -1){
                /// Teams that have left can have a -1 teamIndex
                TeamUtil.removeTeamHead(teamIndex, p);
            }
            if (Defaults.DEBUG_TRANSITIONS)Log.info("   "+transition+" transition restoring items "+insideArena);
            psc.restoreItems(player);
        }
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREENCHANTS)){psc.restoreEffects(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREHEALTH)){ psc.restoreHealth(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREHUNGER)){ psc.restoreHunger(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREMAGIC)) { psc.restoreMagic(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREHEROCLASS)){psc.restoreHeroClass(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREGODMODE)){psc.restoreGodmode(player);}
        if (restoreAll || mo.hasOption(TransitionOption.RESTOREFLIGHT)){psc.restoreFlight(player);}
        return true;
    }

    private static void setMagicLevel(Player p, Integer magic) {
        HeroesController.setMagicLevel(p, magic);
    }

    private static void setMagicLevelP(Player p, Integer magic) {
        HeroesController.setMagicLevelP(p, magic);
    }

    private static void removePerms(ArenaPlayer p, List<String> perms) {
//		if (perms == null || perms.isEmpty()) {
//        }
        /// TODO complete
    }

    private static void addPerms(ArenaPlayer p, List<String> perms, int ticks) {
        if (perms == null || perms.isEmpty())
            return;
        PermissionAttachment attachment = p.getPlayer().addAttachment(BattleArena.getSelf(),ticks);
        for (String perm: perms){
            attachment.setPermission(perm, true);}
    }

    private static void giveItems(final CompetitionState ms, final ArenaPlayer p, final List<ItemStack> items,
                                  final int teamIndex,final boolean woolTeams, final boolean insideArena, Color color) {
        if (woolTeams && insideArena){
            TeamUtil.setTeamHead(teamIndex, p);}
        if (Defaults.DEBUG_TRANSITIONS)Log.info("   "+ms+" transition giving items to " + p.getName());
        if (items == null || items.isEmpty())
            return;
        InventoryUtil.addItemsToInventory(p.getPlayer(),items,woolTeams,color);
    }

    private static ArenaClass getArenaClass(StateOptions mo, final int teamIndex) {
        Map<Integer,ArenaClass> classes = mo.getClasses();
        if (classes == null)
            return null;
        if (classes.containsKey(teamIndex)){
            return classes.get(teamIndex);
        } else if (classes.containsKey(ArenaClass.DEFAULT)){
            return classes.get(ArenaClass.DEFAULT);
        }
        return null;
    }

    private static String getDisguise(StateOptions mo, final int teamIndex) {
        Map<Integer,String> disguises = mo.getDisguises();
        if (disguises==null)
            return null;
        if (disguises.containsKey(teamIndex)){
            return disguises.get(teamIndex);
        } else if (disguises.containsKey(DisguiseInterface.DEFAULT)){
            return disguises.get(DisguiseInterface.DEFAULT);
        }
        return null;
    }

}
