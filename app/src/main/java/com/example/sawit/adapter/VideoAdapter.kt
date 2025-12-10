package com.example.sawit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.model.PlaylistItem

class VideoAdapter(
    private var videos: List<PlaylistItem>,
    private val onVideoClick: (String) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.imgVideoThumbnail)
        val title: TextView = itemView.findViewById(R.id.tvVideoTitle)
        val description: TextView = itemView.findViewById(R.id.tvVideoDescription)

        fun bind(video: PlaylistItem) {
            title.text = video.snippet.title
            description.text = video.snippet.description

            Glide.with(itemView.context)
                .load(video.snippet.thumbnails.medium.url)
                .placeholder(R.color.grey_light)
                .into(thumbnail)

            itemView.setOnClickListener {
                onVideoClick(video.snippet.resourceId.videoId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount() = videos.size

    fun updateVideos(newVideos: List<PlaylistItem>) {
        videos = newVideos
        notifyDataSetChanged()
    }
}