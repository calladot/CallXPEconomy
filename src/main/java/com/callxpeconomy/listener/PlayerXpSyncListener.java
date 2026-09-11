package com.callxpeconomy.listener;

import com.callxpeconomy.PlayerLogFilter;
import com.callxpeconomy.storage.Account;
import com.callxpeconomy.storage.AccountRepository;
import com.callxpeconomy.xp.XpPoints;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public final class PlayerXpSyncListener implements Listener {
    private final Plugin plugin;
    private final AccountRepository accounts;
    private final PlayerLogFilter logFilter;

    public PlayerXpSyncListener(Plugin plugin, AccountRepository accounts, PlayerLogFilter logFilter) {
        this.plugin = plugin;
        this.accounts = accounts;
        this.logFilter = logFilter;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean existingAccount = accounts.find(player.getUniqueId()).isPresent();
        Account account = accounts.createIfAbsent(player.getUniqueId(), player.getName(), XpPoints.total(player));
        XpPoints.apply(player, account.balance());
        log(player, existingAccount ? "join restored" : "join initialized", account.balance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExperienceChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> persist(player, "XP change", true));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        persist(event.getPlayer(), "quit", false);
    }

    private void persist(Player player, String source, boolean requireOnline) {
        if (!requireOnline || player.isOnline()) {
            long balance = XpPoints.total(player);
            Optional<Account> account = accounts.find(player.getUniqueId());
            if (account.isPresent() && account.get().balance() == balance) {
                return;
            }
            accounts.setBalance(player.getUniqueId(), player.getName(), balance);
            log(player, source + " persisted", balance);
        }
    }

    private void log(Player player, String action, long balance) {
        if (logFilter.shouldLog(player.getName())) {
            plugin.getLogger().info("XP sync " + action + ": player=" + player.getName() + ", balance=" + balance);
        }
    }
}
