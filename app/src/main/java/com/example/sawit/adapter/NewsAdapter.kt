package com.example.sawit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.model.Article
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(
    private var articles: List<Article>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgNews: ImageView = view.findViewById(R.id.imgNews)
        val tvNewsTitle: TextView = view.findViewById(R.id.tvNewsTitle)
        val tvNewsDescription: TextView = view.findViewById(R.id.tvNewsDescription)
        val tvNewsSource: TextView = view.findViewById(R.id.tvNewsSource)
        val tvNewsDate: TextView = view.findViewById(R.id.tvNewsDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]

        holder.tvNewsTitle.text = article.title
        holder.tvNewsDescription.text = article.description ?: "Tidak ada deskripsi"
        holder.tvNewsSource.text = article.source.name
        holder.tvNewsDate.text = formatDate(article.publishedAt)

        // Load image with Glide
        Glide.with(holder.itemView.context)
            .load(article.urlToImage)
            .placeholder(R.drawable.article_tbs)
            .error(R.drawable.article_tbs)
            .centerCrop()
            .into(holder.imgNews)

        holder.itemView.setOnClickListener {
            onItemClick(article.url)
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateNews(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}