package com.callxpeconomy.xp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XpPointsTest {
    @Test
    void calculatesMinecraftExperienceThresholds() {
        assertEquals(0, XpPoints.pointsToReachLevel(0));
        assertEquals(315, XpPoints.pointsToReachLevel(15));
        assertEquals(352, XpPoints.pointsToReachLevel(16));
        assertEquals(394, XpPoints.pointsToReachLevel(17));
        assertEquals(1395, XpPoints.pointsToReachLevel(30));
        assertEquals(1507, XpPoints.pointsToReachLevel(31));
        assertEquals(1628, XpPoints.pointsToReachLevel(32));
    }

    @Test
    void findsLevelContainingEachExperienceTotal() {
        assertEquals(0, XpPoints.levelFor(0));
        assertEquals(15, XpPoints.levelFor(351));
        assertEquals(16, XpPoints.levelFor(352));
        assertEquals(17, XpPoints.levelFor(394));
        assertEquals(31, XpPoints.levelFor(1627));
        assertEquals(32, XpPoints.levelFor(1628));
    }

    @Test
    void rejectsNegativeInputs() {
        assertThrows(IllegalArgumentException.class, () -> XpPoints.pointsToReachLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> XpPoints.pointsForNextLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> XpPoints.levelFor(-1));
    }
}
