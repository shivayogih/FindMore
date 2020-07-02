package com.findmore.findmore.ui

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Pair
import android.view.View

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.findmore.findmore.R
import com.findmore.findmore.RecyclerViewAdapter
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionPoint
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_face_recognition.*
import java.io.IOException
import java.util.*

class FaceRecognitionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)

        btnPhoto.setOnClickListener(View.OnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,
                REQUEST_CODE_PICK
            )
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK) {
            val uri = data!!.data
            scanContours(uri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun scanContours(uri: Uri?) {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .build()
        try {
            progressBar!!.visibility = View.VISIBLE
            val image = FirebaseVisionImage.fromFilePath(this, uri!!)
            val detector =
                FirebaseVision.getInstance().getVisionFaceDetector(options)
            detector.detectInImage(image)
                .addOnSuccessListener { firebaseVisionFaces ->
                    val bmp = drawContoursOnImage(uri, firebaseVisionFaces)
                    if (bmp != null) {
                        ivPhoto!!.setImageBitmap(bmp)
                        Toast.makeText(
                            this@FaceRecognitionActivity,
                            "SUCCESS", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@FaceRecognitionActivity,
                            "Faces couldn't be scanned", Toast.LENGTH_SHORT
                        ).show()
                    }
                    progressBar!!.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@FaceRecognitionActivity,
                        e.toString(), Toast.LENGTH_LONG
                    ).show()
                    progressBar!!.visibility = View.GONE
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /*private void scanFaces(final Uri uri) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(this, uri);
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector();
            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                            Bitmap bmp = drawRectsOnImage(uri, firebaseVisionFaces);
                            if (bmp != null) {
                                ivPhoto.setImageBitmap(bmp);
                            } else {
                                Toast.makeText(FaceRecognitionActivity.this, "Faces couldn't be scanned", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(FaceRecognitionActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawRectsOnImage(
        image: Uri,
        faces: List<FirebaseVisionFace>
    ): Bitmap? {
        try {
            val source = ImageDecoder.createSource(contentResolver, image)
            var bmp = ImageDecoder.decodeBitmap(source)
            bmp = bmp.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(bmp)
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 10f
            paint.color = Color.RED
            paint.isAntiAlias = true
            for (face in faces) {
                canvas.drawRect(face.boundingBox, paint)
            }
            return bmp
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawContoursOnImage(
        image: Uri?,
        faces: List<FirebaseVisionFace>
    ): Bitmap? {
        try {
            val source = ImageDecoder.createSource(contentResolver, image!!)
            var bmp = ImageDecoder.decodeBitmap(source)
            bmp = bmp.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(bmp)
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.isAntiAlias = true
            val colorList =
                setupColorList()
            rvContours!!.adapter =
                RecyclerViewAdapter(this, colorList)
            rvContours!!.layoutManager = LinearLayoutManager(this)
            for (face in faces) {
                drawContoursOnCanvas(getAllContourLists(face), colorList, canvas, paint)
            }
            return bmp
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getAllContourLists(face: FirebaseVisionFace): List<List<FirebaseVisionPoint>> {
        val result =
            ArrayList<List<FirebaseVisionPoint>>()
        // the loop goes from 2 to 14 because Firebase provides 14 different enum values
        // for ContourTypes. However, we don't want the first value (ALL_CONTOURS) because
        // then the connections between the ContourTypes would look strange. For example,
        // then there would be a part of the mouth connected with the eyes. To prevent that,
        // we need to get all the single contours.
        for (i in 2..14) {
            result.add(face.getContour(i).points)
        }
        return result
    }

    private fun drawContoursOnCanvas(
        allContours: List<List<FirebaseVisionPoint>>,
        colorList: List<Pair<Int, String?>>,
        canvas: Canvas, paint: Paint
    ) {
        for (i in allContours.indices) {
            val points = allContours[i]
            val curColor = colorList[i].first
            paint.color = curColor
            for (j in 0 until points.size - 1) {
                val point1 = points[j]
                val point2 = points[j + 1]
                canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint)
            }
            if (points.isNotEmpty()) {
                val firstPoint = points[0]
                val lastPoint = points[points.size - 1]
                canvas.drawLine(
                    lastPoint.x,
                    lastPoint.y,
                    firstPoint.x,
                    firstPoint.y,
                    paint
                )
            }
        }
    }

    private fun setupColorList(): ArrayList<Pair<Int, String?>> {
        val r = Random()
        val colorList =
            ArrayList<Pair<Int, String?>>()
        val allContourTypes: ArrayList<String?> = ArrayList<String?>(listOf("Face", "Left Eyebrow Top", "Left Eyebrow Bottom", "Right Eyebrow Top", "Right Eyebrow Bottom", "Left Eye", "Right Eye", "Upper Lip Top", "Upper Lip Bottom", "Lower Lip Top", "Lower Lip Bottom", "Nose Bridge", "Nose Bottom"))
        for (i in 2..14) {
            val color = Color.argb(
                255, r.nextInt(255),
                r.nextInt(255), r.nextInt(255)
            )
            colorList.add(Pair(color, allContourTypes[i - 2]))
        }
        return colorList
    }

    companion object {
        private const val REQUEST_CODE_PICK = 0
    }
}