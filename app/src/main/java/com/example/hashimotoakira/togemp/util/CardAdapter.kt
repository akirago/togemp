package com.example.hashimotoakira.togemp.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.hashimotoakira.togemp.R
import com.example.hashimotoakira.togemp.logic.Card
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import com.example.hashimotoakira.togemp.logic.ChildLogic
import org.greenrobot.eventbus.EventBus


class CardAdapter(val cardList: List<Card>, val context: Context) : RecyclerView.Adapter<CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_main,parent, false) as ImageView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val number = cardList[position].number
        val suit = cardList[position].suit
        val imageId = context.resources.getIdentifier(suit + number, "drawable", context.packageName)
        val bitmap = BitmapFactory.decodeResource(context.resources, imageId)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 60, 91, false)
        //100x100の大きさにリサイズ
        val drawable = BitmapDrawable(context.resources, resizedBitmap)
        (holder.itemView as ImageView).setImageDrawable(drawable)
        holder.itemView.setOnClickListener{
            EventBus.getDefault().post(MessageEvent(position))
        }
//        holder.itemView.setBackgroundResource(imageId)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

}