package com.findmore.findmore

import android.content.Context
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class RecyclerViewAdapter(var context: Context, var elements: ArrayList<Pair<Int, String?>>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.recycler_view_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rlColor.setBackgroundColor(elements[position].first)
        holder.tvContourType.text = elements[position].second
    }

    override fun getItemCount(): Int {
        return elements.size
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var rlColor: RelativeLayout
        var tvContourType: TextView

        init {
            rlColor = itemView.findViewById(R.id.rlColor)
            tvContourType = itemView.findViewById(R.id.tvContourType)
        }
    }

}