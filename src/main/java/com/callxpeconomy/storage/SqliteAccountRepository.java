package com.callxpeconomy.storage;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

public final class SqliteAccountRepository implements AccountRepository {
    private final Connection connection;

    public SqliteAccountRepository(Path databasePath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "uuid TEXT PRIMARY KEY, name TEXT NOT NULL, balance INTEGER NOT NULL CHECK (balance >= 0), "
                    + "updated_at INTEGER NOT NULL)");
        }
    }

    @Override
    public synchronized Optional<Account> find(UUID playerId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name, balance, updated_at FROM accounts WHERE uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(readAccount(playerId, result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    public synchronized Account createIfAbsent(UUID playerId, String lastKnownName, long initialBalance) {
        validateBalance(initialBalance);
        String name = normalizeName(lastKnownName);
        long now = System.currentTimeMillis();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO accounts(uuid, name, balance, updated_at) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, updated_at = excluded.updated_at")) {
            statement.setString(1, playerId.toString());
            statement.setString(2, name);
            statement.setLong(3, initialBalance);
            statement.setLong(4, now);
            statement.executeUpdate();
            return find(playerId).orElseThrow();
        } catch (SQLException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    public synchronized Account setBalance(UUID playerId, String lastKnownName, long balance) {
        validateBalance(balance);
        String name = normalizeName(lastKnownName);
        long now = System.currentTimeMillis();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO accounts(uuid, name, balance, updated_at) VALUES (?, ?, ?, ?) "
                        + "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, balance = excluded.balance, "
                        + "updated_at = excluded.updated_at")) {
            statement.setString(1, playerId.toString());
            statement.setString(2, name);
            statement.setLong(3, balance);
            statement.setLong(4, now);
            statement.executeUpdate();
            return new Account(playerId, name, balance, now);
        } catch (SQLException exception) {
            throw storageFailure(exception);
        }
    }

    @Override
    public synchronized AdjustmentResult adjust(UUID playerId, String lastKnownName, long delta) {
        String name = normalizeName(lastKnownName);
        long now = System.currentTimeMillis();
        try {
            connection.setAutoCommit(false);
            Account account = find(playerId).orElseGet(() -> createIfAbsent(playerId, name, 0));
            long result;
            try {
                result = Math.addExact(account.balance(), delta);
            } catch (ArithmeticException exception) {
                connection.rollback();
                return AdjustmentResult.insufficientFunds(account.balance());
            }
            if (result < 0) {
                connection.rollback();
                return AdjustmentResult.insufficientFunds(account.balance());
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE accounts SET name = ?, balance = ?, updated_at = ? WHERE uuid = ?")) {
                statement.setString(1, name);
                statement.setLong(2, result);
                statement.setLong(3, now);
                statement.setString(4, playerId.toString());
                statement.executeUpdate();
            }
            connection.commit();
            return new AdjustmentResult(true, result);
        } catch (SQLException exception) {
            rollbackQuietly();
            throw storageFailure(exception);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException exception) {
                throw storageFailure(exception);
            }
        }
    }

    @Override
    public synchronized void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw storageFailure(exception);
        }
    }

    private Account readAccount(UUID playerId, ResultSet result) throws SQLException {
        return new Account(playerId, result.getString("name"), result.getLong("balance"), result.getLong("updated_at"));
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private static String normalizeName(String name) {
        return name == null || name.isBlank() ? "unknown" : name;
    }

    private static void validateBalance(long balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }

    private static IllegalStateException storageFailure(SQLException exception) {
        return new IllegalStateException("SQLite account storage failed", exception);
    }
}
