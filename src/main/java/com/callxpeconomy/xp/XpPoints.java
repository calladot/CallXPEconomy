package com.callxpeconomy.xp;

import org.bukkit.entity.Player;

public final class XpPoints {
    private XpPoints() {
    }

    public static long total(Player player) {
        long base = pointsToReachLevel(player.getLevel());
        return base + Math.round(player.getExp() * pointsForNextLevel(player.getLevel()));
    }

    public static void apply(Player player, long totalPoints) {
        if (totalPoints < 0 || totalPoints > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("XP total must fit Minecraft's supported range");
        }
        int level = levelFor(totalPoints);
        long remainder = totalPoints - pointsToReachLevel(level);
        player.setLevel(level);
        player.setExp((float) remainder / pointsForNextLevel(level));
        player.setTotalExperience((int) totalPoints);
    }

    public static long pointsToReachLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Level cannot be negative");
        }
        if (level <= 16) {
            return (long) level * level + 6L * level;
        }
        if (level <= 31) {
            return (5L * level * level - 81L * level + 720L) / 2L;
        }
        return (9L * level * level - 325L * level + 4440L) / 2L;
    }

    public static int pointsForNextLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Level cannot be negative");
        }
        if (level <= 15) {
            return 2 * level + 7;
        }
        if (level <= 30) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    public static int levelFor(long totalPoints) {
        if (totalPoints < 0) {
            throw new IllegalArgumentException("XP total cannot be negative");
        }
        int low = 0;
        int high = 1;
        while (pointsToReachLevel(high) <= totalPoints) {
            high *= 2;
        }
        while (low + 1 < high) {
            int middle = low + (high - low) / 2;
            if (pointsToReachLevel(middle) <= totalPoints) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }
}
