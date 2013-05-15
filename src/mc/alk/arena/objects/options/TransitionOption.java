package mc.alk.arena.objects.options;

import mc.alk.arena.util.MinMax;

import org.bukkit.GameMode;

public enum TransitionOption{
	/// Default only Options
	DUELONLY ("duelOnly", false),					/// DEFAULTS only: this game type is duel only
	ALWAYSOPEN("alwaysOpen", false),				/// DEFAULTS only: this game is always open to joins
	INDIVIDUALWINS("individualWins", false),		/// DEFAULTS only: discrete wins and losses per team

	/// preReq only Options
	NEEDARMOR ("needArmor",false),					/// PREREQ only: player needs armor to join the match
	NOINVENTORY("noInventory",false),				/// PREREQ only: player needs to have no inventory to join
	NEEDITEMS ("needItems",false),					/// PREREQ only: player needs the following items to join
	SAMEWORLD("sameWorld",false),					/// PREREQ only: player can only join from the same world
	WITHINDISTANCE("withinDistance",true),			/// PREREQ only: player needs to be within the following distance to join
	LEVELRANGE("levelRange",true),					/// =<range>: PREREQ only: player needs to be within the given range

	/// Save and Restore Options, These happen when a player first enters, and when the player leaves
	STOREALL("storeAll",false),						/// Do all of the store options
	RESTOREALL("restoreAll", false),				/// Do all of the restore options
	STOREEXPERIENCE("storeExperience",false),		/// Store experience
	RESTOREEXPERIENCE("restoreExperience",false),	/// Restore experience
	STOREGAMEMODE("storeGamemode",false),			/// Store gamemode
	RESTOREGAMEMODE("restoreGamemode",false),		/// Restore gamemode
	STOREITEMS("storeItems",false),					/// Store Items
	RESTOREITEMS("restoreItems",false),				/// Restore Items
	STOREHEALTH("storeHealth",false),				/// Store Health
	RESTOREHEALTH("restoreHealth",false),			/// Restore Health
	STOREHUNGER("storeHunger",false),				/// Store Hunger
	RESTOREHUNGER("restoreHunger",false),			/// Restore Hunger
	STOREMAGIC("storeMagic",false),					/// HEROES only: Store Magic
	RESTOREMAGIC("restoreMagic",false),				/// HEROES only: Restore Magic
	STOREHEROCLASS("storeHeroClass",false),			/// HEROES only: Store the hero class
	RESTOREHEROCLASS("restoreHeroClass",false),		/// HEROES only: Restore the hero class

	/// Default Options (options that need only be specified once anywhere (usually in defaults: ))
	ARMORTEAMS("armorTeams",false),					/// Use team armor colors when appropriate (right now just leather)
	WOOLTEAMS("woolTeams",false),					/// Use team Heads when team sizes are greater than 1 (found in teamColors.yml)
	ALWAYSWOOLTEAMS("alwaysWoolTeams", false),		/// Always use team Heads (found in teamColors.yml)
	ALWAYSTEAMNAMES("alwaysTeamNames", false),		/// Always use team Names (found in teamColors.yml)
	NOTEAMNAMECOLOR("noTeamNameColor", false),		/// Dont use team name color

	/// Teleport Options
	TELEPORTWAITROOM("teleportWaitRoom",false), 	/// Teleport players to the waitroom
	TELEPORTLOBBY("teleportLobby",false), 			/// Teleport players to the lobby
	TELEPORTCOURTYARD("teleportCourtyard",false), 	/// Teleport players to the courtyard
	TELEPORTIN ("teleportIn",false),  				/// Teleport players into the arena
	TELEPORTOUT("teleportOut",false),				/// Teleport players out of the arena, back to their old location
	TELEPORTTO("teleportTo", true), 				/// =<location> : Teleport players to the given
	NOTELEPORT("noTeleport", false), 				/// Prevent players from teleporting
	NOWORLDCHANGE("noWorldChange",false),			/// Prevent players from changing world
	RESPAWNTIME("respawnTime", true),				/// Set a timer that will respawn a player back at the team spawn

	/// Normal Stage Options
	CLEARINVENTORY ("clearInventory",false), 		/// Clear the players inventory
	CLEAREXPERIENCE("clearExperience",false), 		/// Clear the players experience
	GIVEITEMS("giveItems",false), 					/// Give the player the items specified in items:
	GIVECLASS("giveClass",false),					/// Give the player the specified class in classes:
	GIVEDISGUISE("giveDisguise",false),				/// Give the player the specified class in classes:
	HEALTH("health",true),							/// =<int> : set the players health to the given amount
	HEALTHP("healthp",true),						/// =<int> : set the players health to the given percent
	HUNGER("hunger",true),							/// =<int> : set the players food level
	EXPERIENCE("experience",true),					/// =<int>: give the player this much exp
	MAGIC("magic",true),							/// =<int>: set the players magic to the given amount
	MAGICP("magicp",true),							/// =<int>: set the players magic to the given percent
	MONEY("money",true),							/// =<double>: give the player money.  PREREQ: charge a fee to enter
	EFFECT("effect",true),							/// =<string>: do the effect
	POTIONDAMAGEON("potionDamageOn",false),			/// force potion damage to be on
	PVPON("pvpOn",false),							/// Turn on PvP, by default friendly fire is off
	PVPOFF("pvpOff",false),							/// Turn off all Pvp
	INVINCIBLE("invincible",false),					/// Players are invincible
	INVULNERABLE("invulnerable",true),				/// Players are invulnerable for the given amount of seconds: <int>
	BLOCKBREAKOFF("blockBreakOff",false),			/// Disallow Block breaks
	BLOCKBREAKON("blockBreakOn",false),				/// Allow block breaks
	BLOCKPLACEOFF("blockPlaceOff",false),			/// Disallow block place
	BLOCKPLACEON("blockPlaceOn",false),				/// Allow player to place blocks
	ITEMDROPOFF("itemDropOff",false),				/// Stop the player from throwing/dropping items
	ITEMPICKUPOFF("itemPickupOff",false),			/// Stop the player from pickkingup items
	DISGUISEALLAS("disguiseAllAs",true),			/// =<String> : Disguise the players as the given mob/player (needs DisguiseCraft)
	UNDISGUISE("undisguise",false),					/// Undisguise all players in the arena (needs DisguiseCraft)
	ENCHANTS("enchants",false),						/// Give the Enchants found in enchants:
	DEENCHANT("deEnchant",false),					/// DeEnchant all positive and negative effects from the player
	CLASSENCHANTS("classEnchants",false),			/// regive the enchants from the class they have chosen
	ADDPERMS("addPerms", false),					/// NOT IMPLEMENTED
	REMOVEPERMS("removePerms", false),				/// NOT IMPLEMENTED
	GAMEMODE("gameMode",true),						/// =<GameMode> : sets the given gamemode of the player
	DOCOMMANDS("doCommands",false),					/// Run a list of commands as either the console or player
	FLIGHTOFF("flightOff",false),					/// Disable flight
	FLIGHTON("flightOn",false),						/// Enable flight
	FLIGHTSPEED("flightSpeed",true),				/// =<float> Set flight speed

	/// onSpawn and onDeath only Options
	RANDOMSPAWN ("randomSpawn",false), 				/// Spawn player at a random spawn location

	/// onDeath Only Options
	RESPAWN ("respawn",false),						/// Allow player to respawn in Arena after they have died
	RANDOMRESPAWN ("randomRespawn",false), 			/// Respawn player at a random spawn location after they have died
	NOEXPERIENCELOSS("noExperienceLoss",false),		/// cancel exp loss on death
	KEEPINVENTORY("keepInventory", false),			/// Allow the players to keep whatever inventory they currently have when they respawn

	/// onJoin only options
	ALWAYSJOIN("alwaysJoin",false),					/// Allow players to join at any time

	/// onSpawn Only Options
	RESPAWNWITHCLASS("respawnWithClass", false),	/// Respawn player with their previously selected class

	/// World Guard Option
	WGCLEARREGION("wgClearRegion",false),			/// Clear the region of all items
	WGRESETREGION("wgResetRegion",false),			/// Reset the region to it's previous state (from when it was created)
	WGNOLEAVE("wgNoLeave",false),					/// Disallow players from leaving the region
	WGNOENTER("wgNoEnter", false),					/// Disallow players from entering the region
	;

	final String name; /// Transition name

	final boolean hasValue; /// whether the transition needs a value

	TransitionOption(String name,Boolean hasValue){this.name= name;this.hasValue = hasValue;}

	@Override
	public String toString(){return name;}

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
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public Object parseValue(String value) throws Exception{
		/// Handle values for this option
		switch(this){
		case MONEY:
			return Double.valueOf(value);
		case LEVELRANGE:
			return MinMax.valueOf(value);
		case DISGUISEALLAS:
			return value;
		case HEALTH: case HEALTHP:
		case MAGIC: case MAGICP:
		case HUNGER:
		case EXPERIENCE:
		case WITHINDISTANCE:
			return Integer.valueOf(value);
		case INVULNERABLE:
			return Integer.valueOf(value)*20; // multiply by number of ticks per second
		case FLIGHTSPEED:
			return Float.valueOf(value);
		case GAMEMODE:
			GameMode gm = null;
			try {
				gm = GameMode.getByValue(Integer.valueOf(value));
			} catch (Exception e){
				gm = GameMode.valueOf(value.toUpperCase());
			}
			return gm; // multiply by number of ticks per second
		default:
			break;
		}
		return null;
	}
};
