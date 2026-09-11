package com.callxpeconomy;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class PlayerLogFilter {
    private final boolean allPlayers;
    private final Set<String> playerNames;

    public PlayerLogFilter(boolean allPlayers, Collection<String> playerNames) {
        this.allPlayers = allPlayers;
        this.playerNames = playerNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static PlayerLogFilter from(ConfigurationSection section) {
        if (section == null) {
            return new PlayerLogFilter(false, Set.of());
        }
        return new PlayerLogFilter(section.getBoolean("all-players", false), section.getStringList("players"));
    }

    public boolean shouldLog(String playerName) {
        return allPlayers || (playerName != null && playerNames.contains(playerName.toLowerCase(Locale.ROOT)));
    }
}