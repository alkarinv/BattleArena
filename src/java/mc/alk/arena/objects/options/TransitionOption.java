package mc.alk.arena.objects.options;

import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.objects.StateOption;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.MinMax;
import org.bukkit.GameMode;

import static mc.alk.arena.objects.options.TransitionOption.OPTYPE.CONFIG;
import static mc.alk.arena.objects.options.TransitionOption.OPTYPE.STATE;
import static mc.alk.arena.objects.options.TransitionOption.OPTYPE.TRANSITION;

public enum TransitionOption implements StateOption {
    /// Default only Options
    DUELONLY ("duelOnly", false, CONFIG),					/// DEFAULTS only: this game type is duel only
    ALWAYSOPEN("alwaysOpen", false, CONFIG),				/// DEFAULTS only: this game is always open to joins
    INDIVIDUALWINS("individualWins", false,CONFIG),		/// DEFAULTS only: discrete wins and losses per team

    /// preReq only Options
    NEEDARMOR ("needArmor",false, CONFIG),					/// PREREQ only: player needs armor to add the match
    NOINVENTORY("noInventory",false, CONFIG),				/// PREREQ only: player needs to have no inventory to add
    NEEDITEMS ("needItems",false, CONFIG),					/// PREREQ only: player needs the following items to add
    TAKEITEMS ("takeItems",false, CONFIG),					/// PREREQ only: player needs the following items to add (items removed)
    SAMEWORLD("sameWorld",false, CONFIG),					/// PREREQ only: player can only add from the same world
    WITHINDISTANCE("withinDistance",true,CONFIG),			/// PREREQ only: player needs to be within the following distance to add
    LEVELRANGE("levelRange",true, CONFIG),					/// =<range>: PREREQ only: player needs to be within the given range

    /// Save and Restore Options, These happen when a player first enters, and when the player leaves
    STOREALL("storeAll",false, TRANSITION),						/// Do all of the store options
    RESTOREALL("restoreAll", false, TRANSITION),				/// Do all of the restore options
    STOREEXPERIENCE("storeExperience",false,TRANSITION),		/// Store experience
    RESTOREEXPERIENCE("restoreExperience",false,TRANSITION),	/// Restore experience
    STOREGAMEMODE("storeGamemode",false,TRANSITION),			/// Store gamemode
    RESTOREGAMEMODE("restoreGamemode",false,TRANSITION),		/// Restore gamemode
    STOREITEMS("storeItems",false,TRANSITION),					/// Store Items
    RESTOREITEMS("restoreItems",false,TRANSITION),				/// Restore Items
    STOREENCHANTS("storeEnchants",false,TRANSITION),			/// Store Enchants/Potion Effects
    RESTOREENCHANTS("restoreEnchants",false,TRANSITION),		/// Restore Enchants/Potion Effects
    STOREHEALTH("storeHealth",false,TRANSITION),				/// Store Health
    RESTOREHEALTH("restoreHealth",false,TRANSITION),			/// Restore Health
    STOREHUNGER("storeHunger",false,TRANSITION),				/// Store Hunger
    RESTOREHUNGER("restoreHunger",false,TRANSITION),			/// Restore Hunger
    STOREGODMODE("storeGodmode",false,TRANSITION),				/// Store Godmode
    RESTOREGODMODE("restoreGodmode",false,TRANSITION),			/// Restore Godmode
    STOREFLIGHT("storeFlight",false,TRANSITION),				/// Store FLight
    RESTOREFLIGHT("restoreFlight",false,TRANSITION),			/// Restore Flight
    STOREMAGIC("storeMagic",false,TRANSITION),					/// HEROES only: Store Magic
    RESTOREMAGIC("restoreMagic",false,TRANSITION),				/// HEROES only: Restore Magic
    STOREHEROCLASS("storeHeroClass",false,TRANSITION),			/// HEROES only: Store the hero class
    RESTOREHEROCLASS("restoreHeroClass",false,TRANSITION),		/// HEROES only: Restore the hero class

    /// Default Options (options that need only be specified once anywhere (usually in defaults: ))
    ARMORTEAMS("armorTeams",false,CONFIG),					/// Use team armor colors when appropriate (right now just leather)
    WOOLTEAMS("woolTeams",false,CONFIG),					/// Use team Heads when team sizes are greater than 1 (found in teamColors.yml)
    ALWAYSWOOLTEAMS("alwaysWoolTeams", false,CONFIG),		/// Always use team Heads (found in teamColors.yml)
    ALWAYSTEAMNAMES("alwaysTeamNames", false,CONFIG),		/// Always use team Names (found in teamColors.yml)
    NOTEAMNAMECOLOR("noTeamNameColor", false,CONFIG),		/// Dont use team name color
    DROPITEMS("dropItems", false,CONFIG),					/// Drop Items when a player dies or quits

    /// Teleport Options
    TELEPORTMAINWAITROOM("teleportMainWaitRoom",false,TRANSITION), /// Teleport players to the main waitroom
    TELEPORTWAITROOM("teleportWaitRoom",false,TRANSITION), 	/// Teleport players to the waitroom
    TELEPORTMAINLOBBY("teleportMainLobby",false,TRANSITION), 	/// Teleport players to the main lobby
    TELEPORTLOBBY("teleportLobby",false,TRANSITION), 			/// Teleport players to the lobby
    TELEPORTSPECTATE("teleportSpectate",false,TRANSITION), 	/// Teleport players to the spectate spawns
    TELEPORTCOURTYARD("teleportCourtyard",false,TRANSITION), 	/// Teleport players to the courtyard
    TELEPORTIN ("teleportIn",false,TRANSITION),  				/// Teleport players into the arena
    TELEPORTOUT("teleportOut",false,TRANSITION),				/// Teleport players out of the arena, back to their old location
    TELEPORTTO("teleportTo", true,TRANSITION), 				/// =<location> : Teleport players to the given
    NOTELEPORT("noTeleport", false,TRANSITION), 				/// Prevent players from teleporting
    NOWORLDCHANGE("noWorldChange",false,TRANSITION),			/// Prevent players from changing world
    RESPAWNTIME("respawnTime", true,TRANSITION),				/// Set a timer that will respawn a player back at the team spawn

    /// Normal Stage Options
    CLEARINVENTORY ("clearInventory",false,TRANSITION), 		/// Clear the players inventory
    CLEAREXPERIENCE("clearExperience",false,TRANSITION), 		/// Clear the players experience
    GIVEITEMS("giveItems",false,TRANSITION), 					/// Give the player the items specified in items:
    GIVECLASS("giveClass",true,TRANSITION),					/// Give the player the specified class(if they don't already have one):
    GIVEDISGUISE("giveDisguise",false,TRANSITION),				/// Give the player the specified class in classes:
    HEALTH("health",true,TRANSITION),							/// =<int> : set the players health to the given amount
    HEALTHP("healthp",true,TRANSITION),						/// =<int> : set the players health to the given percent
    HUNGER("hunger",true,TRANSITION),							/// =<int> : set the players food level
    EXPERIENCE("experience",true,TRANSITION),					/// =<int>: give the player this much exp
    MAGIC("magic",true,TRANSITION),							/// =<int>: set the players magic to the given amount
    MAGICP("magicp",true,TRANSITION),							/// =<int>: set the players magic to the given percent
    MONEY("money",true,TRANSITION),							/// =<double>: give the player money.  PREREQ: charge a fee to enter
    EFFECT("effect",true,TRANSITION),							/// =<string>: do the effect
    POTIONDAMAGEON("potionDamageOn",false,TRANSITION),			/// force potion damage to be on
    PVPON("pvpOn",false,STATE),							/// Turn on PvP, by default friendly fire is off
    PVPOFF("pvpOff",false,STATE),							/// Turn off all Pvp
    INVINCIBLE("invincible",false, STATE),					/// Players are invincible
    INVULNERABLE("invulnerable",true,TRANSITION),				/// Players are invulnerable for the given amount of seconds: <int>
    BLOCKBREAKOFF("blockBreakOff",false,STATE),			/// Disallow Block breaks
    BLOCKBREAKON("blockBreakOn",false,STATE),				/// Allow block breaks
    BLOCKPLACEOFF("blockPlaceOff",false,STATE),			/// Disallow block place
    BLOCKPLACEON("blockPlaceOn",false,STATE),				/// Allow player to place blocks
    ITEMDROPOFF("itemDropOff",false,STATE),				/// Stop the player from throwing/dropping items
    ITEMPICKUPOFF("itemPickupOff",false,STATE),			/// Stop the player from pickkingup items
    HUNGEROFF("hungerOff",false,STATE),			        /// Stop the player from decreasing hunger level
    DISGUISEALLAS("disguiseAllAs",true,TRANSITION),			/// =<String> : Disguise the players as the given mob/player (needs DisguiseCraft)
    UNDISGUISE("undisguise",false,TRANSITION),					/// Undisguise all players in the arena (needs DisguiseCraft)
    ENCHANTS("enchants",true,TRANSITION),						/// Give the Enchants found in enchants:
    DEENCHANT("deEnchant",false,TRANSITION),					/// DeEnchant all positive and negative effects from the player
    CLASSENCHANTS("classEnchants",false,TRANSITION),			/// regive the enchants from the class they have chosen
    ADDPERMS("addPerms", false,TRANSITION),					/// NOT IMPLEMENTED
    REMOVEPERMS("removePerms", false,TRANSITION),				/// NOT IMPLEMENTED
    GAMEMODE("gameMode",true,TRANSITION),						/// =<GameMode> : sets the given gamemode of the player
    DOCOMMANDS("doCommands",true,TRANSITION),					/// Run a list of commands as either the console or player
    FLIGHTOFF("flightOff",false,TRANSITION),					/// Disable flight
    FLIGHTON("flightOn",false,TRANSITION),						/// Enable flight
    FLIGHTSPEED("flightSpeed",true,TRANSITION),				/// =<float> Set flight speed
    NOLEAVE("noLeave",false, STATE),						/// Prevent players from leaving the competition

    /// onSpawn and onDeath only Options
    RANDOMSPAWN ("randomSpawn",false,TRANSITION), 				/// Spawn player at a random spawn location

    /// onDeath Only Options
    RESPAWN ("respawn",false,TRANSITION),						/// Allow player to respawn in Arena after they have died
    RANDOMRESPAWN ("randomRespawn",false,TRANSITION), 			/// Respawn player at a random spawn location after they have died
    NOEXPERIENCELOSS("noExperienceLoss",false,TRANSITION),		/// cancel exp loss on death
    KEEPINVENTORY("keepInventory", false,TRANSITION),			/// Allow the players to keep whatever inventory they currently have when they respawn

    /// onJoin only options
    ALWAYSJOIN("alwaysJoin",false,CONFIG),					/// Allow players to add at any time

    /// onComplete only options
    REJOIN("rejoin",false, CONFIG),							/// Rejoin players

    /// onSpawn Only Options
    RESPAWNWITHCLASS("respawnWithClass", false, TRANSITION),	/// Respawn player with their previously selected class

    /// World Guard Option
    WGCLEARREGION("wgClearRegion",false, TRANSITION),			/// Clear the region of all items
    WGRESETREGION("wgResetRegion",false, TRANSITION),			/// Reset the region to it's previous state (from when it was created)
    WGNOLEAVE("wgNoLeave",false, STATE),					/// Disallow players from leaving the region
    WGNOENTER("wgNoEnter", false, STATE),					/// Disallow players from entering the region

    /// onVictory, onDrawers, onLosers only Options
    POOLMONEY("poolMoney",true, TRANSITION),					/// =<double>: give the players a percent of the money contributed by all players
    ;

    public boolean isState() {
        return opType==OPTYPE.STATE;
    }

    public boolean isTransition() {
        return opType == OPTYPE.TRANSITION;
    }

    public enum OPTYPE{
        STATE, TRANSITION, CONFIG
    }
    final String name; /// Transition name

    final boolean hasValue; /// whether the transition needs a value

    final OPTYPE opType;
    TransitionOption(String name,Boolean hasValue, OPTYPE opType){
        this.name= name;
        this.hasValue = hasValue;
        this.opType = opType;
    }

    @Override
    public String toString(){return name;}

    @Override
    public boolean hasValue(){return hasValue;}

    public static TransitionOption fromString(String str){
        str = str.toUpperCase();
        try {
            return TransitionOption.valueOf(str);
        } catch (IllegalArgumentException e){
            if (str.equals("DROPITEMOFF"))
                return TransitionOption.ITEMDROPOFF;
            else if (str.equals("RESETREGION"))
                return TransitionOption.WGRESETREGION;
            else if (str.equals("DISGUISEALL"))
                return TransitionOption.DISGUISEALLAS;
            else if (str.equals("COMMANDS") || str.equals("COMMAND"))
                return TransitionOption.DOCOMMANDS;
            else if (str.equals("CLASS"))
                return TransitionOption.GIVECLASS;
            else if (str.equals("INVULNERABILITY") || str.equals("INV"))
                return TransitionOption.INVULNERABLE;
            else if (str.equals("STOREPOTIONEFFECTS"))
                return TransitionOption.STOREENCHANTS;
            else if (str.equals("RESTOREPOTIONEFFECTS"))
                return TransitionOption.RESTOREENCHANTS;
            throw new IllegalArgumentException("The stage option " + str +" does not exist");
        }
    }

    public Object parseValue(String value) throws Exception{
        /// Handle values for this option
        switch(this){
            case HEALTHP:
            case HEALTH:
            case POOLMONEY:
            case WITHINDISTANCE:
            case MONEY:
                return Double.valueOf(value);
            case LEVELRANGE:
                return MinMax.valueOf(value);
            case DISGUISEALLAS:
                return value;
            case MAGIC: case MAGICP:
            case HUNGER:
            case EXPERIENCE:
            case INVULNERABLE:
                return Integer.valueOf(value);
            case FLIGHTSPEED:
                return Float.valueOf(value);
            case ENCHANTS:
                return EffectUtil.parseArg(value, 0, 120);
            case DOCOMMANDS:
                return value;
            case GIVECLASS:
                return ArenaClassController.getClass(value);
            case GAMEMODE:
                GameMode gm;
                try {
                    gm = GameMode.getByValue(Integer.valueOf(value));
                } catch (Throwable e){
                    gm = GameMode.valueOf(value.toUpperCase());
                }
                return gm; // multiply by number of ticks per second
            default:
                break;
        }
        return null;
    }
}
