package com.example.recipeapp

import android.content.ClipDescription
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDB(context: Context?): SQLiteOpenHelper(
       context, Constants.DB_NAME, null, Constants.DB_VERSION
)
{
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(Constants.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS "+ Constants.TABLE_NAME)
        onCreate(db)
    }

    fun insertRecord(
            recipeName:String?,
            recipeType:String?,
            recipeInstruction:String?,
            recipeImage:String?,
    ):Long{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(Constants.C_RECIPENAME, recipeName)
        values.put(Constants.C_RECIPETYPE, recipeType)
        values.put(Constants.C_RECIPEINSTRUCT, recipeInstruction)
        values.put(Constants.C_IMAGE, recipeImage)

        val id = db.insert(Constants.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAllRecords(orderBy:String):ArrayList<ModelRecipe>{
        val recordRecipe = ArrayList<ModelRecipe>()
        val selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " ORDER BY " + orderBy
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                val modelRecipe = ModelRecipe(
                        ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPENAME)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPETYPE)),
                        ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPEINSTRUCT))
                )

                recordRecipe.add(modelRecipe)
            }while (cursor.moveToNext())
        }
        db.close()
        return recordRecipe
    }
    fun searchRecords(query:String):ArrayList<ModelRecipe>{
        val recordRecipe = ArrayList<ModelRecipe>()
        val selectQuery = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.C_RECIPENAME + " LIKE '%" + query + "%'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {

                val modelRecipe = ModelRecipe(
                    ""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPENAME)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPETYPE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPEINSTRUCT))
                )

                recordRecipe.add(modelRecipe)
            }while (cursor.moveToNext())
        }
        db.close()
        return recordRecipe

    }

    fun updateRecord(id:String, name:String?, image:String?, type:String?, instruction: String?): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(Constants.C_RECIPENAME, name)
        values.put(Constants.C_RECIPETYPE, type)
        values.put(Constants.C_RECIPEINSTRUCT, instruction)
        values.put(Constants.C_IMAGE, image)

        return db.update(Constants.TABLE_NAME, values, "${Constants.C_ID}=?", arrayOf(id)).toLong()

    }

    fun recordCount():Int{
        val countQuery = "SELECT * FROM ${Constants.TABLE_NAME}"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        return count
    }

    fun deleteRecord(id:String){
        val db = writableDatabase
        db.delete(Constants.TABLE_NAME, "${Constants.C_ID} = ?", arrayOf(id))
        db
        arrayOf(id)
    }

}