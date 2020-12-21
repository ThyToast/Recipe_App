package com.example.recipeapp

object Constants {
    const val DB_VERSION = 3
    const val DB_NAME = "MY_RECORDS_DB"
    const val TABLE_NAME = "MY_RECORDS_TABLE"

    //columns
    const val C_ID = "ID"
    const val C_RECIPENAME = "NAME"
    const val C_RECIPETYPE ="TYPE"
    const val C_RECIPEINSTRUCT ="INSTRUCTION"
    const val C_IMAGE = "IMAGE"


    const val CREATE_TABLE = (
            "CREATE TABLE " + TABLE_NAME + "("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + C_RECIPENAME + " TEXT,"
            + C_RECIPEINSTRUCT + " TEXT,"
            + C_RECIPETYPE + " TEXT,"
            + C_IMAGE + " TEXT" + ")"
            )
}