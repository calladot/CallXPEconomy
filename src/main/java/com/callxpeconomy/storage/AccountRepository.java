package com.callxpeconomy.storage;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends AutoCloseable {
    Optional<Account> find(UUID playerId);

    Account createIfAbsent(UUID playerId, String lastKnownName, long initialBalance);

    Account setBalance(UUID playerId, String lastKnownName, long balance);

    AdjustmentResult adjust(UUID playerId, String lastKnownName, long delta);

    @Override
    void close();
}
