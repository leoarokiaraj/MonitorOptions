package com.example.monitoroptions

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + id_COL + " INTEGER PRIMARY KEY, " +
                ce_price_COl + " TEXT," +
                pe_price_COL + " TEXT," +
                ce_strike_COL + " TEXT," +
                pe_strike_COL + " TEXT," +
                expiry_COL + " TEXT," +
                alert_COL + " TEXT," +
                previous_profit_COL + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addUpdateOptionData(optionData : NSEOptionData ){
        val id: Int? = ifRecordExist();
        if (id == null) {
            addOptionData(optionData)
        }
        else {
            updateOptionData(optionData, id)
        }
    }

    fun updateOptionData(optionData : NSEOptionData, id: Int ){

        // below we are creating
        // a content values variable
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        values.put(ce_price_COl, optionData.ce_price)
        values.put(pe_price_COL, optionData.pe_price)
        values.put(ce_strike_COL, optionData.ce_strike)
        values.put(pe_strike_COL, optionData.pe_strike)
        values.put(expiry_COL, optionData.expiry)
        values.put(alert_COL, optionData.alert)
        values.put(previous_profit_COL, optionData.previous_profit)

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.update(TABLE_NAME, values, "$id_COL=$id", arrayOf())

        // at last we are
        // closing our database
        db.close()
    }

    fun addOptionData(optionData : NSEOptionData ){

        // below we are creating
        // a content values variable
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        values.put(ce_price_COl, optionData.ce_price)
        values.put(pe_price_COL, optionData.pe_price)
        values.put(ce_strike_COL, optionData.ce_strike)
        values.put(pe_strike_COL, optionData.pe_strike)
        values.put(expiry_COL, optionData.expiry)
        values.put(alert_COL, optionData.alert)
        values.put(previous_profit_COL, optionData.previous_profit)

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(TABLE_NAME, null, values)

        // at last we are
        // closing our database
        db.close()
    }

    @SuppressLint("Range")
    fun ifRecordExist(): Int? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        val cursor  = db.rawQuery("SELECT $id_COL FROM $TABLE_NAME LIMIT 1", null)
        if (cursor!!.moveToFirst())
            return cursor.getString(cursor.getColumnIndex(id_COL)).toInt()
        else
            return null;

    }

    @SuppressLint("Range")
    fun readOptionData(): NSEOptionData? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val optionData  = NSEOptionData();
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        val cursor  = db.rawQuery("SELECT * FROM $TABLE_NAME LIMIT 1", null)
        if (cursor!!.moveToFirst()) {
            optionData.id = cursor.getString(cursor.getColumnIndex(id_COL)).toInt()
            optionData.ce_price = cursor.getString(cursor.getColumnIndex(ce_price_COl))
            optionData.pe_price = cursor.getString(cursor.getColumnIndex(pe_price_COL))
            optionData.ce_strike = cursor.getString(cursor.getColumnIndex(ce_strike_COL))
            optionData.pe_strike = cursor.getString(cursor.getColumnIndex(pe_strike_COL))
            optionData.expiry = cursor.getString(cursor.getColumnIndex(expiry_COL))
            optionData.alert = cursor.getString(cursor.getColumnIndex(alert_COL))
            optionData.previous_profit = cursor.getString(cursor.getColumnIndex(previous_profit_COL))
            return optionData
        } else
            return null

    }

    fun deleteAll() {
        val db = this.writableDatabase
        // this method is to check if table already exists
        db.execSQL("DELETE FROM " + TABLE_NAME)
    }

    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "monitor_options"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        // below is the variable for table name
        val TABLE_NAME = "option_data"


        val id_COL = "id"
        val ce_price_COl = "ce_price"
        val pe_price_COL = "pe_price"
        val ce_strike_COL = "ce_strike"
        val pe_strike_COL = "pe_strike"
        val expiry_COL = "expiry"
        val alert_COL = "alert"
        val previous_profit_COL = "previous_profit"
    }
}
