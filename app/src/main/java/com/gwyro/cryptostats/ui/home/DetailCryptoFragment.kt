package com.gwyro.cryptostats.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crazylegend.viewbinding.viewBinding
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.common.CustomProgressDialog
import com.gwyro.cryptostats.data.db.UserCrypto
import com.gwyro.cryptostats.databinding.FragmentDetailCryptoBinding
import com.gwyro.cryptostats.domain.entities.CryptoItem
import com.gwyro.cryptostats.utils.MetrixUtils
import com.gwyro.cryptostats.utils.Utils

class DetailCryptoFragment : Fragment() {
    private var progressDialog: Dialog? = null
    private val args: DetailCryptoFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentDetailCryptoBinding::bind)
    private lateinit var currency: String
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var isCoinOfTheDay = false
    private var menu: Menu? = null
    private var cryptoUser: UserCrypto? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (args.currency.isNotEmpty()) {
            isCoinOfTheDay = false
            currency = args.currency
        } else {
            isCoinOfTheDay = true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notification -> checkCryptoNotification(item)
            else -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkCryptoNotification(item: MenuItem) {
        cryptoUser?.let {
            if (it.isFavourite) {
                homeViewModel.updateUserCrypto(UserCrypto(it.id, it.name, it.currency, false))
                item.setIcon(R.drawable.ic_notif_disabled)
            } else {
                homeViewModel.updateUserCrypto(UserCrypto(it.id, it.name, it.currency, true))
                item.setIcon(R.drawable.ic_notif_active)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        if (!isCoinOfTheDay) {
            inflater.inflate(R.menu.top_app_bar_notification, menu)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isCoinOfTheDay) {
            homeViewModel.getUserCrypto()
        }

        homeViewModel.userCrypto.observe(viewLifecycleOwner) {
            it?.let { list ->
                for (item in list) {
                    if (!isCoinOfTheDay) {
                        if (item.currency == currency) {
                            cryptoUser = item
                            if (!item.isFavourite) {
                                this.menu?.findItem(R.id.notification)
                                    ?.setIcon(R.drawable.ic_notif_disabled)
                            }
                            break
                        }
                    }
                }
            }

        }


        homeViewModel.errorCall.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    hideProgress()
                    if (!Utils.isNetworkAvailable(requireContext())) {
                        cryptoDetailEnabled(false)
                    } else {
                        cryptoDetailEnabled(true)
                    }
                }
            }
        }

        homeViewModel.detailCryptoItem.observe(viewLifecycleOwner) {
            it?.let {
                it.data.let { list ->
                    if (list.isNotEmpty()) {
                        val detailItem = list[0]
                        binding.itemSite.apply {
                            setTextColor(Color.CYAN)
                            paintFlags = binding.itemSite.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                            setOnClickListener {
                                var link = detailItem.website_link
                                if (detailItem.website_link.contains(",")) {
                                    link = detailItem.website_link.substring(
                                        0,
                                        detailItem.website_link.indexOf(",")
                                    )
                                }

                                if (!link.startsWith("http://") && !link.startsWith("https://")) {
                                    link = "http://$link"
                                }
                                val builder = CustomTabsIntent.Builder()
                                val customTabsIntent: CustomTabsIntent = builder.build()
                                customTabsIntent.launchUrl(requireContext(), Uri.parse(link))
                            }
                        }
                        binding.summaryItem.text = if (detailItem.short_summary.isNullOrEmpty()) {
                            resources.getString(R.string.none)
                        } else {
                            detailItem.short_summary
                        }
                    }
                }
            }
            hideProgress()
            binding.llDetail.isVisible = true
        }

        homeViewModel.dayCrypto.observe(viewLifecycleOwner) {
            if (!progressDialog?.isShowing!!) {
                showProgress()
                binding.llDetail.isVisible = false
            }
            if (isCoinOfTheDay && !it.isNullOrEmpty()) {
                val item = it[0]
                setImage(item)
                item.currency?.let {
                    homeViewModel.getCryptoDetails(item.currency)
                }
            }
        }

        homeViewModel.lunaraDay.observe(viewLifecycleOwner) {
            it?.let {
                homeViewModel.fillListCoiOfTheDay()
            }
        }

        homeViewModel.updateCryptoList.observe(viewLifecycleOwner){
            it?.let {
                if(it){
                    showProgress()
                    cryptoDetailEnabled(true)
                    homeViewModel.getCoinOfTheDay()
                }
            }
        }


        binding.llError.setOnClickListener {
            if (Utils.isNetworkAvailable(requireContext())){
                homeViewModel.updateCryptoList()
            }
        }

        binding.llDetail.isVisible = false
        showProgress()
        if (!isCoinOfTheDay) {
            homeViewModel.cryptoItem.value?.let { list ->
                for (item in list) {
                    if (item.currency == currency) {
                        setImage(item)
                    }
                }
            }
        } else {
            if (Utils.isNetworkAvailable(requireContext())) {
                homeViewModel.getCoinOfTheDay()
            } else {
                homeViewModel.updateErrorCall()
            }
        }

    }

    private fun setImage(item: CryptoItem) {
        Glide
            .with(requireActivity())
            .load(item.imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.itemImg.setImageResource(R.mipmap.ic_loading)
                    fillCard(item)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    fillCard(item)
                    return false
                }

            })
            .centerCrop()
            .into(binding.itemImg)
    }

    fun fillCard(item: CryptoItem) {
        binding.apply {
            itemTitle.text = Utils.editLenghtTitle(item.name ?: "")
            item.price?.let {
                val cost =
                    MetrixUtils.convertDoubleToCurrency(it.toDouble(), homeViewModel.getCurrency())
                var i = cost.length
                val finalCost = StringBuilder()
                while (i < 12) {
                    finalCost.append(" ")
                    i++
                }
                finalCost.append(cost)
                itemCost.text = finalCost.toString()
            }
            var perc = "${item.percent_change_24h}%"
            item.percent_change_24h?.let {
                if (it > 0) {
                    itemPerc.setTextColor(Color.GREEN)
                    perc = "+${item.percent_change_24h}%"
                }
            }
            itemPerc.text = perc
            itemMarketCap.text = MetrixUtils.convertDoubleToCurrencyString(
                requireContext(),
                item.market_cap ?: resources.getString(R.string.none)
            )
            itemMarketSupply.text = MetrixUtils.convertDoubleToCurrencyString(
                requireContext(),
                item.circulating_supply ?: resources.getString(R.string.none)
            )
            itemMarketMaxSupply.text = MetrixUtils.convertDoubleToCurrencyString(
                requireContext(),
                item.max_supply ?: resources.getString(R.string.none)
            )
            if (Utils.isNetworkAvailable(requireContext())) {
                if (!isCoinOfTheDay) {
                    homeViewModel.getCryptoDetails(currency)
                }
            } else {
                homeViewModel.updateErrorCall()
            }
        }
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = CustomProgressDialog.showLoadingDialog(this.requireContext())
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }

    private fun cryptoDetailEnabled(value: Boolean) {
        binding.apply {
            llDetail.isVisible = value
            llError.isVisible = !value
        }
    }

}