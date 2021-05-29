package com.gwyro.cryptostats.ui.news.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.data.model.DataNewsLunarItem
import com.gwyro.cryptostats.databinding.ListItemNewsBinding
import com.gwyro.cryptostats.databinding.ProgressBarItemBinding
import com.gwyro.cryptostats.utils.LOADING_ITEM_KEY


class CryptoNewsAdapter(
    private val crytoList: List<DataNewsLunarItem>,
    private val clickListener: (DataNewsLunarItem) -> Unit
) : RecyclerView.Adapter<CustomViewHolder>() {

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_ITEM) {
            val binding: ListItemNewsBinding = ListItemNewsBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            NewsViewHolder(parent.context, binding)
        } else {
            val binding = ProgressBarItemBinding.inflate(layoutInflater, parent, false)
            ProgressBarViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        if (holder is NewsViewHolder) {
            holder.bind(crytoList[position], clickListener)
        } else {
            val holderProgress = holder as ProgressBarViewHolder
            holderProgress.bind()
        }
    }

    override fun getItemCount(): Int {
        return crytoList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (crytoList[position].title == LOADING_ITEM_KEY) {
            VIEW_TYPE_LOADING
        } else VIEW_TYPE_ITEM
    }


}

class NewsViewHolder(private val context: Context, private val binding: ListItemNewsBinding) :
    CustomViewHolder(
        binding.root
    ) {
    fun bind(cryptoItem: DataNewsLunarItem, clickListener: (DataNewsLunarItem) -> Unit) {

        binding.cardViewNews.setOnClickListener {
            clickListener(cryptoItem)
        }

        Glide
            .with(context)
            .load(Uri.parse(cryptoItem.thumbnail))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.imgNews.setImageResource(R.mipmap.ic_loading)
                    fillCard(binding, cryptoItem)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    fillCard(binding, cryptoItem)
                    return false
                }

            })
            .centerCrop()
            .into(binding.imgNews)

    }

    fun fillCard(binding: ListItemNewsBinding, cryptoItem: DataNewsLunarItem) {
        binding.apply {
            this.newsTitle.text = cryptoItem.title
        }

    }

}

class ProgressBarViewHolder(private val binding: ProgressBarItemBinding) : CustomViewHolder(
    binding.root
) {
    fun bind() {
        binding.progressbar.visibility = View.VISIBLE
    }
}

open class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(
    itemView
)