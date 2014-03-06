package mc.alk.arena.objects;

import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.containers.GameManager;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.MessageUtil;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MatchParams extends ArenaParams implements Comparable<MatchParams>{

    String prefix;
    String signDisplayName;
    VictoryType vc;
    Integer intervalTime;
    AnnouncementOptions ao;

    Integer numConcurrentCompetitions;
    Set<ArenaModule> modules;
    Boolean useBTPvP;
    Boolean useBTMessages;
    Boolean useBTTeamRating;

    MatchParams mparent;


    public MatchParams(){
        super();
    }

    public MatchParams(ArenaType at) {
        super(at);
    }

    public MatchParams(MatchParams mp) {
        super(mp);
    }

    public void copy(ArenaParams ap){
        if (this==ap)
            return;
        super.copy(ap);
        if (ap instanceof MatchParams){
            MatchParams mp = (MatchParams)ap;
            this.prefix = mp.prefix;
            this.vc = mp.vc;

            this.intervalTime = mp.intervalTime;
            this.ao = mp.ao;
            this.numConcurrentCompetitions = mp.numConcurrentCompetitions;
            this.mparent = mp.mparent;
            this.useBTMessages = mp.useBTMessages;
            this.useBTPvP = mp.useBTPvP;
            this.useBTTeamRating  = mp.useBTTeamRating;
            this.signDisplayName = mp.signDisplayName;
            if (mp.modules != null)
                this.modules = new HashSet<ArenaModule>(mp.modules);
        }

    }
    @Override
    public void flatten() {
        if (mparent != null){
            if (this.prefix == null) this.prefix = mparent.getPrefix();
            if (this.vc == null) this.vc = mparent.getVictoryType();
            if (this.intervalTime == null) this.intervalTime = mparent.getIntervalTime();
            if (this.ao == null) this.ao = mparent.getAnnouncementOptions();
            if (this.numConcurrentCompetitions == null) this.numConcurrentCompetitions = mparent.getNConcurrentCompetitions();
            if (this.useBTMessages == null) this.useBTMessages = mparent.getUseTrackerMessages();
            if (this.useBTPvP == null) this.useBTPvP = mparent.getUseTrackerPvP();
            if (this.useBTTeamRating == null) this.useBTTeamRating = mparent.isTeamRating();
            if (this.signDisplayName== null) this.signDisplayName = mparent.getSignDisplayName();
            this.modules = getModules();
            this.mparent = null;
        }
        super.flatten();
    }

    public void setVictoryType(VictoryType type){this.vc = type;}

    public VictoryType getVictoryType() {
        return vc == null && mparent!=null ? mparent.getVictoryType() : vc;
    }

    public String getPrefix(){
        return prefix == null && mparent!=null ? mparent.getPrefix() : prefix;
    }

    public String getSignDisplayName(){
        return signDisplayName == null && mparent!=null ? mparent.getSignDisplayName() : signDisplayName;
    }

    public void setSignDisplayName(String signDisplayName) {
        this.signDisplayName = signDisplayName;
    }

    public void setPrefix(String str){prefix = str;}

    public void setCommand(String str){cmd = str;}

    public int compareTo(MatchParams other) {
        Integer hash = this.hashCode();
        return hash.compareTo(other.hashCode());
    }


    public void setVictoryCondition(VictoryType victoryCondition) {
        this.vc = victoryCondition;
    }

    public Integer getIntervalTime() {
        return intervalTime ==null && mparent!=null ? mparent.getIntervalTime() : intervalTime;
    }

    public void setIntervalTime(Integer intervalTime) {
        this.intervalTime = intervalTime;
    }



    @Override
    public int hashCode() {
        return ((arenaType.ordinal()) << 27);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof MatchParams && this.hashCode() == other.hashCode());
    }

    public void setAnnouncementOptions(AnnouncementOptions announcementOptions) {
        this.ao = announcementOptions;
    }

    public AnnouncementOptions getAnnouncementOptions() {
        return ao;
    }

    @Override
    public String toString(){
        return super.toString()+",vc=" + vc;
    }

    public ChatColor getColor() {
        return MessageUtil.getFirstColor(prefix);
    }

    public void setNConcurrentCompetitions(int number) {
        this.numConcurrentCompetitions = number;
    }

    public Integer getNConcurrentCompetitions(){
        return numConcurrentCompetitions != null ? numConcurrentCompetitions :
                (mparent!=null ? mparent.getNConcurrentCompetitions() : null);
    }

    public JoinType getJoinType() {
        return JoinType.QUEUE;
    }

    public void addModule(ArenaModule am) {
        if (modules == null)
            modules = new HashSet<ArenaModule>();
        modules.add(am);
    }

    public Set<ArenaModule> getModules() {
        Set<ArenaModule> ms  = modules == null ? new HashSet<ArenaModule>() : new HashSet<ArenaModule>(modules);

        if (mparent != null) {
            ms.addAll(mparent.getModules());
        }
        return ms;
    }

    public void setUseTrackerPvP(Boolean enable) {
        useBTPvP = enable;
    }

    public Boolean getUseTrackerPvP() {
        return useBTPvP != null ? useBTPvP : (mparent!= null ? mparent.getUseTrackerPvP() : null);
    }


    public Boolean getUseTrackerMessages() {
        return useBTMessages != null ? useBTMessages : (mparent!= null ? mparent.getUseTrackerMessages() : null);
    }

    public void setUseTrackerMessages(Boolean enable) {
        useBTMessages = enable;
    }


    @Override
    public boolean valid() {
        return super.valid() && (getTransitionOptions() == null ||
                        (!getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY) ||
                                RoomController.hasLobby(getType())));
    }

    @Override
    public Collection<String> getInvalidReasons() {
        List<String> reasons = new ArrayList<String>();
        if (getTransitionOptions() != null &&
                getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY) && !RoomController.hasLobby(getType()))
            reasons.add("Needs a Lobby");
        reasons.addAll(super.getInvalidReasons());
        return reasons;
    }

    @Override
    public void setParent(ArenaParams parent) {
        super.setParent(parent);
        if (parent != null && parent instanceof MatchParams){
            this.mparent = (MatchParams) parent;}
        else
            this.mparent = null;
    }

    public boolean hasQueue() {
        return true;
    }

    public GameManager getGameManager() {
        return GameManager.getGameManager(this);
    }

    public void setTeamRating(Boolean b) {
        this.useBTTeamRating = b;
    }

    public Boolean isTeamRating(){
        return useBTTeamRating != null ? useBTTeamRating : (mparent!= null ? mparent.isTeamRating() : null);
    }


}
