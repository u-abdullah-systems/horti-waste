package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "collection_records",
    indices = [
        Index(value = ["dateString", "houseType", "houseNumber"]),
        Index(value = ["dateString"]),
        Index(value = ["houseType"])
    ]
)
data class CollectionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val houseType: String, // "B", "C", "D", "E", "F", "G", "H"
    val houseNumber: String, // e.g. "1", "12", "45-A"
    val isSegregated: Boolean, // true = segregated (organic separated), false = mixed
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // "yyyy-MM-dd"
    val notes: String = ""
) {
    val fullAddress: String
        get() = "$houseType-$houseNumber"
}
