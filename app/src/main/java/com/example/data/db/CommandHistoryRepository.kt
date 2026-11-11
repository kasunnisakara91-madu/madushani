package com.example.data.db

import kotlinx.coroutines.flow.Flow

class CommandHistoryRepository(private val dao: CommandHistoryDao) {
    val history: Flow<List<CommandHistoryEntity>> = dao.getAllCommands()

    suspend fun logCommand(
        rawCommand: String,
        parsedAction: String,
        aiResponse: String,
        language: String,
        isSuccess: Boolean = true
    ): Long {
        val entry = CommandHistoryEntity(
            rawCommand = rawCommand,
            parsedAction = parsedAction,
            aiResponse = aiResponse,
            language = language,
            isSuccess = isSuccess
        )
        return dao.insertCommand(entry)
    }

    suspend fun deleteEntry(id: Long) {
        dao.deleteCommandById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }
}
