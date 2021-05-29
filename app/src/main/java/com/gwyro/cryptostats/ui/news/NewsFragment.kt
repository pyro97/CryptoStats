package com.gwyro.cryptostats.ui.news

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crazylegend.viewbinding.viewBinding
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.common.CustomProgressDialog
import com.gwyro.cryptostats.data.model.DataNewsLunarItem
import com.gwyro.cryptostats.databinding.FragmentNewsBinding
import com.gwyro.cryptostats.ui.news.adapter.CryptoNewsAdapter
import com.gwyro.cryptostats.utils.LOADING_ITEM_KEY
import com.gwyro.cryptostats.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsFragment : Fragment() {

    private val newsViewModel: NewsViewModel by activityViewModels()
    private val binding by viewBinding(FragmentNewsBinding::bind)
    private var progressDialog: Dialog? = null
    private var cryptoNewsToInsert = mutableListOf<DataNewsLunarItem>()
    private var allCryptoNews = mutableListOf<DataNewsLunarItem>()
    private var isLoading = false
    private var maxSize = 50


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel.errorCall.observe(viewLifecycleOwner) {
            hideProgress()
            if (!Utils.isNetworkAvailable(requireContext())) {
                cryptoNewsEnabled(false)
            } else {
                cryptoNewsEnabled(true)
            }
        }

        newsViewModel.news.observe(viewLifecycleOwner) {
            it?.let {
                if (!it.isNullOrEmpty()) {
                    allCryptoNews.clear()
                    cryptoNewsToInsert.clear()
                    for (item in it) {
                        var duplicated = false
                        for (news in allCryptoNews) {
                            if (news.image == item.image) {
                                duplicated = true
                            }
                        }
                        if (!duplicated) {
                            allCryptoNews.add(item)
                        }
                    }
                    if (allCryptoNews.size < maxSize) {
                        while (allCryptoNews.size < maxSize) {
                            maxSize = -10
                        }
                        val temp = allCryptoNews
                        allCryptoNews.clear()
                        if (maxSize > 0) {
                            allCryptoNews.addAll(temp.subList(0, maxSize))
                        }
                    }
                    if (maxSize > 0) {
                        initRecycler()
                    } else {
                        newsViewModel.updateErrorCall()
                    }
                }
            }
        }

        showProgress()
        if (Utils.isNetworkAvailable(requireContext())) {
            cryptoNewsEnabled(true)
            newsViewModel.getCryptoNews()
        } else {
            newsViewModel.updateErrorCall()
        }


    }

    private fun initRecycler() {
        binding.rvCryptoNews.layoutManager = LinearLayoutManager(this.requireActivity())
        cryptoNewsToInsert.addAll(allCryptoNews.subList(0, 9))
        binding.rvCryptoNews.adapter =
            CryptoNewsAdapter(cryptoNewsToInsert) { selectedItem: DataNewsLunarItem ->
                listItemClicked(selectedItem)
            }

        binding.rvCryptoNews.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.rvCryptoNews.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    hideProgress()
                }
            })

        binding.rvCryptoNews.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager: LinearLayoutManager =
                    binding.rvCryptoNews.layoutManager as LinearLayoutManager
                if (!isLoading) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == cryptoNewsToInsert.size - 1 && cryptoNewsToInsert.size < maxSize) {
                        loadMore()
                        isLoading = true
                    }

                }
            }
        })
    }

    private fun loadMore() {
        binding.rvCryptoNews.let { rv ->
            cryptoNewsToInsert.add(DataNewsLunarItem(LOADING_ITEM_KEY, "", "", "", "", ""))
            rv.adapter?.notifyItemInserted(cryptoNewsToInsert.size - 1)
            Handler(Looper.getMainLooper()).postDelayed({
                cryptoNewsToInsert.removeAt(cryptoNewsToInsert.size - 1)
                val scrollPosition: Int = cryptoNewsToInsert.size
                rv.adapter?.notifyItemRemoved(scrollPosition)
                var currentSize = scrollPosition
                val nextLimit = currentSize + 10
                while (currentSize < nextLimit) {
                    cryptoNewsToInsert.add(allCryptoNews[currentSize])
                    currentSize++
                }
                rv.adapter?.notifyDataSetChanged()
                isLoading = false
            }, 2000)
        }
    }

    private fun listItemClicked(selectedItem: DataNewsLunarItem) {
        if (Utils.isNetworkAvailable(requireContext())) {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent: CustomTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(selectedItem.url))
        } else {
            newsViewModel.updateErrorCall()
        }
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = CustomProgressDialog.showLoadingDialog(this.requireContext())
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }

    private fun cryptoNewsEnabled(value: Boolean) {
        binding.apply {
            rvCryptoNews.isVisible = value
            llError.isVisible = !value
        }
    }
}