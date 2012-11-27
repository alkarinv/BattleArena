package mc.alk.arena.controllers;

import mc.alk.arena.Defaults;
import net.milkbowl.vault.economy.Economy;

import com.nijikokun.register.payment.Methods;

public class MoneyController {
	static boolean initialized = false;
	static boolean hasVault = false;
	static boolean hasRegister = false;
	static boolean useVault = false;

	protected static Economy economy = null;
	public static boolean hasEconomy(){
		return initialized && (hasRegister || hasVault);
	}
	public static boolean hasRegisterEconomy(){
		return initialized && hasRegister;
	}
	public static boolean hasVaultEconomy(){
		return initialized && hasVault;
	}

	public static boolean hasAccount(String name) {
		if (!initialized) return true;
		return useVault? economy.hasAccount(name) : Methods.getMethod().hasAccount(name);
	}
	public static boolean hasEnough(String name, double fee) {
		if (!initialized) return true;
		return hasEnough(name, (float) fee);
	}
	public static boolean hasEnough(String name, float amount) {
		if (!initialized) return true;
		return useVault? economy.getBalance(name) >= amount :Methods.getMethod().getAccount(name).hasEnough(amount);
	}
	public static void subtract(String name, double fee) {
		if (!initialized) return;
		subtract(name,(float) fee);
	}
	public static void subtract(String name, float amount) {
		if (!initialized) return;
		if (useVault) economy.withdrawPlayer(name, amount);
		else Methods.getMethod().getAccount(name).subtract(amount);
	}
	public static void add(String name, float amount) {
		if (!initialized) return;
		if (useVault) economy.depositPlayer(name, amount) ;
		else Methods.getMethod().getAccount(name).add(amount);
	}
	public static Double balance(String name) {
		if (!initialized) return 0.0;
		return useVault ? economy.getBalance(name) : Methods.getMethod().getAccount(name).balance();
	}
	public static void add(String name, double amount) {
		if (!initialized) return;
		add(name,(float)amount);
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
		} catch (Exception e){}
	}

	public static void setRegisterEconomy() {
		hasRegister = true;
		initialized = true;
	}

}
