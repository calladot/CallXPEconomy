package com.callxpeconomy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerLogFilterTest {
    @Test
    void isDisabledForAnEmptyPlayerList() {
        PlayerLogFilter filter = new PlayerLogFilter(false, List.of());

        assertFalse(filter.shouldLog("Calladot"));
        assertFalse(filter.shouldLog(null));
    }

    @Test
    void logsEveryKnownAndUnknownPlayerWhenEnabledForAll() {
        PlayerLogFilter filter = new PlayerLogFilter(true, List.of());

        assertTrue(filter.shouldLog("Calladot"));
        assertTrue(filter.shouldLog(null));
    }

    @Test
    void logsOnlyConfiguredPlayerNamesWithoutCaseSensitivity() {
        PlayerLogFilter filter = new PlayerLogFilter(false, List.of("Calladot", "AnotherPlayer"));

        assertTrue(filter.shouldLog("calladot"));
        assertTrue(filter.shouldLog("ANOTHERPLAYER"));
        assertFalse(filter.shouldLog("SomeoneElse"));
    }

    @Test
    void ignoresBlankConfiguredNames() {
        PlayerLogFilter filter = new PlayerLogFilter(false, List.of("", "   "));

        assertFalse(filter.shouldLog("Calladot"));
    }
}