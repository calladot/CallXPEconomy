package com.callxpeconomy.storage;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

public final class YamlAccountRepository implements AccountRepository {
    private final Path file;
    private final YamlConfiguration configuration;

    public YamlAccountRepository(Path file) {
        this.file = file;
        configuration = YamlConfiguration.loadConfiguration(file.toFile());
    }

    @Override
    public synchronized Optional<Account> find(UUID playerId) {
        String path = accountPath(playerId);
        if (!configuration.contains(path + ".balance")) {
            return Optional.empty();
        }
        return Optional.of(new Account(playerId, configuration.getString(path + ".name", "unknown"),
                configuration.getLong(path + ".balance"), configuration.getLong(path + ".updated-at")));
    }

    @Override
    public synchronized Account createIfAbsent(UUID playerId, String lastKnownName, long initialBalance) {
        validateBalance(initialBalance);
        return find(playerId)
                .map(account -> write(playerId, lastKnownName, account.balance()))
                .orElseGet(() -> write(playerId, lastKnownName, initialBalance));
    }

    @Override
    public synchronized Account setBalance(UUID playerId, String lastKnownName, long balance) {
        validateBalance(balance);
        return write(playerId, lastKnownName, balance);
    }

    @Override
    public synchronized AdjustmentResult adjust(UUID playerId, String lastKnownName, long delta) {
        Account account = find(playerId).orElseGet(() -> createIfAbsent(playerId, lastKnownName, 0));
        long balance;
        try {
            balance = Math.addExact(account.balance(), delta);
        } catch (ArithmeticException exception) {
            return AdjustmentResult.insufficientFunds(account.balance());
        }
        if (balance < 0) {
            return AdjustmentResult.insufficientFunds(account.balance());
        }
        write(playerId, lastKnownName, balance);
        return new AdjustmentResult(true, balance);
    }

    @Override
    public void close() {
        // Every mutation is saved before it returns.
    }

    private Account write(UUID playerId, String lastKnownName, long balance) {
        String name = normalizeName(lastKnownName);
        long now = System.currentTimeMillis();
        String path = accountPath(playerId);
        configuration.set(path + ".name", name);
        configuration.set(path + ".balance", balance);
        configuration.set(path + ".updated-at", now);
        saveAtomically();
        return new Account(playerId, name, balance, now);
    }

    private void saveAtomically() {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path temporaryFile = Files.createTempFile(file.toAbsolutePath().getParent(), file.getFileName().toString(), ".tmp");
            configuration.save(temporaryFile.toFile());
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("YAML account storage failed", exception);
        }
    }

    private static String accountPath(UUID playerId) {
        return "accounts." + playerId;
    }

    private static String normalizeName(String name) {
        return name == null || name.isBlank() ? "unknown" : name;
    }

    private static void validateBalance(long balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }
}
