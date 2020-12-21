package com.example.recipeapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.recipeapp.databinding.ActivityMainBinding
import com.example.recipeapp.databinding.ActivityUpdateRecipeBinding
import com.example.recipeapp.databinding.FragmentAddRecipeFragmentBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class UpdateRecipe : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateRecipeBinding

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 101

    private val IMAGE_PICK_CAMERA_CODE = 102
    private val IMAGE_PICK_GALLERY_CODE = 103

    private var imageUri: Uri? = null
    lateinit var dbHelper:MyDB

    private var isEditMode = false

    private  lateinit var cameraPermission: Array<String>
    private  lateinit var storagePermission: Array<String>

    private var oldImage:Uri? = null
    private var oldId:String? = ""
    private var oldName:String? = ""
    private var oldType:String? = ""
    private var oldInstruction:String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraPermission = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        storagePermission = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        val intent = intent
        isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode){
//            actionBar!!.title = "Update Recipe"

            oldId = intent.getStringExtra("ID")
            oldImage = Uri.parse(intent.getStringExtra("IMAGE"))
            oldName = intent.getStringExtra("NAME")
            oldType = intent.getStringExtra("TYPE")
            oldInstruction = intent.getStringExtra("INSTRUCTION")


            if(imageUri.toString() == "null"){
                binding.updateUploadImageButton.setImageResource(R.drawable.ic_no_food)
            }
            else{
                binding.updateUploadImageButton.setImageURI(imageUri)
            }

            binding.updateRecipeNameEditText.setText(oldName)
            binding.updateRecipeNameEditText2.setText(oldInstruction)
        }

        val xml_data = this.assets?.open("recipetypes.xml")
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
        val arrayAdapter:ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipes)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.updateRecipeTypeSpinner.adapter = arrayAdapter
        binding.updateRecipeTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        dbHelper = MyDB(this)

        binding.updateUploadImageButton.setOnClickListener(){
            imagePickDialog()
        }

        binding.updateButton.setOnClickListener(){
            val recipe_Name = binding.updateRecipeNameEditText.text
            val recipe_Type = binding.updateRecipeTypeSpinner.selectedItem.toString()
            val recipe_instruction = binding.updateRecipeNameEditText2.text

            if (imageUri == null){
                Toast.makeText(this, "Image not uploaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(recipe_Name)){
                Toast.makeText(this, "Please enter recipe name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(recipe_Type)){
                Toast.makeText(this, "Please enter recipe type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(recipe_instruction)){
                Toast.makeText(this, "Please enter recipe instructions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Display name", recipe_Name.toString())
            Log.d("Display type", recipe_Type)
            Log.d("Display instruction", recipe_instruction.toString())

            dbHelper.updateRecord(
                "" + oldId,
                "" + recipe_Name,
                "" + imageUri,
                "" + recipe_Type,
            "" + recipe_instruction)

            Toast.makeText(this, "Updated Recipe", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
        }

    }

    private fun imagePickDialog(){
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)

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
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Image Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image Description")

        imageUri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
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
                        Toast.makeText(this, "Camera permissions required", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Storage permissions required", Toast.LENGTH_SHORT).show()
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
                    CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this)
                }
                IMAGE_PICK_CAMERA_CODE -> {
                    CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this)
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = result.uri
                        imageUri = resultUri

                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Toast.makeText(this, "" + result.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Glide.with(this).asBitmap().centerCrop().load(imageUri).into(binding.updateUploadImageButton)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}