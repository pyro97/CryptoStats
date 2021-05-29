package com.gwyro.cryptostats.ui.home.adapter

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
import com.gwyro.cryptostats.databinding.ListItemCryptoBinding
import com.gwyro.cryptostats.databinding.ProgressBarItemBinding
import com.gwyro.cryptostats.domain.entities.CryptoItem
import com.gwyro.cryptostats.utils.MetrixUtils
import com.gwyro.cryptostats.utils.Utils


class CryptoListAdapter(
    private val crytoList: List<CryptoItem>,
    private val currency: String,
    private val clickListener: (CryptoItem) -> Unit
) : RecyclerView.Adapter<CustomViewHolder>() {

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

        return if (viewType == VIEW_TYPE_ITEM) {
            val binding: ListItemCryptoBinding = ListItemCryptoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CryptoViewHolder(parent.context, binding)
        } else {
            val binding =
                ProgressBarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ProgressBarViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        if (holder is CryptoViewHolder) {
            holder.bind(currency,crytoList[position], clickListener)
        } else {
            val holderProgress = holder as ProgressBarViewHolder
            holderProgress.bind()
        }
    }

    override fun getItemCount(): Int {
        return crytoList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (crytoList[position].id != -1) {
            VIEW_TYPE_ITEM
        } else VIEW_TYPE_LOADING
    }


}

class CryptoViewHolder(private val context: Context, private val binding: ListItemCryptoBinding) :
    CustomViewHolder(
        binding.root
    ) {
    fun bind(currency: String, cryptoItem: CryptoItem, clickListener: (CryptoItem) -> Unit) {

        binding.imageStar.apply {
            setOnClickListener {
                cryptoItem.favourite = !cryptoItem.favourite
                if (cryptoItem.favourite) {
                    this.setImageResource(R.drawable.ic_fav)
                } else {
                    this.setImageResource(R.drawable.ic_no_fav)
                }
                clickListener(cryptoItem)
            }
        }

        Glide
            .with(context)
            .load(Uri.parse(cryptoItem.imageUrl))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.imgCrypto.setImageResource(R.mipmap.ic_loading)
                    fillCard(currency,binding, cryptoItem)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    fillCard(currency,binding, cryptoItem)
                    return false
                }

            })
            .centerCrop()
            .into(binding.imgCrypto)

    }

    fun fillCard(currency: String,binding: ListItemCryptoBinding, cryptoItem: CryptoItem) {
        binding.apply {
            cryptoName.text = Utils.editLenghtTitle(cryptoItem.name ?: "")
            cryptoId.text = cryptoItem.currency
            cryptoItem.price?.let {
                cryptoPrice.text = MetrixUtils.convertDoubleToCurrency(it.toDouble(),currency)
            }
            if (cryptoItem.favourite) {
                imageStar.setImageResource(R.drawable.ic_fav)
            } else {
                binding.imageStar.setImageResource(R.drawable.ic_no_fav)
            }
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