package com.callxpeconomy.storage;

import java.util.UUID;

public record Account(UUID playerId, String lastKnownName, long balance, long updatedAt) {
}
