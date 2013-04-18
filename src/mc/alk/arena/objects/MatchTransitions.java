package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.InventoryUtil;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class MatchTransitions {
	final Map<MatchState,TransitionOptions> ops = new EnumMap<MatchState,TransitionOptions>(MatchState.class);
	final Set<TransitionOption> allops = new HashSet<TransitionOption>();

	public MatchTransitions() {}
	public MatchTransitions(MatchTransitions o) {
		for (MatchState ms: o.ops.keySet()){
			ops.put(ms, new TransitionOptions(o.ops.get(ms)));
		}
		calculateAllOptions();
	}

	public Map<MatchState,TransitionOptions> getAllOptions(){
		return ops;
	}

	public void addTransitionOptions(MatchState ms, TransitionOptions tops) {
		ops.put(ms, tops);
		Map<TransitionOption,Object> ops = tops.getOptions();
		if (ops != null)
			allops.addAll(ops.keySet());
	}

	public void addTransitionOption(MatchState state, TransitionOption option) throws InvalidOptionException {
		allops.add(option);
		TransitionOptions tops = ops.get(state);
		if (tops == null){
			tops = new TransitionOptions();
			ops.put(state, tops);
		}
		tops.addOption(option);
	}

	public void removeTransitionOptions(MatchState ms) {
		ops.remove(ms);
		calculateAllOptions();
	}

	private void calculateAllOptions(){
		allops.clear();
		for (TransitionOptions top: ops.values()){
			allops.addAll(top.getOptions().keySet());
		}
	}

	public boolean hasAnyOption(TransitionOption... options) {
		for (TransitionOption op: options){
			if (allops.contains(op))
				return true;
		}
		return false;
	}
	public MatchState getMatchState(TransitionOption option) {
		for (MatchState state: ops.keySet()){
			TransitionOptions tops = ops.get(state);
			if (tops.hasOption(option))
				return state;
		}
		return null;
	}

	public boolean hasAllOptions(TransitionOption... options) {
		Set<TransitionOption> ops = new HashSet<TransitionOption>(Arrays.asList(options));
		return allops.containsAll(ops);
	}

	public boolean hasOptionAt(MatchState state, TransitionOption option) {
		TransitionOptions tops = ops.get(state);
		return tops == null ? false : tops.hasOption(option);
	}

	public boolean needsClearInventory() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).clearInventory() : false;
	}

	public String getRequiredString(String header) {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getNotReadyMsg(header): null;
	}

	public String getRequiredString(ArenaPlayer p, World w, String header) {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getNotReadyMsg(p,w,header): null;
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

	public boolean playerReady(ArenaPlayer p, World w) {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).playerReady(p,w): true;
	}

	public boolean teamReady(ArenaTeam t, World w) {
		TransitionOptions to = ops.get(MatchState.PREREQS);
		if (to == null)
			return true;
		for (ArenaPlayer p: t.getPlayers()){
			if (!to.playerReady(p,w))
				return false;
		}
		return true;
	}
	public List<MatchState> getMatchStateRange(TransitionOption startOption, TransitionOption endOption) {
		boolean foundOption = false;
		List<MatchState> list = new ArrayList<MatchState>();
		for (MatchState ms : MatchState.values()){
			TransitionOptions to = ops.get(ms);
			if (to == null) continue;
			if (to.hasOption(startOption)){
				foundOption = true;}
			if (to.hasOption(endOption))
				return list;
			if (foundOption)
				list.add(ms);
		}
		return list;
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
					sb.append(" " + ac.getDisplayName());}
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
