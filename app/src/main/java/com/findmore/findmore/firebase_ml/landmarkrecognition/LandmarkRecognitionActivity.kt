package com.findmore.findmore.firebase_ml.landmarkrecognition

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.findmore.findmore.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_landmark_recognition.*

class LandmarkRecognitionActivity : AppCompatActivity() {

    private val imageView by lazy { findViewById<ImageView>(R.id.landmark_recognition_image_view)!! }

    private val bottomSheetButton by lazy { findViewById<FrameLayout>(R.id.bottom_sheet_button)!! }
    private val bottomSheetRecyclerView by lazy { findViewById<RecyclerView>(R.id.bottom_sheet_recycler_view)!! }
    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)!!) }

    private val landmarkRecognitionModels = ArrayList<LandmarkRecognitionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmark_recognition)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bottomSheetButton.setOnClickListener {
            CropImage.activity().start(this)
        }

        bottomSheetRecyclerView.layoutManager = LinearLayoutManager(this)
        bottomSheetRecyclerView.adapter = LandmarkRecognitionAdapter(this, landmarkRecognitionModels)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                val imageUri = result.uri
                analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, imageUri))
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "There was some error : ${result.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        imageView.setImageBitmap(null)
        landmarkRecognitionModels.clear()
        bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val options = FirebaseVisionCloudDetectorOptions.Builder()
                .setMaxResults(5)
                .build()
        val landmarkDetector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(options)
        landmarkDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener {
                    val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                    recognizeLandmarks(it, mutableImage)

                    imageView.setImageBitmap(mutableImage)
                    hideProgress()
                    bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                .addOnFailureListener {
                    Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
                    hideProgress()
                }
    }

    private fun recognizeLandmarks(landmarks: List<FirebaseVisionCloudLandmark>?, image: Bitmap?) {
        if (landmarks == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        for (landmark in landmarks) {

            landmarkRecognitionModels.add(LandmarkRecognitionModel(landmark.landmark, landmark.confidence))
        }
    }

    private fun showProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.GONE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.VISIBLE
    }

    private fun hideProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.GONE
    }

}