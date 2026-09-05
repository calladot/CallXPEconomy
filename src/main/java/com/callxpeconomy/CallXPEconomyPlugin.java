package com.callxpeconomy;

import com.callxpeconomy.listener.PlayerXpSyncListener;
import com.callxpeconomy.storage.AccountRepository;
import com.callxpeconomy.storage.SqliteAccountRepository;
import com.callxpeconomy.storage.YamlAccountRepository;
import com.callxpeconomy.vault.XpEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;

public final class CallXPEconomyPlugin extends JavaPlugin {
    private AccountRepository accounts;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            Files.createDirectories(getDataFolder().toPath());
            accounts = createRepository();
        } catch (IOException | SQLException | IllegalArgumentException exception) {
            getLogger().severe("Could not initialize account storage: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerXpSyncListener(this, accounts), this);
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault was not found; the XP economy provider will not be registered.");
            return;
        }

        XpEconomyProvider provider = new XpEconomyProvider(this, accounts,
                getConfig().getString("currency.singular", "XP"),
                getConfig().getString("currency.plural", "XP"));
        getServer().getServicesManager().register(Economy.class, provider, this, ServicePriority.Highest);
        getLogger().info("Registered Vault XP economy using " + getConfig().getString("storage.type") + " storage.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        if (accounts != null) {
            accounts.close();
        }
    }

    private AccountRepository createRepository() throws IOException, SQLException {
        String type = getConfig().getString("storage.type", "sqlite").toLowerCase(Locale.ROOT);
        Path dataFolder = getDataFolder().toPath();
        return switch (type) {
            case "sqlite" -> new SqliteAccountRepository(dataFolder.resolve(
                    getConfig().getString("storage.sqlite.file", "accounts.db")));
            case "yaml" -> new YamlAccountRepository(dataFolder.resolve(
                    getConfig().getString("storage.yaml.file", "accounts.yml")));
            default -> throw new IllegalArgumentException("Unsupported storage.type '" + type + "'. Use sqlite or yaml.");
        };
    }
}
