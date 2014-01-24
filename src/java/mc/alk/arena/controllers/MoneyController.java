package mc.alk.arena.controllers;

import mc.alk.arena.Defaults;
import mc.alk.arena.util.Log;
import net.milkbowl.vault.economy.Economy;

public class MoneyController {
	static boolean initialized = false;
	public static Economy economy = null;

	public static boolean hasEconomy(){
		return initialized;
	}

	public static boolean hasAccount(String name) {
		if (!initialized) return true;
		try{
			return economy.hasAccount(name);
		} catch (Throwable e){
			Log.printStackTrace(e);
			return true;
		}

	}
	public static boolean hasEnough(String name, double fee) {
        return !initialized || hasEnough(name, (float) fee);
    }
	public static boolean hasEnough(String name, float amount) {
		if (!initialized) return true;
		try{
			return economy.getBalance(name) >= amount;
		} catch (Throwable e){
			Log.printStackTrace(e);
			return true;
		}

	}

    @SuppressWarnings({"unused"})
	public static boolean hasEnough(String name, float amount, String world) {
		return hasEnough(name,amount);
	}

    @SuppressWarnings({"unused"})
	public static void subtract(String name, float amount, String world) {
		subtract(name,amount);
	}

	public static void subtract(String name, double amount) {
		subtract(name,(float) amount);
	}

	public static void subtract(String name, float amount) {
		if (!initialized) return;
		try{
			economy.withdrawPlayer(name, amount);
		} catch (Throwable e){
			Log.printStackTrace(e);
		}
	}


    @SuppressWarnings({"unused"})
    public static void add(String name, float amount, String world) {
		add(name,amount);
	}

	public static void add(String name, double amount) {
		if (!initialized) return;
		add(name,(float)amount);
	}

	public static void add(String name, float amount) {
		if (!initialized) return;
		try{
			economy.depositPlayer(name, amount) ;
		} catch (Throwable e){
			Log.printStackTrace(e);
		}
	}

    @SuppressWarnings({"unused"})
	public static Double balance(String name, String world) {
		return balance(name);
	}

	public static Double balance(String name) {
		if (!initialized) return 0.0;
		try{
			return economy.getBalance(name);
		} catch (Throwable e){
			Log.printStackTrace(e);
			return 0.0;
		}
	}


	public static void setEconomy(Economy economy) {
		MoneyController.economy = economy;
		initialized = true;
		/// Certain economy plugins don't implement this method correctly due to a NPE (I'm looking at you BOSEconomy! -_-)
		try{
			String cur = economy.currencyNameSingular();
			if (cur == null || cur.isEmpty()){
				Log.warn("[BattleArena] Warning currency was empty, using name from config.yml");
			} else {
				Defaults.MONEY_STR = cur;
				Defaults.MONEY_SET = true;
			}
		} catch (Throwable e){
			Log.err("[BattleArena] Error setting currency name through vault. Defaulting to BattleArena/config.yml");
			Log.err("[BattleArena] Error was '" + e.getMessage()+"'");
		}
	}

	public static void setRegisterEconomy() {
		initialized = true;
	}

}
