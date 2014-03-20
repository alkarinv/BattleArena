package mc.alk.arena.objects;

import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.InventoryUtil;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StateGraph {
	final Map<CompetitionState,StateOptions> ops = new HashMap<CompetitionState,StateOptions>();
	Set<TransitionOption> allops;

	public StateGraph() {}
	public StateGraph(StateGraph o) {
		for (CompetitionState ms: o.ops.keySet()){
			ops.put(ms, new StateOptions(o.ops.get(ms)));
		}
	}

	public Map<CompetitionState,StateOptions> getAllOptions(){
		return ops;
	}

	public void addTransitionOptions(CompetitionState ms, StateOptions tops) {
		ops.put(ms, tops);
		allops = null;
	}

	public void addTransitionOption(MatchState state, TransitionOption option) throws InvalidOptionException {
		StateOptions tops = ops.get(state);
		if (tops == null){
			tops = new StateOptions();
			ops.put(state, tops);
		}
		tops.addOption(option);
        allops = null;
	}

	public void addTransitionOption(CompetitionState state, TransitionOption option, Object value) throws InvalidOptionException {
		StateOptions tops = ops.get(state);
		if (tops == null){
			tops = new StateOptions();
			ops.put(state, tops);
		}
		tops.addOption(option,value);
        allops = null;
	}

	public boolean removeTransitionOption(CompetitionState state, TransitionOption option) {
		StateOptions tops = ops.get(state);
		return tops != null && tops.removeOption(option) != null;
	}

	public void removeTransitionOptions(CompetitionState ms) {
		ops.remove(ms);
        allops = null;
	}

	private void calculateAllOptions(){
		if (allops != null){
            allops.clear();
        } else {
            allops = new HashSet<TransitionOption>();
        }
        for (StateOptions top: ops.values()){
			allops.addAll(top.getOptions().keySet());
		}
	}

	public boolean hasAnyOption(TransitionOption option) {
        if (allops == null)
            calculateAllOptions();
		return allops.contains(option);
	}

	public boolean hasAnyOption(TransitionOption... options) {
        if (allops == null)
            calculateAllOptions();
		for (TransitionOption op: options){
			if (allops.contains(op))
				return true;
		}
		return false;
	}

	public CompetitionState getMatchState(TransitionOption option) {
		for (CompetitionState state: ops.keySet()){
			StateOptions tops = ops.get(state);
			if (tops.hasOption(option))
				return state;
		}
		return null;
	}

	public boolean hasAllOptions(TransitionOption... options) {
		Set<TransitionOption> ops = new HashSet<TransitionOption>(Arrays.asList(options));
        if (allops == null)
            calculateAllOptions();
        return allops.containsAll(ops);
	}

	public boolean hasInArenaOrOptionAt(CompetitionState state, TransitionOption option) {
		StateOptions tops = ops.get(state);
		return tops == null ? hasOptionAt(MatchState.INARENA,option) : tops.hasOption(option);
	}

	public boolean hasOptionAt(CompetitionState state, TransitionOption option) {
		StateOptions tops = ops.get(state);
		return tops != null && tops.hasOption(option);
	}

    /**
     * Use the newer more generic
     * public boolean hasOptionAt(CompetitionState state, TransitionOption option) {
     * @param state MatchState
     * @param option TransitionOption
     * @return true or false
     */
    @Deprecated
    public boolean hasOptionAt(MatchState state, TransitionOption option) {
        return hasOptionAt((CompetitionState) state, option);
    }

	public boolean hasOptionIn(MatchState beginState, MatchState endState, TransitionOption option) {
		List<MatchState> states = MatchState.getStates(beginState, endState);
		for (MatchState state : states){
			StateOptions tops = ops.get(state);
			if (tops != null && tops.hasOption(option))
				return true;
		}
		return false;
	}

	@SuppressWarnings("SimplifiableConditionalExpression")
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

	public StateOptions getOptions(CompetitionState ms) {
		return ops.get(ms);
	}

	public Double getEntranceFee() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getMoney() : null;
	}

    public boolean hasEntranceFee() {
		return ops.containsKey(MatchState.PREREQS) && ops.get(MatchState.PREREQS).hasMoney();
	}

	public boolean playerReady(ArenaPlayer p, World w) {
		return !ops.containsKey(MatchState.PREREQS) || ops.get(MatchState.PREREQS).playerReady(p, w);
	}

	public boolean teamReady(ArenaTeam t, World w) {
		StateOptions to = ops.get(MatchState.PREREQS);
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
			StateOptions to = ops.get(ms);
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

    class CStateComparator implements Comparator<CompetitionState> {
        @Override
        public int compare(CompetitionState o1, CompetitionState o2) {
            return o1.globalOrdinal() - o2.globalOrdinal();
        }
    }
    private ChatColor getColor(Object o) {
        return o == null ? ChatColor.GOLD : ChatColor.WHITE;
    }
    public String getOptionString(StateGraph subset) {
        if (subset == null) {
            subset = new StateGraph();
        }
        StringBuilder sb = new StringBuilder();
        List<CompetitionState> states = new ArrayList<CompetitionState>(ops.keySet());
        List<CompetitionState> states2 = new ArrayList<CompetitionState>(subset.ops.keySet());
        Collections.sort(states, new CStateComparator());
        Collections.sort(states2, new CStateComparator());

        for (CompetitionState ms : states){
            StateOptions to = ops.get(ms);
            StateOptions to2 = subset.ops.get(ms);
            sb.append(ms).append(" -- ");
            sb.append(to.getOptionString(to2)).append("\n");
            Map<Integer, ArenaClass> classes = to.getClasses();
            if (classes != null){
                sb.append("             classes - ");
                for (ArenaClass ac : classes.values()){
                    sb.append(" ").append(ac.getDisplayName());}
                sb.append("\n");
            }
            List<ItemStack> items = to.getGiveItems();
            if (items != null){
                sb.append("             items - ");
                for (ItemStack item: items){
                    sb.append(" ").append(InventoryUtil.getItemString(item));}
                sb.append("\n");
            }
            items = to.getNeedItems();
            if (items != null){
                sb.append("             needitems - ");
                for (ItemStack item: items){
                    sb.append(" ").append(InventoryUtil.getItemString(item));}
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String getOptionString() {
        return getOptionString(null);
    }

	public Double getDoubleOption(MatchState state, TransitionOption option) {
		StateOptions tops = getOptions(state);
		return tops == null ? null : tops.getDouble(option);
	}

	public static StateGraph mergeChildWithParent(StateGraph cmt, StateGraph pmt) {
        if (cmt == null && pmt == null)
            return null;
        if (cmt == null){
			cmt = new StateGraph();}
		if (pmt == null)
			return cmt;
		for (Entry<CompetitionState, StateOptions> entry: pmt.ops.entrySet()){
            if (cmt.ops.containsKey(entry.getKey())){
                cmt.ops.get(entry.getKey()).addOptions(entry.getValue());
            } else {
                cmt.ops.put(entry.getKey(), new StateOptions(entry.getValue()));
            }
		}
		cmt.calculateAllOptions();
		return cmt;
	}

    public void deleteOptions(CompetitionState state) {
        ops.remove(state);
        calculateAllOptions();
    }
}

