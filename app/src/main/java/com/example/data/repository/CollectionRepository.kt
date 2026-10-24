package com.example.data.repository

import com.example.data.local.CollectionDao
import com.example.data.model.CollectionRecord
import com.example.data.model.ColonyOverview
import com.example.data.model.DailySummary
import com.example.data.model.HouseTypeStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CollectionRepository(private val dao: CollectionDao) {

    val allDates: Flow<List<String>> = dao.getAllDates()

    fun getRecordsForDate(date: String): Flow<List<CollectionRecord>> {
        return dao.getRecordsForDate(date)
    }

    fun getHouseHistory(houseType: String, houseNumber: String): Flow<List<CollectionRecord>> {
        return dao.getHistoryForHouse(houseType.trim().uppercase(), houseNumber.trim())
    }

    fun searchRecords(query: String): Flow<List<CollectionRecord>> {
        return dao.searchRecords(query.trim())
    }

    suspend fun findExistingRecordToday(houseType: String, houseNumber: String, date: String): CollectionRecord? {
        return dao.getRecordForHouseOnDate(houseType.trim().uppercase(), houseNumber.trim(), date)
    }

    suspend fun saveRecord(
        houseType: String,
        houseNumber: String,
        isSegregated: Boolean,
        dateString: String,
        notes: String = ""
    ): CollectionRecord {
        val cleanType = houseType.trim().uppercase()
        val cleanNumber = houseNumber.trim()
        val existing = dao.getRecordForHouseOnDate(cleanType, cleanNumber, dateString)

        val recordToSave = if (existing != null) {
            existing.copy(
                isSegregated = isSegregated,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
        } else {
            CollectionRecord(
                houseType = cleanType,
                houseNumber = cleanNumber,
                isSegregated = isSegregated,
                timestamp = System.currentTimeMillis(),
                dateString = dateString,
                notes = notes
            )
        }

        if (existing != null) {
            dao.updateRecord(recordToSave)
            return recordToSave
        } else {
            val newId = dao.insertRecord(recordToSave)
            return recordToSave.copy(id = newId)
        }
    }

    suspend fun toggleSegregation(record: CollectionRecord) {
        val updated = record.copy(
            isSegregated = !record.isSegregated,
            timestamp = System.currentTimeMillis()
        )
        dao.updateRecord(updated)
    }

    suspend fun deleteRecord(record: CollectionRecord) {
        dao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: Long) {
        dao.deleteRecordById(id)
    }

    fun getDailySummaryFlow(date: String): Flow<DailySummary> {
        return dao.getRecordsForDate(date).map { list ->
            val total = list.size
            val segregated = list.count { it.isSegregated }
            val mixed = total - segregated
            DailySummary(
                dateString = date,
                totalHouses = total,
                segregatedCount = segregated,
                mixedCount = mixed
            )
        }
    }

    fun getAllDailySummaries(): Flow<List<DailySummary>> {
        return dao.getAllRecords().map { list ->
            list.groupBy { it.dateString }
                .map { (date, records) ->
                    val total = records.size
                    val segregated = records.count { it.isSegregated }
                    val mixed = total - segregated
                    DailySummary(
                        dateString = date,
                        totalHouses = total,
                        segregatedCount = segregated,
                        mixedCount = mixed
                    )
                }
                .sortedBy { it.dateString }
        }
    }

    fun getColonyOverviewFlow(date: String): Flow<ColonyOverview> {
        val supportedTypes = listOf("B", "C", "D", "E", "F", "G", "H")
        return dao.getRecordsForDate(date).map { list ->
            val total = list.size
            val segregated = list.count { it.isSegregated }
            val mixed = total - segregated

            val typeStats = supportedTypes.map { type ->
                val typeRecords = list.filter { it.houseType.equals(type, ignoreCase = true) }
                val typeTotal = typeRecords.size
                val typeSegregated = typeRecords.count { it.isSegregated }
                val typeMixed = typeTotal - typeSegregated
                HouseTypeStat(
                    houseType = type,
                    totalHouses = typeTotal,
                    segregatedCount = typeSegregated,
                    mixedCount = typeMixed
                )
            }

            ColonyOverview(
                dateString = date,
                totalHouses = total,
                segregatedCount = segregated,
                mixedCount = mixed,
                typeStats = typeStats,
                records = list
            )
        }
    }

    suspend fun seedInitialDataIfEmpty(todayDate: String) {
        val count = dao.getTotalRecordCount()
        if (count == 0) {
            val sampleRecords = mutableListOf<CollectionRecord>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            // Seed past 7 days (days -6 to today)
            val pastDaysPattern = listOf(
                // Pair(totalSegregated, totalMixed)
                Pair(14, 4), // 6 days ago
                Pair(18, 3), // 5 days ago
                Pair(12, 6), // 4 days ago
                Pair(22, 2), // 3 days ago
                Pair(16, 5), // 2 days ago
                Pair(20, 3), // yesterday
                Pair(15, 2)  // today
            )

            val types = listOf("G", "H", "F", "E", "D", "C", "B")

            pastDaysPattern.forEachIndexed { dayOffset, (segregatedCount, mixedCount) ->
                val daysAgo = (pastDaysPattern.size - 1) - dayOffset
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dateStr = dateFormat.format(cal.time)

                var houseNum = 1
                // Add segregated houses
                for (i in 0 until segregatedCount) {
                    val t = types[i % types.size]
                    sampleRecords.add(
                        CollectionRecord(
                            houseType = t,
                            houseNumber = "$houseNum",
                            isSegregated = true,
                            timestamp = cal.timeInMillis + (i * 120000L),
                            dateString = dateStr,
                            notes = "Clean segregated organic waste"
                        )
                    )
                    houseNum++
                }

                // Add mixed houses
                for (j in 0 until mixedCount) {
                    val t = types[(segregatedCount + j) % types.size]
                    sampleRecords.add(
                        CollectionRecord(
                            houseType = t,
                            houseNumber = "$houseNum",
                            isSegregated = false,
                            timestamp = cal.timeInMillis + ((segregatedCount + j) * 120000L),
                            dateString = dateStr,
                            notes = "Mixed plastic and food waste"
                        )
                    )
                    houseNum++
                }
            }

            dao.insertAll(sampleRecords)
        }
    }
}
