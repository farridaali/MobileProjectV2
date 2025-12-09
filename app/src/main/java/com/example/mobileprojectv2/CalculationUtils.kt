package com.example.mobileprojectv2

fun calculateTotalCost(items: List<ItemEntity>): Double {
    return items.sumOf { it.quantity * it.price }
}

fun calculateRemainingCost(items: List<ItemEntity>): Double {
    return items.filter { !it.isBought }.sumOf { it.quantity * it.price }
}