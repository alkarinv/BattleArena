package mc.alk.arena.objects;

import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.InventoryUtil;
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
import java.util.Set;

public class MatchTransitions {
	final Map<CompetitionState,TransitionOptions> ops = new HashMap<CompetitionState,TransitionOptions>();
	Set<TransitionOption> allops;

	public MatchTransitions() {}
	public MatchTransitions(MatchTransitions o) {
		for (CompetitionState ms: o.ops.keySet()){
			ops.put(ms, new TransitionOptions(o.ops.get(ms)));
		}
	}

	public Map<CompetitionState,TransitionOptions> getAllOptions(){
		return ops;
	}

	public void addTransitionOptions(CompetitionState ms, TransitionOptions tops) {
		ops.put(ms, tops);
		Map<TransitionOption,Object> ops = tops.getOptions();
		allops = null;
	}

	public void addTransitionOption(MatchState state, TransitionOption option) throws InvalidOptionException {
		TransitionOptions tops = ops.get(state);
		if (tops == null){
			tops = new TransitionOptions();
			ops.put(state, tops);
		}
		tops.addOption(option);
        allops = null;
	}

	public void addTransitionOption(CompetitionState state, TransitionOption option, Object value) throws InvalidOptionException {
		TransitionOptions tops = ops.get(state);
		if (tops == null){
			tops = new TransitionOptions();
			ops.put(state, tops);
		}
		tops.addOption(option,value);
        allops = null;
	}

	public boolean removeTransitionOption(CompetitionState state, TransitionOption option) {
		TransitionOptions tops = ops.get(state);
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
        for (TransitionOptions top: ops.values()){
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
			TransitionOptions tops = ops.get(state);
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
		TransitionOptions tops = ops.get(state);
		return tops == null ? hasOptionAt(MatchState.INARENA,option) : tops.hasOption(option);
	}

	public boolean hasOptionAt(CompetitionState state, TransitionOption option) {
		TransitionOptions tops = ops.get(state);
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
			TransitionOptions tops = ops.get(state);
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

	public TransitionOptions getOptions(CompetitionState ms) {
		return ops.get(ms);
	}

	public Double getEntranceFee() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).getMoney() : null;
	}

    @SuppressWarnings("SimplifiableConditionalExpression")
    public boolean hasEntranceFee() {
		return ops.containsKey(MatchState.PREREQS) ? ops.get(MatchState.PREREQS).hasMoney() : false;
	}

    @SuppressWarnings("SimplifiableConditionalExpression")
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
		List<CompetitionState> states = new ArrayList<CompetitionState>(ops.keySet());
		Collections.sort(states, new Comparator<CompetitionState>() {
            @Override
            public int compare(CompetitionState o1, CompetitionState o2) {
                return o1.globalOrdinal() - o2.globalOrdinal();
            }
        });

		for (CompetitionState ms : states){
			TransitionOptions to = ops.get(ms);
			sb.append(ms).append(" -- ").append(to).append("\n");
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

	public Double getDoubleOption(MatchState state, TransitionOption option) {
		TransitionOptions tops = getOptions(state);
		return tops == null ? null : tops.getDouble(option);
	}

	public static MatchTransitions mergeChildWithParent(MatchTransitions cmt, MatchTransitions pmt) {
        if (cmt == null && pmt == null)
            return null;
        if (cmt == null){
			cmt = new MatchTransitions();}
		if (pmt == null)
			return cmt;
		for (CompetitionState ms: pmt.ops.keySet()){
            if (cmt.ops.containsKey(ms))
                continue;
			cmt.ops.put(ms, new TransitionOptions(pmt.ops.get(ms)));
		}
		cmt.calculateAllOptions();
		return cmt;
	}

    public void deleteOptions(CompetitionState state) {
        ops.remove(state);
        calculateAllOptions();
    }
}

