package mc.alk.arena.objects;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.util.MinMax;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.Entry;


public class ArenaParams {
    ArenaType arenaType;
    Boolean rated;

    String name;
    String displayName;
    String cmd;

    Integer timeBetweenRounds;
    Integer secondsTillMatch;
    Integer matchTime;
    Integer secondsToLoot;

    Integer forceStartTime;

    StateGraph stateGraph;
    StateGraph mergedStateGraph;
    String dbName;

    ArenaParams parent;
    MinMax nTeams;
    MinMax teamSize;
    Boolean closeWaitroomWhileRunning;
    Boolean cancelIfNotEnoughPlayers;
    Integer arenaCooldown;
    Integer allowedTeamSizeDifference;
    Integer nLives;

    private Map<Integer, MatchParams> teamParams;

    public ArenaParams() {}

    public ArenaParams(ArenaType at) {
        this.arenaType = at;
    }

    public ArenaParams(ArenaParams ap) {
        this(ap.getType());
        this.copy(ap);
    }

    public void copy(ArenaParams ap){
        if (this == ap)
            return;
        this.arenaType = ap.arenaType;
        this.rated = ap.rated;
        this.cmd = ap.cmd;
        this.name = ap.name;
        this.timeBetweenRounds = ap.timeBetweenRounds;
        this.secondsTillMatch = ap.secondsTillMatch;
        this.secondsToLoot = ap.secondsToLoot;
        this.dbName = ap.dbName;
        this.closeWaitroomWhileRunning = ap.closeWaitroomWhileRunning;
        this.cancelIfNotEnoughPlayers= ap.cancelIfNotEnoughPlayers;
        this.arenaCooldown = ap.arenaCooldown;
        this.allowedTeamSizeDifference = ap.allowedTeamSizeDifference;
        this.matchTime = ap.matchTime;
        this.forceStartTime = ap.forceStartTime;
        this.nLives = ap.nLives;
        this.displayName = ap.displayName;
        this.mergedStateGraph = null;
        if (ap.stateGraph != null)
            this.stateGraph = new StateGraph(ap.stateGraph);
        if (ap.nTeams != null)
            this.nTeams = new MinMax(ap.nTeams);
        if (ap.teamSize != null)
            this.teamSize = new MinMax(ap.teamSize);
        this.teamParams = ap.teamParams;
        this.parent = ap.parent;
    }

    public void flatten() {
        if (parent == null){
            return;}
        if (this.arenaType == null) this.arenaType = parent.getType();
        if (this.rated == null) this.rated = parent.isRated();
        if (this.cmd == null) this.cmd = parent.getCommand();
        if (this.name == null) this.name = parent.getName();
        if (this.timeBetweenRounds == null) this.timeBetweenRounds = parent.getTimeBetweenRounds();
        if (this.secondsTillMatch == null) this.secondsTillMatch = parent.getSecondsTillMatch();
        if (this.matchTime == null) this.matchTime = parent.getMatchTime();
        if (this.forceStartTime== null) this.forceStartTime = parent.getForceStartTime();
        if (this.secondsToLoot == null) this.secondsToLoot = parent.getSecondsToLoot();
        if (this.dbName == null) this.dbName = parent.getDBName();
        if (this.nLives == null) this.nLives = parent.getNLives();
        if (this.closeWaitroomWhileRunning == null) this.closeWaitroomWhileRunning = parent.isWaitroomClosedWhenRunning();
        if (this.cancelIfNotEnoughPlayers == null) this.cancelIfNotEnoughPlayers = parent.isCancelIfNotEnoughPlayers();
        if (this.arenaCooldown== null) this.arenaCooldown = parent.getArenaCooldown();
        if (this.allowedTeamSizeDifference== null) this.allowedTeamSizeDifference= parent.getAllowedTeamSizeDifference();
        if (this.displayName == null) this.displayName = parent.getDisplayName();
        this.stateGraph = mergeChildWithParent(this, parent);
        if (this.nTeams == null && parent.getNTeams()!=null) this.nTeams = new MinMax(parent.getNTeams());
        if (this.teamSize == null && parent.getTeamSizes() !=null) this.teamSize = new MinMax(parent.getTeamSizes());

        if (this.teamParams != null && parent.getTeamParams() != null) {
            HashMap<Integer,MatchParams> tp = new HashMap<Integer,MatchParams>(this.teamParams);
            tp.putAll(parent.getTeamParams());
            this.parent = null;
            this.teamParams = null;
            for (Entry<Integer, MatchParams> e : tp.entrySet()) {
                MatchParams ap = ParamController.copyParams(e.getValue());
                ap.setParent(this);
                ap.flatten();
                tp.put(e.getKey(), ap);
            }
            this.teamParams = tp;
        } else if (parent.getTeamParams() != null){
            HashMap<Integer,MatchParams> tp =  new HashMap<Integer,MatchParams>(parent.getTeamParams());
            this.parent = null;
            this.teamParams = null;
            for (Entry<Integer, MatchParams> e : tp.entrySet()) {
                MatchParams ap = ParamController.copyParams(e.getValue());
                ap.setParent(this);
                ap.flatten();
                tp.put(e.getKey(), ap);
            }
            this.teamParams = tp;
        } else if (this.teamParams != null){
            HashMap<Integer,MatchParams> tp =  new HashMap<Integer,MatchParams>(this.teamParams);
            this.parent = null;
            this.teamParams = null;
            for (Entry<Integer, MatchParams> e : tp.entrySet()) {
                MatchParams ap = ParamController.copyParams(e.getValue());
                ap.setParent(this);
                ap.flatten();
                tp.put(e.getKey(), ap);
            }
            this.teamParams = tp;
        }
        this.mergedStateGraph = this.stateGraph;
        this.parent = null;
    }

    private StateGraph mergeChildWithParent(ArenaParams cap, ArenaParams pap) {
        StateGraph mt = cap.stateGraph == null ? new StateGraph() : new StateGraph(cap.stateGraph);
        if (pap != null) {
            StateGraph.mergeChildWithParent(mt, mergeChildWithParent(pap, pap.parent));
        }
        return mt;
    }

    public StateGraph getThisTransitionOptions(){
        return stateGraph;
    }

    public void setTransitionOptions(StateGraph transitionOptions) {
        this.stateGraph = transitionOptions;
        clearMerged();
    }

    public MinMax getThisTeamSize() {
        return teamSize;
    }

    public MinMax getThisNTeams() {
        return nTeams;
    }

    public String getPlayerRange() {
        return ArenaSize.rangeString(getMinPlayers(),getMaxPlayers());
    }

    public ArenaType getType() {return arenaType;}

    public void setType(ArenaType type) {this.arenaType = type;}

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean intersect(ArenaParams params) {
        if (!getType().matches(params.getType()))
            return false;
        if (getNTeams() != null && params.getNTeams() != null && !getNTeams().intersect(params.getNTeams())) {
            return false;}
        return (this.getTeamSizes() != null && params.getTeamSizes() != null &&
                !getTeamSizes().intersect(params.getTeamSizes()));
    }

    public boolean matches(final JoinOptions jo){
        return this.matches(jo.getMatchParams());
    }

    public boolean matches(final ArenaParams ap) {
        if (arenaType != null && ap.arenaType != null &&
                arenaType.matches(ap.arenaType)){
            MinMax nt = getNTeams();
            MinMax ts = getTeamSizes();
            if (nt == null || ts == null)
                return true;
            MinMax nt2 = ap.getNTeams();
            MinMax ts2 = ap.getTeamSizes();
            return nt2 == null || ts2 == null || nt.intersect(nt2) && ts.intersect(ts2);
        }
        return false;
    }

    public Collection<String> getInvalidMatchReasons(ArenaParams ap) {
        List<String> reasons = new ArrayList<String>();
        if (arenaType == null) reasons.add("ArenaType is null");
        if (ap.arenaType == null) reasons.add("Passed params have an arenaType of null");
        else reasons.addAll(arenaType.getInvalidMatchReasons(ap.getType()));
        if (getNTeams() != null && ap.getNTeams() != null && !getNTeams().intersect(ap.getNTeams())){
            reasons.add("Arena accepts nteams="+getNTeams()+". you requested "+ap.getNTeams());
        }
        if (getTeamSizes() != null && ap.getTeamSizes() != null && !getTeamSizes().intersect(ap.getTeamSizes())){
            reasons.add("Arena accepts teamSize="+getTeamSizes()+". you requested "+ap.getTeamSizes());
        }
        return reasons;
    }

    public boolean valid() {
        return (arenaType != null &&
                (nTeams == null || nTeams.valid()) && (teamSize == null || teamSize.valid()));
    }

    public Collection<String> getInvalidReasons() {
        List<String> reasons = new ArrayList<String>();
        if (arenaType == null) reasons.add("ArenaType is null");
        if (nTeams != null && !nTeams.valid()){
            reasons.add("Min Teams is greater than Max Teams " + nTeams.min + ":" + nTeams.max);}
        if (teamSize != null && !teamSize.valid()){
            reasons.add("Min Team Size is greater than Max Team Size " + teamSize.min + ":" + teamSize.max);}
        return reasons;
    }

    public String getCommand() {
        return cmd != null ? cmd :
                (parent != null ? parent.getCommand() : null);

    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }
    public Boolean isRated(){
        return rated != null ? rated : (parent != null ? parent.isRated() : null);
    }

    public void setSecondsToLoot(Integer i) {
        secondsToLoot=i;
    }

    public Integer getSecondsToLoot() {
        return secondsToLoot != null ? secondsToLoot :
                (parent != null ? parent.getSecondsToLoot() : null);
    }

    public void setSecondsTillMatch(Integer i) {
        secondsTillMatch=i;
    }

    public Integer getSecondsTillMatch() {
        return secondsTillMatch != null ? secondsTillMatch :
                (parent != null ? parent.getSecondsTillMatch() : null);
    }

    public void setTimeBetweenRounds(Integer i) {
        timeBetweenRounds=i;
    }

    public Integer getTimeBetweenRounds() {
        return timeBetweenRounds != null ? timeBetweenRounds :
                (parent != null ? parent.getTimeBetweenRounds() : null);
    }

    public void setDBName(String dbName) {
        this.dbName = dbName;
    }
    public String getDBName(){
        return dbName != null ? dbName :
                (parent != null ? parent.getDBName() : null);
    }

    public String getName() {
        return name != null ? name :
                (parent != null ? parent.getName() : null);
    }
    public void setName(String name) {
        this.name = name;
    }

    public String toPrettyString() {
        return  getDisplayName()+":"+arenaType+",nteams="+getNTeams()+",teamSize="+getTeamSizes();
    }

    private ChatColor getColor(Object o) {
        return o == null ? ChatColor.GOLD : ChatColor.WHITE;
    }

    public String toSummaryString() {
        return  "&2&f"+name+"&2:&f"+arenaType+
                "&2,nteams="+getColor(nTeams) +getNTeams()+
                "&2,teamSize="+getColor(teamSize)+getTeamSizes() +"\n"+
                "&5forceStartTime="+getColor(forceStartTime)+getForceStartTime()+
                "&5, timeUntilMatch="+getColor(secondsTillMatch)+getSecondsTillMatch() +
                "&5, matchTime="+getColor(matchTime)+getMatchTime()+
                "&5, secondsToLoot="+getColor(secondsToLoot)+getSecondsToLoot()+"\n"+
                "&crated="+getColor(rated)+isRated()+"&c, nLives="+getColor(nLives)+getNLives()+"&e";
    }

    public String getOptionsSummaryString() {
        return getStateGraph().getOptionString(stateGraph);
    }

    @Override
    public String toString(){
        return  name+":"+arenaType +",nteams="+
                getNTeams()+",teamSize="+getTeamSizes() +" options=\n"+
                (getThisTransitionOptions()==null ? "" : getThisTransitionOptions().getOptionString());
    }

    public boolean isDuelOnly() {
        return getStateGraph().hasOptionAt(MatchState.DEFAULTS, TransitionOption.DUELONLY);
    }

    public boolean isAlwaysOpen(){
        return getStateGraph().hasOptionAt(MatchState.DEFAULTS, TransitionOption.ALWAYSOPEN);
    }

    public void setParent(ArenaParams parent) {
        this.parent=parent;
        clearMerged();
    }

    protected void clearMerged() {
        this.mergedStateGraph = null;
        if (teamParams!=null) {
            for (MatchParams mp: teamParams.values()){
                mp.clearMerged();
            }
        }
    }

    public ArenaParams getParent() {
        return parent;
    }

    @SuppressWarnings("ConstantConditions")
    public Integer getMinTeamSize() {
        return teamSize != null ? teamSize.min : (parent != null ? parent.getMinTeamSize() : null);
    }

    @SuppressWarnings("ConstantConditions")
    public Integer getMaxTeamSize() {
        return teamSize != null ? teamSize.max : (parent != null ? parent.getMaxTeamSize() : null);
    }

    @SuppressWarnings("ConstantConditions")
    public Integer getMinTeams() {
        return nTeams != null ? nTeams.min : (parent != null ? parent.getMinTeams() : null);
    }

    @SuppressWarnings("ConstantConditions")
    public Integer getMaxTeams() {
        return nTeams != null ? nTeams.max : (parent != null ? parent.getMaxTeams() : null);
    }

    public Integer getMaxPlayers() {
        MinMax nt = getNTeams();
        MinMax ts = getTeamSizes();
        if (nt==null || ts == null)
            return null;
        return nt.max == ArenaSize.MAX || ts.max == ArenaSize.MAX ? ArenaSize.MAX : nt.max * ts.max;
    }

    public Integer getMinPlayers() {
        MinMax nt = getNTeams();
        MinMax ts = getTeamSizes();
        if (nt==null || ts == null)
            return null;
        return nt.min == ArenaSize.MAX || ts.min == ArenaSize.MAX ? ArenaSize.MAX : nt.min * ts.min;
    }

    public void setTeamSize(Integer n) {
        if (n == null){
            teamSize = null;
        } else {
            if (teamSize == null){
                teamSize = new MinMax(n);
            } else {
                teamSize.min = n;
                teamSize.max = n;
            }
        }
    }

    public void setNTeams(MinMax mm) {
        if (mm == null){
            nTeams = null;
        } else {
            if (nTeams == null){
                nTeams = new MinMax(mm);
            } else {
                nTeams.min = mm.min;
                nTeams.max = mm.max;
            }
        }
    }

    /**
     * @return MinMax representing the number of teams
     */
    public MinMax getNTeams(){
        return nTeams != null ? nTeams : (parent != null ? parent.getNTeams() : null);
    }

    /**
     * @return MinMax representing the team sizes
     */
    public MinMax getTeamSizes(){
        return teamSize != null ? teamSize : (parent != null ? parent.getTeamSizes() : null);
    }

    public void setTeamSizes(MinMax mm) {
        if (mm == null){
            teamSize = null;
        } else {
            if (teamSize == null){
                teamSize = new MinMax(mm);
            } else {
                teamSize.min = mm.min;
                teamSize.max = mm.max;
            }
        }
    }

    public void setMinTeamSize(int n) {
        if (teamSize == null){ teamSize = new MinMax(n);}
        else teamSize.min = n;
    }
    public void setMaxTeamSize(int n) {
        if (teamSize == null){ teamSize = new MinMax(n);}
        else teamSize.max = n;
    }
    public void setMinTeams(int n) {
        if (nTeams == null){ nTeams = new MinMax(n);}
        else nTeams.min = n;
    }
    public void setMaxTeams(int n) {
        if (nTeams == null){ nTeams = new MinMax(n);}
        else nTeams.max = n;
    }

    public boolean matchesTeamSize(int i) {
        return teamSize != null ? teamSize.contains(i) : (parent != null && parent.matchesTeamSize(i));
    }

    public boolean hasOptionAt(CompetitionState state, TransitionOption op) {
        return getStateGraph().hasOptionAt(state, op);
    }

    public boolean hasEntranceFee() {
        return hasOptionAt(MatchState.PREREQS,TransitionOption.MONEY);
    }

    public Double getEntranceFee(){
        return getDoubleOption(MatchState.PREREQS, TransitionOption.MONEY);
    }

    public Double getDoubleOption(MatchState state, TransitionOption option){
        return getStateGraph().getDoubleOption(state, option);
    }

    public boolean hasAnyOption(TransitionOption option) {
        return getStateGraph().hasAnyOption(option);
    }

    public List<ItemStack> getWinnerItems() {
        return getGiveItems(MatchState.WINNERS);
    }

    public List<ItemStack> getLoserItems() {
        return getGiveItems(MatchState.LOSERS);
    }

    public List<ItemStack> getGiveItems(MatchState state) {
        StateOptions tops = getStateOptions(state);
        return (tops != null && tops.hasOption(TransitionOption.GIVEITEMS)) ? tops.getGiveItems() : null;
    }

    public List<PotionEffect> getEffects(MatchState state) {
        StateOptions tops = getStateOptions(state);
        return (tops != null && tops.hasOption(TransitionOption.ENCHANTS)) ? tops.getEffects() : null;
    }

    public boolean needsWaitroom() {
        return getStateGraph().hasAnyOption(TransitionOption.TELEPORTMAINWAITROOM, TransitionOption.TELEPORTWAITROOM);
    }

    public boolean needsSpectate() {
        return getStateGraph().hasAnyOption(TransitionOption.TELEPORTSPECTATE);
    }

    public boolean needsLobby() {
        return getStateGraph().hasAnyOption(TransitionOption.TELEPORTMAINLOBBY, TransitionOption.TELEPORTLOBBY);
    }

    public Boolean isWaitroomClosedWhenRunning(){
        return closeWaitroomWhileRunning != null ? closeWaitroomWhileRunning :
                (parent != null ? parent.isWaitroomClosedWhenRunning() : null);
    }

    public void setWaitroomClosedWhileRunning(Boolean value) {
        this.closeWaitroomWhileRunning = value;
    }

    public Boolean isCancelIfNotEnoughPlayers(){
        return cancelIfNotEnoughPlayers != null ? cancelIfNotEnoughPlayers :
                (parent != null ? parent.isCancelIfNotEnoughPlayers() : null);
    }

    public void setCancelIfNotEnoughPlayers(Boolean value) {
        this.cancelIfNotEnoughPlayers = value;
    }

    public String getDisplayName() {
        return displayName != null ? displayName :
                (parent != null ? parent.getDisplayName() : this.getName());
    }

    public String getThisDisplayName() {
        return displayName;
    }

    public int getQueueCount() {
        return BattleArena.getBAController().getArenaMatchQueue().getQueueCount(this);
    }

    public void setArenaCooldown(int cooldown) {
        this.arenaCooldown = cooldown;
    }

    public Integer getArenaCooldown() {
        return arenaCooldown != null ? arenaCooldown : (parent != null ? parent.getArenaCooldown(): null);
    }

    public void setAllowedTeamSizeDifference(int difference) {
        this.allowedTeamSizeDifference = difference;
    }

    public Integer getAllowedTeamSizeDifference() {
        return allowedTeamSizeDifference != null ? allowedTeamSizeDifference :
                (parent != null ? parent.getAllowedTeamSizeDifference(): null);
    }

    public Integer getForceStartTime() {
        return forceStartTime != null ? forceStartTime : (parent!= null ? parent.getForceStartTime() : null);
    }
    public void setForceStartTime(Integer forceStartTime) {
        this.forceStartTime = forceStartTime;
    }

    public Integer getMatchTime() {
        return matchTime == null && parent!=null ? parent.getMatchTime() : matchTime;
    }

    public void setMatchTime(Integer matchTime) {
        this.matchTime = matchTime;
    }
    public void setNLives(Integer nlives){
        this.nLives = nlives;
    }

    public Integer getNLives() {
        return nLives == null && parent != null ? parent.getNLives() : nLives;
    }

    public Map<Integer, MatchParams> getTeamParams() {
        return teamParams == null && parent != null ? parent.getTeamParams() : teamParams;
    }

    public MatchParams getTeamParams(int index) {
        if (teamParams!=null) {
            MatchParams mp = teamParams.get(index);
            if (mp != null) {
                return mp;}
        }
        if (parent != null) {
            return parent.getTeamParams(index);
        }
        return null;
    }

    public Map<Integer, MatchParams> getThisTeamParams() {
        return teamParams;
    }

    public void setTeamParams(Map<Integer, MatchParams> teamParams) {
        this.teamParams = teamParams;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public StateGraph getTransitionOptions() {
        return getStateGraph();
    }

    public StateGraph getStateGraph(){
        if (mergedStateGraph != null)
            return mergedStateGraph;
        if (stateGraph == null && parent!=null) {
            /// this is a bit hard to keep synced, but worth it for the speed improvements
            mergedStateGraph = parent.getStateGraph();
        } else {
            mergedStateGraph = mergeChildWithParent(this, this.parent);
        }

        return mergedStateGraph;
    }

    public StateOptions getStateOptions(CompetitionState state) {
        return getStateGraph().getOptions(state);
    }
}
