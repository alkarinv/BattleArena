package mc.alk.arena.controllers;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.plugins.McMMOController;
import mc.alk.arena.controllers.plugins.TagAPIController;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.listeners.competition.BlockBreakListener;
import mc.alk.arena.listeners.competition.BlockPlaceListener;
import mc.alk.arena.listeners.competition.DamageListener;
import mc.alk.arena.listeners.competition.HungerListener;
import mc.alk.arena.listeners.competition.ItemDropListener;
import mc.alk.arena.listeners.competition.ItemPickupListener;
import mc.alk.arena.listeners.competition.PlayerMoveListener;
import mc.alk.arena.listeners.competition.PlayerTeleportListener;
import mc.alk.arena.listeners.competition.PotionListener;
import mc.alk.arena.listeners.competition.PreClearInventoryListener;
import mc.alk.arena.listeners.competition.TeamHeadListener;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.regions.ArenaRegion;
import mc.alk.arena.objects.scoreboard.ScoreboardFactory;

public class ListenerAdder {

	public static void addListeners(PlayerHolder holder, MatchTransitions tops) {
		boolean needsDamageEvents = tops.hasAnyOption(TransitionOption.PVPOFF,TransitionOption.PVPON,TransitionOption.INVINCIBLE);
		boolean woolTeams = tops.hasAnyOption(TransitionOption.WOOLTEAMS) && holder.getParams().getMaxTeamSize() >1 ||
				tops.hasAnyOption(TransitionOption.ALWAYSWOOLTEAMS);
		if (woolTeams){
			holder.addArenaListener(new TeamHeadListener());}
		if (needsDamageEvents){
			holder.addArenaListener(new DamageListener(holder));}
		if (tops.hasAnyOption(TransitionOption.NOTELEPORT, TransitionOption.NOWORLDCHANGE, TransitionOption.WGNOENTER)){
			holder.addArenaListener(new PlayerTeleportListener(holder));}
		if (tops.hasAnyOption(TransitionOption.BLOCKBREAKON,TransitionOption.BLOCKBREAKOFF)){
			holder.addArenaListener(new BlockBreakListener(holder));}
		if (tops.hasAnyOption(TransitionOption.BLOCKPLACEON,TransitionOption.BLOCKPLACEOFF)){
			holder.addArenaListener(new BlockPlaceListener(holder));}
		if (tops.hasAnyOption(TransitionOption.ITEMDROPOFF)){
			holder.addArenaListener(new ItemDropListener(holder));}
        if (tops.hasAnyOption(TransitionOption.HUNGEROFF)){
            holder.addArenaListener(new HungerListener(holder));}
		if (tops.hasAnyOption(TransitionOption.ITEMPICKUPOFF)){
			holder.addArenaListener(new ItemPickupListener(holder));}
        if (tops.hasAnyOption(TransitionOption.POTIONDAMAGEON)){
            holder.addArenaListener(new PotionListener(holder));}
        if (McMMOController.enabled() && McMMOController.hasDisabledSkills()){
            holder.addArenaListener(McMMOController.createNewListener());}
        if (tops.hasAnyOption(TransitionOption.WGNOLEAVE)) {
            ArenaRegion region = null;
            if (holder instanceof Match) {
                region = ((Match) holder).getArena().getWorldGuardRegion();
            } else if (holder instanceof Arena) {
                region = ((Arena) holder).getWorldGuardRegion();
            }
            if (region != null && region.valid())
                holder.addArenaListener(new PlayerMoveListener(holder,region));
        }
        if (!ScoreboardFactory.hasBukkitScoreboard() &&
				TagAPIController.enabled() && !tops.hasAnyOption(TransitionOption.NOTEAMNAMECOLOR)){
			holder.addArenaListener(TagAPIController.getNewListener());}
        if (Defaults.PLUGIN_ANTILOOT && tops.hasOptionAt(MatchState.ONDEATH,TransitionOption.CLEARINVENTORY)){
            holder.addArenaListener(new PreClearInventoryListener());
        }
	}

}
