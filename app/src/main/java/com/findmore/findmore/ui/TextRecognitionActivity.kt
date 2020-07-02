package com.findmore.findmore.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.findmore.findmore.BuildConfig
import com.findmore.findmore.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_text_recognition.*
import java.io.File
import java.io.IOException

class TextRecognitionActivity : AppCompatActivity() {

    private var mUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_recognition)

        btnPhoto.setOnClickListener(View.OnClickListener {
            capturePhoto()
        })

        btnOpenGallery.setOnClickListener{
            //check permission at runtime
            val checkSelfPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
                //Requests permissions to be granted to this application at runtime
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
            else{
                openGallery()
            }
        }
    }


    private fun show(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
    private fun capturePhoto(){
        mUri=null
        val capturedImage = File(externalCacheDir, System.currentTimeMillis().toString()+  ".jpg" )
        if(capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if(Build.VERSION.SDK_INT >= 24){
            FileProvider.getUriForFile(this, "com.findmore.findmore.fileprovider", capturedImage)
        } else {
            Uri.fromFile(capturedImage)
        }

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
    }
    private fun openGallery(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }
    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            ivPhoto?.setImageBitmap(bitmap)
        }
        else {
            show("ImagePath is null")
        }
    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

            when(requestCode){
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(mUri!!))
                        //ivPhoto!!.setImageBitmap(bitmap)
                        ivPhoto!!.setImageURI(mUri)
                        scanText(mUri)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val selectedImage = data!!.data
                        scanText(selectedImage)
                        ivPhoto!!.setImageURI(selectedImage)
                    }
        }



    }

    private fun getImagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri!!, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }
    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri!!.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse(
                    "content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri!!.scheme, ignoreCase = true)){
            imagePath = getImagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        renderImage(imagePath)
    }
    private fun scanText(uri: Uri?) {
        try {
            val image =  FirebaseVisionImage.fromFilePath(this@TextRecognitionActivity, uri!!)
            val textRecognizer =
                FirebaseVision.getInstance().onDeviceTextRecognizer
                textRecognizer.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    tvImageText!!.text = firebaseVisionText.text
                    Toast.makeText(this@TextRecognitionActivity, "Success", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@TextRecognitionActivity,
                        e.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun requestPermission(){
        ActivityCompat.requestPermissions(
            this@TextRecognitionActivity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), OPERATION_CHOOSE_PHOTO)
    }
    companion object {
        private val OPERATION_CAPTURE_PHOTO = 1
        private val OPERATION_CHOOSE_PHOTO = 2
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            OPERATION_CHOOSE_PHOTO -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                capturePhoto()
            } else {
                Toast.makeText(this@TextRecognitionActivity, "Permission Denied!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}