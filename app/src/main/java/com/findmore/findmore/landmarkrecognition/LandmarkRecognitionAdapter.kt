package com.findmore.findmore.landmarkrecognition

import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.findmore.findmore.R

class LandmarkRecognitionAdapter(private val context: Context, private val landmarkRecognitionModels: ArrayList<LandmarkRecognitionModel>) : RecyclerView.Adapter<LandmarkRecognitionAdapter.LandmarkRecognitionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandmarkRecognitionViewHolder {
        return LandmarkRecognitionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_landmark_recognition, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LandmarkRecognitionViewHolder, position: Int) {
        holder.text1.text = landmarkRecognitionModels[position].text
        holder.text2.text = landmarkRecognitionModels[position].confidence.toString()
    }

    override fun getItemCount() = landmarkRecognitionModels.size

    class LandmarkRecognitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view = itemView
        val text1 = itemView.findViewById<TextView>(R.id.item_landmark_recognition_text_view1)!!
        val text2 = itemView.findViewById<TextView>(R.id.item_landmark_recognition_text_view2)!!
    }
}