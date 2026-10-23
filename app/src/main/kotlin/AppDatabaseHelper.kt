package com.example.wastrain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryItem(
    val id: Long,
    val motifId: String,
    val motifName: String,
    val confidence: Float,
    val percentage: String,
    val modelName: String,
    val imagePath: String,
    val timestamp: String
)

data class FavoriteItem(
    val id: Long,
    val motifId: String,
    val motifName: String,
    val origin: String,
    val category: String,
    val timestamp: String
)

class AppDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "wastrain_app.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_HISTORY = "history"
        private const val COL_HIST_ID = "id"
        private const val COL_HIST_MOTIF_ID = "motif_id"
        private const val COL_HIST_MOTIF_NAME = "motif_name"
        private const val COL_HIST_CONFIDENCE = "confidence"
        private const val COL_HIST_PERCENTAGE = "percentage"
        private const val COL_HIST_MODEL_NAME = "model_name"
        private const val COL_HIST_IMAGE_PATH = "image_path"
        private const val COL_HIST_TIMESTAMP = "timestamp"

        private const val TABLE_FAVORITES = "favorites"
        private const val COL_FAV_ID = "id"
        private const val COL_FAV_MOTIF_ID = "motif_id"
        private const val COL_FAV_MOTIF_NAME = "motif_name"
        private const val COL_FAV_ORIGIN = "origin"
        private const val COL_FAV_CATEGORY = "category"
        private const val COL_FAV_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createHistoryTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COL_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HIST_MOTIF_ID TEXT,
                $COL_HIST_MOTIF_NAME TEXT,
                $COL_HIST_CONFIDENCE REAL,
                $COL_HIST_PERCENTAGE TEXT,
                $COL_HIST_MODEL_NAME TEXT,
                $COL_HIST_IMAGE_PATH TEXT,
                $COL_HIST_TIMESTAMP TEXT
            )
        """.trimIndent()

        val createFavTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_FAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FAV_MOTIF_ID TEXT UNIQUE,
                $COL_FAV_MOTIF_NAME TEXT,
                $COL_FAV_ORIGIN TEXT,
                $COL_FAV_CATEGORY TEXT,
                $COL_FAV_TIMESTAMP TEXT
            )
        """.trimIndent()

        db.execSQL(createHistoryTable)
        db.execSQL(createFavTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    // --- History Functions ---
    fun saveHistoryImage(bitmap: Bitmap): String {
        val imagesDir = File(context.filesDir, "history_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val fileName = "scan_${System.currentTimeMillis()}.jpg"
        val imageFile = File(imagesDir, fileName)
        try {
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageFile.absolutePath
    }

    fun insertHistory(
        motifId: String,
        motifName: String,
        confidence: Float,
        percentage: String,
        modelName: String,
        imagePath: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_HIST_MOTIF_ID, motifId)
            put(COL_HIST_MOTIF_NAME, motifName)
            put(COL_HIST_CONFIDENCE, confidence)
            put(COL_HIST_PERCENTAGE, percentage)
            put(COL_HIST_MODEL_NAME, modelName)
            put(COL_HIST_IMAGE_PATH, imagePath)
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            put(COL_HIST_TIMESTAMP, sdf.format(Date()))
        }
        return db.insert(TABLE_HISTORY, null, values)
    }

    fun getAllHistory(): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_HISTORY, null, null, null, null, null, "$COL_HIST_ID DESC")

        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(COL_HIST_ID)
            val mIdIdx = cursor.getColumnIndexOrThrow(COL_HIST_MOTIF_ID)
            val mNameIdx = cursor.getColumnIndexOrThrow(COL_HIST_MOTIF_NAME)
            val confIdx = cursor.getColumnIndexOrThrow(COL_HIST_CONFIDENCE)
            val pctIdx = cursor.getColumnIndexOrThrow(COL_HIST_PERCENTAGE)
            val modelIdx = cursor.getColumnIndexOrThrow(COL_HIST_MODEL_NAME)
            val imgIdx = cursor.getColumnIndexOrThrow(COL_HIST_IMAGE_PATH)
            val timeIdx = cursor.getColumnIndexOrThrow(COL_HIST_TIMESTAMP)

            do {
                list.add(
                    HistoryItem(
                        id = cursor.getLong(idIdx),
                        motifId = cursor.getString(mIdIdx),
                        motifName = cursor.getString(mNameIdx),
                        confidence = cursor.getFloat(confIdx),
                        percentage = cursor.getString(pctIdx),
                        modelName = cursor.getString(modelIdx),
                        imagePath = cursor.getString(imgIdx),
                        timestamp = cursor.getString(timeIdx)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteHistory(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_HISTORY, "$COL_HIST_ID = ?", arrayOf(id.toString())) > 0
    }

    fun clearAllHistory(): Boolean {
        val db = writableDatabase
        db.delete(TABLE_HISTORY, null, null)
        val imagesDir = File(context.filesDir, "history_images")
        if (imagesDir.exists()) {
            imagesDir.deleteRecursively()
        }
        return true
    }

    // --- Favorites Functions ---
    fun toggleFavorite(motifId: String, motifName: String, origin: String, category: String): Boolean {
        return if (isFavorite(motifId)) {
            removeFavorite(motifId)
            false
        } else {
            addFavorite(motifId, motifName, origin, category)
            true
        }
    }

    fun addFavorite(motifId: String, motifName: String, origin: String, category: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FAV_MOTIF_ID, motifId)
            put(COL_FAV_MOTIF_NAME, motifName)
            put(COL_FAV_ORIGIN, origin)
            put(COL_FAV_CATEGORY, category)
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            put(COL_FAV_TIMESTAMP, sdf.format(Date()))
        }
        val res = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return res != -1L
    }

    fun removeFavorite(motifId: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_FAVORITES, "$COL_FAV_MOTIF_ID = ?", arrayOf(motifId)) > 0
    }

    fun isFavorite(motifId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COL_FAV_ID),
            "$COL_FAV_MOTIF_ID = ?",
            arrayOf(motifId),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllFavorites(): List<FavoriteItem> {
        val list = mutableListOf<FavoriteItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_FAVORITES, null, null, null, null, null, "$COL_FAV_ID DESC")

        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(COL_FAV_ID)
            val mIdIdx = cursor.getColumnIndexOrThrow(COL_FAV_MOTIF_ID)
            val mNameIdx = cursor.getColumnIndexOrThrow(COL_FAV_MOTIF_NAME)
            val origIdx = cursor.getColumnIndexOrThrow(COL_FAV_ORIGIN)
            val catIdx = cursor.getColumnIndexOrThrow(COL_FAV_CATEGORY)
            val timeIdx = cursor.getColumnIndexOrThrow(COL_FAV_TIMESTAMP)

            do {
                list.add(
                    FavoriteItem(
                        id = cursor.getLong(idIdx),
                        motifId = cursor.getString(mIdIdx),
                        motifName = cursor.getString(mNameIdx),
                        origin = cursor.getString(origIdx),
                        category = cursor.getString(catIdx),
                        timestamp = cursor.getString(timeIdx)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
