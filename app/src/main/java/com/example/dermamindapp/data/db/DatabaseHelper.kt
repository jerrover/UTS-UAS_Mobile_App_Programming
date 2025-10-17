// Implementasi SQLiteOpenHelper untuk mengelola database CRUD.
package com.example.dermamindapp.data.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dermamindapp.data.model.SkinAnalysis

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DermaMind.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "skin_analysis_history"
        private const val COL_ID = "id"
        private const val COL_DATE = "date"
        private const val COL_IMAGE_URI = "imageUri"
        private const val COL_RESULT = "result"
        private const val COL_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_DATE INTEGER, " +
                "$COL_IMAGE_URI TEXT, " +
                "$COL_RESULT TEXT, " +
                "$COL_NOTES TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // CREATE
    fun addAnalysis(analysis: SkinAnalysis) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_DATE, analysis.date)
            put(COL_IMAGE_URI, analysis.imageUri)
            put(COL_RESULT, analysis.result)
            put(COL_NOTES, analysis.notes)
        }
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    // READ
    @SuppressLint("Range")
    fun getAllAnalyses(): List<SkinAnalysis> {
        val analysisList = mutableListOf<SkinAnalysis>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COL_DATE DESC", null)

        if (cursor.moveToFirst()) {
            do {
                analysisList.add(
                    SkinAnalysis(
                        id = cursor.getLong(cursor.getColumnIndex(COL_ID)),
                        date = cursor.getLong(cursor.getColumnIndex(COL_DATE)),
                        imageUri = cursor.getString(cursor.getColumnIndex(COL_IMAGE_URI)),
                        result = cursor.getString(cursor.getColumnIndex(COL_RESULT)),
                        notes = cursor.getString(cursor.getColumnIndex(COL_NOTES))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return analysisList
    }

    // UPDATE
    fun updateNotes(id: Long, notes: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_NOTES, notes)
        val success = db.update(TABLE_NAME, contentValues, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
        return success
    }

    // DELETE
    fun deleteAnalysis(id: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
    }
}