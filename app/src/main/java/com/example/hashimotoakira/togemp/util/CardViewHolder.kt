package com.example.hashimotoakira.togemp.util

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class CardViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}