package com.example.wastrain

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

data class UserAccount(
    val id: Long,
    val fullName: String,
    val email: String,
    val passwordHash: String
)

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "wastrain_users.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"

        private const val COL_ID = "id"
        private const val COL_NAME = "full_name"
        private const val COL_EMAIL = "email"
        private const val COL_PASSWORD_HASH = "password_hash"

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT,
                $COL_EMAIL TEXT UNIQUE,
                $COL_PASSWORD_HASH TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun registerUser(fullName: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, fullName)
            put(COL_EMAIL, email.lowercase().trim())
            put(COL_PASSWORD_HASH, hashPassword(password))
        }
        val result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        return result != -1L
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ID),
            "$COL_EMAIL = ?",
            arrayOf(email.lowercase().trim()),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun validateLogin(email: String, password: String): UserAccount? {
        val db = readableDatabase
        val emailNorm = email.lowercase().trim()
        val pwdHash = hashPassword(password)

        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COL_EMAIL = ? AND $COL_PASSWORD_HASH = ?",
            arrayOf(emailNorm, pwdHash),
            null, null, null
        )

        var user: UserAccount? = null
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(COL_ID)
            val nameIdx = cursor.getColumnIndexOrThrow(COL_NAME)
            val emailIdx = cursor.getColumnIndexOrThrow(COL_EMAIL)
            val hashIdx = cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH)

            user = UserAccount(
                id = cursor.getLong(idIdx),
                fullName = cursor.getString(nameIdx),
                email = cursor.getString(emailIdx),
                passwordHash = cursor.getString(hashIdx)
            )
        }
        cursor.close()
        return user
    }
}
