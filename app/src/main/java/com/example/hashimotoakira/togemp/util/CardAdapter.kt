package com.example.hashimotoakira.togemp.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.hashimotoakira.togemp.R
import com.example.hashimotoakira.togemp.logic.Card

class CardAdapter(val cardList: List<Card>, val context: Context) : RecyclerView.Adapter<CardViewHolder>() {

    lateinit var listener: CardViewHolder.ItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_main,parent, false) as ImageView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val number = cardList[position].number
        val suit = cardList[position].suit
        val imageId = context.resources.getIdentifier(suit + number, "drawable", context.packageName)
        holder.itemView.setBackgroundResource(imageId)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    fun setClickListener(listener: CardViewHolder.ItemClickListener) {
        this.listener = listener
    }

}