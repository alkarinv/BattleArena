package mc.alk.arena.controllers;

import mc.alk.arena.Defaults;
import mc.alk.arena.util.Log;
import net.milkbowl.vault.economy.Economy;

public class MoneyController {
	static boolean initialized = false;
	static boolean hasVault = false;
	static boolean useVault = false;
	public static Economy economy = null;
	public static boolean hasEconomy(){
		return initialized;
	}
	public static boolean hasAccount(String name) {
		if (!initialized) return true;
		return useVault? economy.hasAccount(name) : true;
	}
	public static boolean hasEnough(String name, double fee) {
		if (!initialized) return true;
		return hasEnough(name, (float) fee);
	}
	public static boolean hasEnough(String name, float amount) {
		if (!initialized) return true;
		return useVault? economy.getBalance(name) >= amount : true;
	}

	public static boolean hasEnough(String name, float amount, String world) {
		return hasEnough(name,amount);
	}

	public static void subtract(String name, float amount, String world) {
		subtract(name,amount);
	}

	public static void subtract(String name, double amount) {
		subtract(name,(float) amount);
	}

	public static void subtract(String name, float amount) {
		if (!initialized) return;
		if (useVault) economy.withdrawPlayer(name, amount);
	}


	public static void add(String name, float amount, String world) {
		add(name,amount);
	}

	public static void add(String name, double amount) {
		if (!initialized) return;
		add(name,(float)amount);
	}

	public static void add(String name, float amount) {
		if (!initialized) return;
		if (useVault) economy.depositPlayer(name, amount) ;
	}

	public static Double balance(String name, String world) {
		return balance(name);
	}

	public static Double balance(String name) {
		if (!initialized) return 0.0;
		return useVault ? economy.getBalance(name) : 0;
	}


	public static void setEconomy(Economy economy) {
		MoneyController.economy = economy;
		initialized = true;
		hasVault = true;
		useVault = true;
		/// Certain economy plugins don't implement this method correctly due to a NPE (I'm looking at you BOSEconomy! -_-)
		try{
			Defaults.MONEY_STR = economy.currencyNameSingular();
			Defaults.MONEY_SET = true;
		} catch (Error e){
			Log.err("[BattleArena] Error setting currency name through vault");
			e.printStackTrace();
		} catch (Exception e){
			Log.err("[BattleArena] Error setting currency name through vault");
			e.printStackTrace();
		}
	}

	public static void setRegisterEconomy() {
		initialized = true;
	}

}
