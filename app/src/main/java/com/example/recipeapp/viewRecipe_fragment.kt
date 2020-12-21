package com.example.recipeapp

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipeapp.databinding.FragmentAddRecipeFragmentBinding
import com.example.recipeapp.databinding.FragmentViewRecipeFragmentBinding
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class viewRecipe_fragment : Fragment(R.layout.fragment_view_recipe_fragment) {

    private var fragment: FragmentViewRecipeFragmentBinding? = null
    private val binding get() = fragment!!

    lateinit var dbHelper:MyDB
    private val first = "${Constants.C_RECIPENAME} DESC"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = MyDB(requireContext())

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragment = FragmentViewRecipeFragmentBinding.inflate(inflater, container, false)
        loadRecords()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recipeRecyclerView.visibility = View.VISIBLE
    }

    private fun loadRecords() {
        val adapterRecipe = AdapterRecipe(requireActivity(), dbHelper.getAllRecords(first))
        binding.recipeRecyclerView.adapter = adapterRecipe
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragment = null
    }

}




