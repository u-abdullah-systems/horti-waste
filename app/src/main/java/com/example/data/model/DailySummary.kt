package com.example.data.model

data class DailySummary(
    val dateString: String,
    val totalHouses: Int,
    val segregatedCount: Int,
    val mixedCount: Int
) {
    val segregationPercentage: Int
        get() = if (totalHouses > 0) ((segregatedCount * 100f) / totalHouses).toInt() else 0
}

data class HouseTypeStat(
    val houseType: String,
    val totalHouses: Int,
    val segregatedCount: Int,
    val mixedCount: Int
) {
    val compliancePercentage: Int
        get() = if (totalHouses > 0) ((segregatedCount * 100f) / totalHouses).toInt() else 0
}

data class ColonyOverview(
    val dateString: String,
    val totalHouses: Int,
    val segregatedCount: Int,
    val mixedCount: Int,
    val typeStats: List<HouseTypeStat>,
    val records: List<CollectionRecord>
) {
    val segregationPercentage: Int
        get() = if (totalHouses > 0) ((segregatedCount * 100f) / totalHouses).toInt() else 0
}
