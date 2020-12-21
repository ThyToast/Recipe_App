package com.example.recipeapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.recipeapp.databinding.ActivityRecipePageBinding

class RecipePage : AppCompatActivity() {

    private lateinit var binding: ActivityRecipePageBinding

    private var dbHelper:MyDB? = null
    private var recordID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MyDB(this)

        val intent = intent
        recordID = intent.getStringExtra("RECORD_ID")
        showRecordDetails()
    }

    private fun showRecordDetails() {
        val selectQuery = "SELECT * FROM ${Constants.TABLE_NAME} WHERE ${Constants.C_ID} =\"$recordID\""
        val db = dbHelper!!.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        Log.d("show record detail", "code has passed through")

        if(cursor.moveToFirst()){
            do {
                val id =""+cursor.getInt(cursor.getColumnIndex(Constants.C_ID))
                val image =""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE))
                val name = ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPENAME))
                val type= ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPETYPE))
                val instruction = ""+cursor.getString(cursor.getColumnIndex(Constants.C_RECIPEINSTRUCT))

                if (name == null){
                    Log.d("name", "null")
                }
                else{
                    Log.d("name", ""+name)
                }

                //set data
                binding.detailedTitle.text = name
                binding.detailedInstruction.text = instruction
                if (image == "null"){
                    binding.detailedImage.setImageResource(R.drawable.ic_no_food)
                }
                else{
                    binding.detailedImage.setImageURI(Uri.parse(image))
                }
            }while (
                cursor.moveToNext()
            )
        }
        db.close()
    }
}