package com.example.recursosnativos
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "register.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "FormData"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_NAME = "name"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_IMAGE_PATH = "image_path"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_COMMENT TEXT,
                $COLUMN_IMAGE_PATH TEXT
            )
        """
        db.execSQL(createTable)
    }

    fun resetDatabase() {
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_NAME, user.name)
            put(COLUMN_COMMENT, user.comment)
            put(COLUMN_IMAGE_PATH, user.imagePath)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    @SuppressLint("Range")
    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL))
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val comment = cursor.getString(cursor.getColumnIndex(COLUMN_COMMENT))
                val imagePath = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH))

                val user = User(id, email, name, comment, imagePath)
                users.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return users
    }


}

