package mc.alk.arena.events.prizes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Represents a reward event, called when rewards are given out to players.
 * See also: ArenaDrawersPrizeEvent, ArenaLosersPrizeEvent, ArenaWinnersPrizeEvent
 */
public class ArenaPrizeEvent extends BAEvent {
	final Competition competition;
	final Collection<ArenaTeam> teams;

	Integer exp;
	Double money;
	List<ItemStack> items;
	List<PotionEffect> effects;
	List<Reward> rewards;

	public ArenaPrizeEvent(Competition competition, Collection<ArenaTeam> teams){
		this.competition = competition;
		this.teams = teams;
	}
	public Integer getExp() {
		return exp;
	}
	public void setExp(Integer exp) {
		this.exp = exp;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
	public List<ItemStack> getItems() {
		return items;
	}
	public void setItems(List<ItemStack> items) {
		this.items = items;
	}
	public List<PotionEffect> getEffects() {
		return effects;
	}
	public void setEffects(List<PotionEffect> effects) {
		this.effects = effects;
	}
	/**
	 * Returns a list of all reward objects for this arena.
	 * Does not contain rewards listed in the ArenaConfig.yml file.
	 */
	public List<Reward> getRewards() {
		return rewards;
	}
	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}
	/**
	 * Adds a new reward for the supplied teams.
	 * @param reward The reward to give the teams involved in this event.
	 *
	 * Usage:
	 * Reward r = new Reward(){
	 *		@Override
	 *		public void reward(Team team) {
	 *			//Whatever you'd like to do with the winning team...
	 * 			//Teleport, give potion effects, kill them all.
	 *		}
	 *	};
	 */
	public void addReward(Reward reward){
		if (this.rewards == null){
			this.rewards = new ArrayList<Reward>();}
		this.rewards.add(reward);
	}
	public Collection<ArenaTeam> getTeams() {
		return teams;
	}
}
