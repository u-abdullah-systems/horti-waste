package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CollectionRecord
import com.example.data.repository.CollectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CollectionRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CollectionRepository(db.collectionDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Horti Waste", appName)
    }

    @Test
    fun testSaveAndRetrieveCollectionRecord() = runBlocking {
        val saved = repository.saveRecord(
            houseType = "G",
            houseNumber = "12",
            isSegregated = true,
            dateString = "2026-10-24",
            notes = "Clean organic leaves"
        )

        assertNotNull(saved)
        assertEquals("G-12", saved.fullAddress)
        assertTrue(saved.isSegregated)

        val records = repository.getRecordsForDate("2026-10-24").first()
        assertEquals(1, records.size)
        assertEquals("G", records[0].houseType)
        assertEquals("12", records[0].houseNumber)
        assertTrue(records[0].isSegregated)

        // Test updating existing record on the same day
        val updated = repository.saveRecord(
            houseType = "G",
            houseNumber = "12",
            isSegregated = false,
            dateString = "2026-10-24",
            notes = "Mixed plastic found"
        )
        val updatedRecords = repository.getRecordsForDate("2026-10-24").first()
        assertEquals(1, updatedRecords.size)
        assertEquals(false, updatedRecords[0].isSegregated)
    }

    @Test
    fun testDailySummaryCalculation() = runBlocking {
        repository.saveRecord("G", "1", true, "2026-10-24")
        repository.saveRecord("G", "2", true, "2026-10-24")
        repository.saveRecord("F", "1", false, "2026-10-24")

        val summary = repository.getDailySummaryFlow("2026-10-24").first()
        assertEquals(3, summary.totalHouses)
        assertEquals(2, summary.segregatedCount)
        assertEquals(1, summary.mixedCount)
        assertEquals(66, summary.segregationPercentage)
    }

    @Test
    fun testAllDailySummariesChronologicalOrder() = runBlocking {
        repository.saveRecord("G", "1", true, "2026-10-22")
        repository.saveRecord("G", "2", false, "2026-10-23")
        repository.saveRecord("G", "3", true, "2026-10-24")

        val summaries = repository.getAllDailySummaries().first()
        assertEquals(3, summaries.size)
        assertEquals("2026-10-22", summaries[0].dateString)
        assertEquals("2026-10-23", summaries[1].dateString)
        assertEquals("2026-10-24", summaries[2].dateString)
    }
}
