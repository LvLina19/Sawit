package com.example.sawit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(
    private val list: List<YoutubeItem>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail = itemView.findViewById<ImageView>(R.id.imgThumbnail)
        val title = itemView.findViewById<TextView>(R.id.txtTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val videoId = item.snippet.resourceId.videoId

        holder.title.text = item.snippet.title

        Glide.with(holder.itemView.context)
            .load(item.snippet.thumbnails.medium.url)
            .into(holder.thumbnail)

        holder.itemView.setOnClickListener {
            onClick(videoId)
        }
    }
}
