package mc.alk.arena.controllers;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.plugins.TagAPIController;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.listeners.competition.BlockBreakListener;
import mc.alk.arena.listeners.competition.BlockPlaceListener;
import mc.alk.arena.listeners.competition.DamageListener;
import mc.alk.arena.listeners.competition.ItemDropListener;
import mc.alk.arena.listeners.competition.ItemPickupListener;
import mc.alk.arena.listeners.competition.PlayerTeleportListener;
import mc.alk.arena.listeners.competition.PotionListener;
import mc.alk.arena.listeners.competition.PreClearInventoryListener;
import mc.alk.arena.listeners.competition.TeamHeadListener;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;

public class ListenerAdder {

	public static void addListeners(PlayerHolder match, MatchTransitions tops) {
		boolean needsDamageEvents = tops.hasAnyOption(TransitionOption.PVPOFF,TransitionOption.PVPON,TransitionOption.INVINCIBLE);
		boolean woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS) && match.getParams().getMaxTeamSize() >1 ||
				tops.hasAnyOption(TransitionOption.ALWAYSWOOLTEAMS);
		if (woolTeams){
			match.addArenaListener(new TeamHeadListener());}
		if (needsDamageEvents){
			match.addArenaListener(new DamageListener(match));}
		if (tops.hasAnyOption(TransitionOption.NOTELEPORT, TransitionOption.NOWORLDCHANGE, TransitionOption.WGNOENTER)){
			match.addArenaListener(new PlayerTeleportListener(match));}
		if (tops.hasAnyOption(TransitionOption.BLOCKBREAKON,TransitionOption.BLOCKBREAKOFF)){
			match.addArenaListener(new BlockBreakListener(match));}
		if (tops.hasAnyOption(TransitionOption.BLOCKPLACEON,TransitionOption.BLOCKPLACEOFF)){
			match.addArenaListener(new BlockPlaceListener(match));}
		if (tops.hasAnyOption(TransitionOption.ITEMDROPOFF)){
			match.addArenaListener(new ItemDropListener(match));}
		if (tops.hasAnyOption(TransitionOption.ITEMPICKUPOFF)){
			match.addArenaListener(new ItemPickupListener(match));}
		if (tops.hasAnyOption(TransitionOption.POTIONDAMAGEON)){
			match.addArenaListener(new PotionListener(match));}
		if (!ScoreboardFactory.hasBukkitScoreboard() &&
				TagAPIController.enabled() && !tops.hasAnyOption(TransitionOption.NOTEAMNAMECOLOR)){
			match.addArenaListener(TagAPIController.getNewListener());}
        if (Defaults.PLUGIN_ANTILOOT && tops.hasOptionAt(MatchState.ONDEATH,TransitionOption.CLEARINVENTORY)){
            match.addArenaListener(new PreClearInventoryListener());
        }
	}

}
