package com.callxpeconomy.vault;

import com.callxpeconomy.PlayerLogFilter;
import com.callxpeconomy.storage.AccountRepository;
import com.callxpeconomy.storage.AdjustmentResult;
import com.callxpeconomy.xp.XpPoints;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class XpEconomyProvider implements Economy {
    private final Plugin plugin;
    private final AccountRepository accounts;
    private final String singular;
    private final String plural;
    private final PlayerLogFilter logFilter;

    public XpEconomyProvider(Plugin plugin, AccountRepository accounts, String singular, String plural,
                             PlayerLogFilter logFilter) {
        this.plugin = plugin;
        this.accounts = accounts;
        this.singular = singular;
        this.plural = plural;
        this.logFilter = logFilter;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "CallXPEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        long wholeAmount = Math.round(amount);
        return wholeAmount + " " + (wholeAmount == 1 ? singular : plural);
    }

    @Override
    public String currencyNamePlural() {
        return plural;
    }

    @Override
    public String currencyNameSingular() {
        return singular;
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName) {
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return accounts.find(player.getUniqueId()).isPresent();
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return accounts.find(player.getUniqueId()).map(account -> (double) account.balance()).orElse(0.0D);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, double amount) {
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return isWholeNonNegative(amount) && getBalance(player) >= amount;
    }

    @Override
    @Deprecated
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return adjust(player, amount, -1, "withdraw");
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return adjust(player, amount, 1, "deposit");
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank(String name, String player) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return unsupportedBank();
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return unsupportedBank();
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember(String name, String playerName) {
        return unsupportedBank();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return unsupportedBank();
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        accounts.createIfAbsent(player.getUniqueId(), accountName(player), 0);
        return true;
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    private EconomyResponse adjust(OfflinePlayer player, double amount, int direction, String action) {
        if (!isWholeNonNegative(amount)) {
            double balance = getBalance(player);
            log(player, action + " rejected: amount=" + amount + ", reason=amount must be a finite, non-negative whole number"
                    + ", balance=" + balance);
            return failed(balance, "XP amounts must be finite, non-negative whole numbers.");
        }
        long points = (long) amount;
        try {
            AdjustmentResult result = accounts.adjust(player.getUniqueId(), accountName(player), direction * points);
            if (!result.successful()) {
                log(player, action + " rejected: amount=" + points + ", reason=insufficient XP, balance=" + result.balance());
                return failed(result.balance(), "Insufficient XP.");
            }
            applyToOnlinePlayer(player, result.balance());
            log(player, action + " successful: amount=" + points + ", balance=" + result.balance());
            return new EconomyResponse(amount, result.balance(), EconomyResponse.ResponseType.SUCCESS, null);
        } catch (IllegalStateException exception) {
            plugin.getLogger().warning("Could not change XP balance for " + player.getUniqueId() + ": " + exception.getMessage());
            double balance = getBalance(player);
            log(player, action + " rejected: amount=" + points + ", reason=storage operation failed, balance=" + balance);
            return failed(balance, "Storage operation failed.");
        }
    }

    private void log(OfflinePlayer player, String message) {
        if (logFilter.shouldLog(player.getName())) {
            plugin.getLogger().info("Vault " + message + ", player=" + accountName(player));
        }
    }

    private void applyToOnlinePlayer(OfflinePlayer offlinePlayer, long balance) {
        Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
        if (player != null && player.isOnline()) {
            XpPoints.apply(player, balance);
        }
    }

    private EconomyResponse unsupportedBank() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts are not supported.");
    }

    private EconomyResponse failed(double balance, String message) {
        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, message);
    }

    private static boolean isWholeNonNegative(double amount) {
        return Double.isFinite(amount) && amount >= 0 && amount <= Integer.MAX_VALUE && amount == Math.rint(amount);
    }

    private static String accountName(OfflinePlayer player) {
        return player.getName() == null ? "unknown" : player.getName();
    }
}
