package com.gwyro.cryptostats.ui.home.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.databinding.ListItemWatchBinding
import com.gwyro.cryptostats.domain.entities.CryptoItem
import com.gwyro.cryptostats.utils.MetrixUtils
import com.gwyro.cryptostats.utils.Utils

class WatchListAdapterAdapter(
    private val crytoList: List<CryptoItem>,
    private val currency: String,
    private val clickListener: (CryptoItem) -> Unit
) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: ListItemWatchBinding =
            ListItemWatchBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(parent.context, binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(crytoList[position], clickListener, currency)
    }

    override fun getItemCount(): Int {
        return crytoList.size
    }
}

class MyViewHolder(private val context: Context, private val binding: ListItemWatchBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(cryptoItem: CryptoItem, clickListener: (CryptoItem) -> Unit, currency: String) {

        Glide
            .with(context)
            .load(Uri.parse(cryptoItem.imageUrl))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.imgWatch.setImageResource(R.mipmap.ic_loading)
                    fillCard(binding, cryptoItem, currency)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    fillCard(binding, cryptoItem, currency)
                    return false
                }

            })
            .centerCrop()
            .into(binding.imgWatch)

        binding.cardView.setOnClickListener {
            clickListener(cryptoItem)
        }
    }

    fun fillCard(binding: ListItemWatchBinding, cryptoItem: CryptoItem, currency: String) {
        binding.apply {
            coinName.text = Utils.editLenghtTitle(cryptoItem.name ?: "")
            idName.text = cryptoItem.currency
            cryptoItem.price?.let {
                priceCoin.text = MetrixUtils.convertDoubleToCurrency(it.toDouble(), currency)
            }
            cryptoItem.percent_change_24h?.let {
                var perc = "$it%"
                if (it > 0) {
                    percCoin.setTextColor(Color.GREEN)
                    perc = "+$it%"
                }
                percCoin.text = perc
            }

        }

    }

}