package com.example.recipeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    lateinit var dbHelper:MyDB
    private var recordID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController:NavController = findNavController(R.id.nav_fragment)
        val appBarConfig = AppBarConfiguration(
            setOf(
                    R.id.viewRecipe_fragment,
                    R.id.addRecipe_fragment
            )
        )

        binding.bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfig)

        dbHelper = MyDB(this)

        val intent = intent
        recordID = intent.getStringExtra("RECORD_ID")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        val item = menu.findItem(R.id.searchRecipe)
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchRecords(newText)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchRecords(query)
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun searchRecords(query:String) {
        val adapterRecipe = AdapterRecipe(this, dbHelper.searchRecords(query))
        var recicpeRecyclerView = findViewById<RecyclerView>(R.id.recipe_recyclerView)
        recicpeRecyclerView.adapter = adapterRecipe
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()|| super.onSupportNavigateUp()
    }
}