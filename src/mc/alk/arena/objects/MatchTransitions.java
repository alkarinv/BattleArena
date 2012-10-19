package mc.alk.arena.objects;

import java.util.HashMap;
import java.util.List;

import mc.alk.arena.objects.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.inventory.ItemStack;

public class MatchTransitions {
	HashMap<MatchState,TransitionOptions> ops = new HashMap<MatchState,TransitionOptions>();
	
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
		for (MatchState ms : ops.keySet()){
			sb.append(ms +" -- " + ops.get(ms)+"\n");
			List<ItemStack> items = ops.get(ms).getItems();
			if (items != null){
				for (ItemStack item: items){
					sb.append("          item - " + InventoryUtil.getItemString(item));}
			}
		}
		return sb.toString();
	}
}
