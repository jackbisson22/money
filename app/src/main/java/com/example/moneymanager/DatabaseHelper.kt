package com.example.moneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MoneyManager.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "transactions"
        private const val COL_ID = "id"
        private const val COL_TYPE = "type"
        private const val COL_AMOUNT = "amount"
        private const val COL_DESCRIPTION = "description"
        private const val COL_CATEGORY = "category"
        private const val COL_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TYPE TEXT,
                $COL_AMOUNT REAL,
                $COL_DESCRIPTION TEXT,
                $COL_CATEGORY TEXT,
                $COL_DATE TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // INSERT transaction
    fun addTransaction(transaction: Transaction): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_TYPE, transaction.type)
            put(COL_AMOUNT, transaction.amount)
            put(COL_DESCRIPTION, transaction.description)
            put(COL_CATEGORY, transaction.category)
            put(COL_DATE, transaction.date)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result
    }

    // GET all transactions
    fun getAllTransactions(): ArrayList<Transaction> {
        val list = ArrayList<Transaction>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME ORDER BY $COL_ID DESC", null
        )

        if (cursor.moveToFirst()) {
            do {
                val transaction = Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
                )
                list.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // GET total income
    fun getTotalIncome(): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_NAME WHERE $COL_TYPE = 'IN'", null
        )
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    // GET total expense
    fun getTotalExpense(): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_NAME WHERE $COL_TYPE = 'OUT'", null
        )
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    // DELETE transaction
    fun deleteTransaction(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID=?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // DELETE all transactions
    fun deleteAllTransactions() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
}
