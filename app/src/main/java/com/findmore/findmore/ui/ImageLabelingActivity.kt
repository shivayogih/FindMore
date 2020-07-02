package com.findmore.findmore.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.findmore.findmore.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_image_labeling.*
import java.io.IOException

class ImageLabelingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_labeling)

        btnPhoto.setOnClickListener(View.OnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK)
        })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK) {
            val uri = data!!.data
            labelImage(uri)
        }
    }

    private fun labelImage(uri: Uri?) {
        try {
            val image = FirebaseVisionImage.fromFilePath(this, uri!!)
            val labeler =
                FirebaseVision.getInstance().onDeviceImageLabeler
            progressBar!!.visibility = View.VISIBLE
            labeler.processImage(image)
                .addOnSuccessListener { firebaseVisionImageLabels ->
                    if (firebaseVisionImageLabels.isEmpty()) {
                        tvLabels!!.text = "No labels detected"
                    } else {
                        val sb =
                            StringBuilder("""Recognized labels:""".trimIndent())
                        for (i in 1..firebaseVisionImageLabels.size) {
                            sb.append("$i. " + firebaseVisionImageLabels[i - 1].text + "\n")
                        }
                        tvLabels!!.text = sb.toString()
                        progressBar!!.visibility = View.GONE
                        ivPhoto!!.setImageURI(uri)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this@ImageLabelingActivity,
                        "Oops, that didn't work!", Toast.LENGTH_SHORT
                    ).show()
                    progressBar!!.visibility = View.GONE
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK = 0
    }
}