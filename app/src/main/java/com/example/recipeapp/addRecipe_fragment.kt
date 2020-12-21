package com.example.recipeapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recipeapp.databinding.FragmentAddRecipeFragmentBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.concurrent.thread


class addRecipe_fragment : Fragment(R.layout.fragment_add_recipe_fragment) {

    private var fragment: FragmentAddRecipeFragmentBinding? = null
    private val binding get() = fragment!!

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 101

    private val IMAGE_PICK_CAMERA_CODE = 102
    private val IMAGE_PICK_GALLERY_CODE = 103

    private var imageUri:Uri? = null
    lateinit var dbHelper:MyDB

    private  lateinit var cameraPermission: Array<String>
    private  lateinit var storagePermission: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraPermission = arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        storagePermission = arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        dbHelper = MyDB(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragment = FragmentAddRecipeFragmentBinding.inflate(layoutInflater)

        //obtains xml data from recipetypes
        val xml_data = activity?.assets?.open("recipetypes.xml")
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()

        parser.setInput(xml_data, null)

        var event = parser.eventType
        val recipes: MutableList<String> = ArrayList()
        while (event!= XmlPullParser.END_DOCUMENT){
            val tagName = parser.name
            when(event){
                XmlPullParser.END_TAG -> {
                    if (tagName == "recipe") {
                        val name = parser.getAttributeValue(1)
                        recipes.add(name)
                    }

                }
            }
            event = parser.next()
        }
        Log.d("Display recipe types", recipes.toString())

//        Old code
//        val t=inflater.inflate(R.layout.fragment_add_recipe_fragment, container, false)
//        val spinner = t.findViewById<Spinner>(R.id.recipeType_spinner)
//        val button = t.findViewById<Button>(R.id.submit_Button)
//        val recipeBox = t.findViewById<EditText>(R.id.recipeName_EditText)
//        val recipeBox2 = t.findViewById<EditText>(R.id.recipeName_EditText3)
//                button.setOnClickListener(){
//                val recipe_Name = recipeBox.text
//                val recipe_Type = spinner.selectedItem.toString()
//                val recipe_instruction = recipeBox2.text
//
//                Log.d("Display name", recipe_Name.toString())
//                Log.d("Display type", recipe_Type)
//                Log.d("Display instruction", recipe_instruction.toString())
//        }

        binding.recipeTypeSpinner.adapter = ArrayAdapter(activity?.applicationContext!!, R.layout.support_simple_spinner_dropdown_item, recipes) as SpinnerAdapter
        binding.recipeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Error")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val type = parent?.getItemAtPosition(position).toString()
            }
        }

        //uploads data to SQLite database
        binding.submitButton.setOnClickListener(){
            val recipe_Name = binding.recipeNameEditText.text
            val recipe_Type = binding.recipeTypeSpinner.selectedItem.toString()
            val recipe_instruction = binding.recipeNameEditText3.text

            if (imageUri == null){
                Toast.makeText(requireContext(), "Image not uploaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(recipe_Name)){
                Toast.makeText(requireContext(), "Please enter recipe name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(recipe_Type)){
                Toast.makeText(requireContext(), "Please enter recipe type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(recipe_instruction)){
                Toast.makeText(requireContext(), "Please enter recipe instructions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Display name", recipe_Name.toString())
            Log.d("Display type", recipe_Type)
            Log.d("Display instruction", recipe_instruction.toString())

            dbHelper.insertRecord(
                    "" + recipe_Name,
                    "" + recipe_Type,
                    "" + recipe_instruction,
                    "" + imageUri)

            Toast.makeText(requireContext(), "Added New Recipe", Toast.LENGTH_SHORT).show()

            binding.recipeNameEditText.text.clear()
            binding.recipeNameEditText3.text.clear()
            binding.uploadImageButton.setImageResource(R.drawable.ic_baseline_cloud_upload_24)
        }

        binding.uploadImageButton.setOnClickListener{
            imagePickDialog()
        }
        return binding.root
    }

    private fun imagePickDialog(){
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Select image from:")
        builder.setItems(options){ dialog, which->
            if (which == 0){
                if (!checkCameraPermissions()){
                    requestCameraPermission()
                    pickFromCamera()
                }
                else{
                    pickFromCamera()
                }

            }
            else {
                if (!checkStoragePermission()) {
                    requestStoragePermission()
                    pickFromGallery()
                }
                else{
                    pickFromGallery()
                }
            }
        }
        builder.show()
    }

    private fun pickFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(requireActivity(), storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), cameraPermission, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Image Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image Description")

        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted) {
                        pickFromCamera()
                    } else {
                        Toast.makeText(requireContext(), "Camera permissions required", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(requireContext(), "Storage permissions required", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode:Int,data: Intent?) {
        if (resultCode == Activity.RESULT_OK){
            when(requestCode) {
                IMAGE_PICK_GALLERY_CODE -> {
                    imageUri = data!!.data
                    CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(requireActivity())
                }
                IMAGE_PICK_CAMERA_CODE -> {
                    CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(requireActivity())
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = result.uri
                        imageUri = resultUri

                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Toast.makeText(requireContext(), "" + result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Glide.with(requireContext()).asBitmap().centerCrop().load(imageUri).into(binding.uploadImageButton)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragment = null
    }
}
