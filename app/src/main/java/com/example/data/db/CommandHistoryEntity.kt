package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val rawCommand: String,
    val parsedAction: String,
    val aiResponse: String,
    val language: String, // "Sinhala", "English", "Mixed"
    val isSuccess: Boolean = true
)
