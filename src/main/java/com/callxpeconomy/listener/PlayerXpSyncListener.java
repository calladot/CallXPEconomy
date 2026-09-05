package com.callxpeconomy.listener;

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

public final class PlayerXpSyncListener implements Listener {
    private final Plugin plugin;
    private final AccountRepository accounts;

    public PlayerXpSyncListener(Plugin plugin, AccountRepository accounts) {
        this.plugin = plugin;
        this.accounts = accounts;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Account account = accounts.createIfAbsent(player.getUniqueId(), player.getName(), XpPoints.total(player));
        XpPoints.apply(player, account.balance());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExperienceChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> persist(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        persist(event.getPlayer());
    }

    private void persist(Player player) {
        if (player.isOnline()) {
            accounts.setBalance(player.getUniqueId(), player.getName(), XpPoints.total(player));
        }
    }
}
