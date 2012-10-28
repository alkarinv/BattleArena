package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.options.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.inventory.ItemStack;

public class MatchTransitions {
	Map<MatchState,TransitionOptions> ops = new EnumMap<MatchState,TransitionOptions>(MatchState.class);

	public MatchTransitions() {}
	public MatchTransitions(MatchTransitions o) {
		for (MatchState ms: o.ops.keySet()){
			ops.put(ms, new TransitionOptions(o.ops.get(ms)));
		}
	}

	public void addTransition(MatchState ms, TransitionOptions tops) {
		ops.put(ms, tops);
	}

	public boolean hasOptions(TransitionOption... options) {
		for (TransitionOption op: options){
			for (TransitionOptions tops : ops.values()){
				if (tops.hasOption(op))
					return true;
			}
		}
		return false;
	}

	public boolean hasOptionAt(MatchState state, TransitionOption option) {
		TransitionOptions tops = ops.get(state);
		return tops == null ? false : tops.hasOption(option);
	}

	public boolean needsClearInventory() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).clearInventory() : false;
	}

	public void removeOptions(MatchState ms) {
		ops.remove(ms);
	}

	public String getRequiredString(String header) {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getNotReadyMsg(header): null;
	}

	public String getRequiredString(ArenaPlayer p, String header) {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getNotReadyMsg(p,header): null;
	}

	public String getGiveString(MatchState ms) {
		return ops.containsKey(ms) ? ops.get(ms).getPrizeMsg(null): null;
	}

	public TransitionOptions getOptions(MatchState ms) {
		return ops.get(ms);
	}

	public Double getEntranceFee() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getMoney() : null;
	}

	public boolean hasEntranceFee() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).hasMoney() : false;
	}

	public boolean playerReady(ArenaPlayer p) {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).playerReady(p): true;
	}

	public boolean teamReady(Team t) {
		TransitionOptions to = ops.get(MatchState.PREREQS);
		if (to == null)
			return true;
		for (ArenaPlayer p: t.getPlayers()){
			if (!to.playerReady(p))
				return false;
		}
		return true;
	}
	public String getOptionString() {
		StringBuilder sb = new StringBuilder();
		List<MatchState> states = new ArrayList<MatchState>(ops.keySet());
		Collections.sort(states);
		for (MatchState ms : states){
			TransitionOptions to = ops.get(ms);
			sb.append(ms +" -- " + to+"\n");
			Map<Integer, ArenaClass> classes = to.getClasses();
			if (classes != null){
				sb.append("             classes - ");
				for (ArenaClass ac : classes.values()){
					sb.append(" " + ac.getPrettyName());}
				sb.append("\n");
			}
			List<ItemStack> items = to.getGiveItems();
			if (items != null){
				sb.append("             items - ");
				for (ItemStack item: items){
					sb.append(" " + InventoryUtil.getItemString(item));}
				sb.append("\n");
			}
			items = to.getNeedItems();
			if (items != null){
				sb.append("             needitems - ");
				for (ItemStack item: items){
					sb.append(" " + InventoryUtil.getItemString(item));}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
