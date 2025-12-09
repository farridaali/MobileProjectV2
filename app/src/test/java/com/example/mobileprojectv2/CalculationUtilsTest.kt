package com.example.mobileprojectv2

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculationUtilsTest {

    @Test
    fun testCalculateTotalCost() {
        val items = listOf(
            ItemEntity(1, "Apple", 2, 1.5, false),
            ItemEntity(2, "Banana", 3, 0.5, true)
        )

        val totalCost = calculateTotalCost(items)

        assertEquals(4.5, totalCost, 0.0)
    }

    @Test
    fun testCalculateRemainingCost() {
        val items = listOf(
            ItemEntity(1, "Apple", 2, 1.5, false),
            ItemEntity(2, "Banana", 3, 0.5, true)
        )

        val remainingCost = calculateRemainingCost(items)

        assertEquals(3.0, remainingCost, 0.0)
    }
}