package com.example.wastrain

import android.content.Context
import android.graphics.Bitmap

class HistoryDatabaseHelper(context: Context) {
    private val appDb = AppDatabaseHelper(context)

    fun saveHistoryImage(bitmap: Bitmap): String = appDb.saveHistoryImage(bitmap)

    fun insertHistory(
        motifId: String,
        motifName: String,
        confidence: Float,
        percentage: String,
        modelName: String,
        imagePath: String
    ): Long = appDb.insertHistory(motifId, motifName, confidence, percentage, modelName, imagePath)

    fun getAllHistory(): List<HistoryItem> = appDb.getAllHistory()

    fun deleteHistory(id: Long): Boolean = appDb.deleteHistory(id)

    fun clearAllHistory(): Boolean = appDb.clearAllHistory()
}
