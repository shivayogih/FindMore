package com.findmore.findmore.firebase_ml.smartreply

import android.content.Context
import android.graphics.drawable.ColorDrawable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.findmore.findmore.R

class SmartReplyAdapter(private val context: Context, private val smartReplyConversation: List<SmartReplyConversationModel>) : RecyclerView.Adapter<SmartReplyAdapter.SmartReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmartReplyViewHolder {
        return SmartReplyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_smart_reply_conversation, parent, false))
    }

    override fun onBindViewHolder(holder: SmartReplyViewHolder, position: Int) {
        if (smartReplyConversation[position].isLocalUser)
            holder.profile.background = ColorDrawable(ContextCompat.getColor(context, android.R.color.holo_green_light))
        else
            holder.profile.background = ColorDrawable(ContextCompat.getColor(context, android.R.color.holo_blue_light))
        holder.text1.text = if (smartReplyConversation[position].isLocalUser) "You" else "Him/Her"
        holder.text2.text = smartReplyConversation[position].message
    }

    override fun getItemCount() = smartReplyConversation.size

    class SmartReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view = itemView
        val profile = itemView.findViewById<View>(R.id.item_smart_reply_profile_view)!!
        val text1 = itemView.findViewById<TextView>(R.id.item_smart_reply_text_view1)!!
        val text2 = itemView.findViewById<TextView>(R.id.item_smart_reply_text_view2)!!
    }
}