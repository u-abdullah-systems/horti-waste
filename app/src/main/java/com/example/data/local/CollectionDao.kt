package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CollectionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collection_records WHERE dateString = :date ORDER BY timestamp DESC")
    fun getRecordsForDate(date: String): Flow<List<CollectionRecord>>

    @Query("SELECT * FROM collection_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CollectionRecord>>

    @Query("SELECT DISTINCT dateString FROM collection_records ORDER BY dateString DESC")
    fun getAllDates(): Flow<List<String>>

    @Query("SELECT * FROM collection_records WHERE houseType = :houseType AND houseNumber = :houseNumber AND dateString = :date LIMIT 1")
    suspend fun getRecordForHouseOnDate(houseType: String, houseNumber: String, date: String): CollectionRecord?

    @Query("SELECT * FROM collection_records WHERE houseType = :houseType AND houseNumber = :houseNumber ORDER BY timestamp DESC")
    fun getHistoryForHouse(houseType: String, houseNumber: String): Flow<List<CollectionRecord>>

    @Query("SELECT * FROM collection_records WHERE (houseType || '-' || houseNumber) LIKE '%' || :query || '%' OR houseNumber LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 100")
    fun searchRecords(query: String): Flow<List<CollectionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CollectionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<CollectionRecord>)

    @Update
    suspend fun updateRecord(record: CollectionRecord)

    @Delete
    suspend fun deleteRecord(record: CollectionRecord)

    @Query("DELETE FROM collection_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM collection_records")
    suspend fun getTotalRecordCount(): Int
}
