package com.example.recipeapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class AdapterRecipe(): RecyclerView.Adapter<AdapterRecipe.HolderRecipe>() {
    private var context: Context? = null
    private var recordList:ArrayList<ModelRecipe>? = null

    lateinit var dbHelper: MyDB

    constructor(context: Context, recordlist:ArrayList<ModelRecipe>?) :this(){
        this.context = context
        this.recordList = recordlist

        dbHelper = MyDB(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipe {
        return HolderRecipe(
                LayoutInflater.from(context).inflate(R.layout.recipes_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HolderRecipe, position: Int) {
        val model = recordList!![position]
        val id = model.id
        val image = model.image
        val title = model.recipeName
        val type = model.recipeType
        val instruction = model.recipeInstruction

        holder.recipeTitle.text = title
        holder.recipeInstruction.text = instruction

        if (image == "null"){
            holder.recipeImage.setImageResource(R.drawable.ic_no_food)
        }
        else{
            holder.recipeImage.setImageURI(Uri.parse(image))
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(context, RecipePage::class.java)
            intent.putExtra("RECORD_ID", id)
            context!!.startActivity(intent)
        }
        holder.editRecipe.setOnClickListener{
            editOptions(id, title, type, instruction, image)
        }
        holder.deleteRecipe.setOnClickListener{
            dbHelper.deleteRecord(id)
        }
    }

    private fun editOptions(id:String, title:String, type:String, instruction:String, image:String ){
        val intent = Intent(context, UpdateRecipe::class.java)
        intent.putExtra("ID", id)
        intent.putExtra("NAME", title)
        intent.putExtra("TYPE", type)
        intent.putExtra("INSTRUCTION", instruction)
        intent.putExtra("IMAGE", image)
        intent.putExtra("isEditMode", true)
        context!!.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return recordList!!.size
    }

    inner class HolderRecipe(itemView: View): RecyclerView.ViewHolder(itemView){
        var recipeImage:ImageView = itemView.findViewById(R.id.recipeImage)
        var recipeTitle:TextView = itemView.findViewById(R.id.recipeTitle)
        var recipeInstruction:TextView = itemView.findViewById(R.id.recipeDescription)
        var deleteRecipe:ImageButton = itemView.findViewById(R.id.delete_recipe)
        var editRecipe:ImageButton = itemView.findViewById(R.id.edit_recipe)

    }

}