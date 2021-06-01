package com.gwyro.cryptostats.ui.home

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crazylegend.viewbinding.viewBinding
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.common.CustomProgressDialog
import com.gwyro.cryptostats.data.db.UserCrypto
import com.gwyro.cryptostats.databinding.FragmentListCryptoBinding
import com.gwyro.cryptostats.domain.entities.CryptoItem
import com.gwyro.cryptostats.ui.home.adapter.CryptoListAdapter
import com.gwyro.cryptostats.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CryptoListFragment : Fragment() {
    private var progressDialog: Dialog? = null
    private val binding by viewBinding(FragmentListCryptoBinding::bind)
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var userCrypto = mutableListOf<UserCrypto>()
    private var allCryptoList = mutableListOf<CryptoItem>()
    private var cryptoListToInsert = mutableListOf<CryptoItem>()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        findNavController().popBackStack()
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_crypto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserCrypto()
        homeViewModel.errorCall.observe(viewLifecycleOwner) {
            hideProgress()
            if (!Utils.isNetworkAvailable(requireContext())) {
                cryptoListEnabled(false)
            }
        }

        homeViewModel.userCrypto.observe(viewLifecycleOwner) {
            it?.let {
                userCrypto.clear()
                userCrypto.addAll(it.toMutableList())
            }
            showProgress()
            if (Utils.isNetworkAvailable(requireContext())) {
                homeViewModel.getAllCrypto(homeViewModel.getCurrency())
            } else {
                homeViewModel.updateErrorCall()
            }
        }

        homeViewModel.allCrypto.observe(viewLifecycleOwner) {
            it?.let { list ->
                if (userCrypto.isNullOrEmpty()) {
                    allCryptoList.addAll(it)
                } else {
                    for (item in list) {
                        for (fav in userCrypto) {
                            if (item.currency == fav.currency) {
                                item.favourite = true
                            }
                        }
                        allCryptoList.add(item)
                    }
                }
                if (!allCryptoList.isNullOrEmpty()) {
                    initRecycler()
                }
            }
        }

        homeViewModel.errorCall.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    hideProgress()
                }
            }
        }

        homeViewModel.updateCryptoList.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    hideProgress()
                } else {
                    hideProgress()
                }
            }
        }

        binding.llError.setOnClickListener {
            getUserCrypto()
        }


    }

    private fun getUserCrypto() {
        if (Utils.isNetworkAvailable(requireContext())) {
            cryptoListEnabled(true)
            homeViewModel.getUserCrypto()
        } else {
            homeViewModel.updateErrorCall()
        }
    }

    private fun initRecycler() {
        binding.rvCryptoList.layoutManager = LinearLayoutManager(this.requireActivity())
        cryptoListToInsert.addAll(allCryptoList.subList(0, 9))
        binding.rvCryptoList.adapter =
            CryptoListAdapter(cryptoListToInsert,homeViewModel.getCurrency()) { selectedItem: CryptoItem ->
                listItemClicked(selectedItem)
            }

        binding.rvCryptoList.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.rvCryptoList.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    hideProgress()
                }
            })

        binding.rvCryptoList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager: LinearLayoutManager =
                    binding.rvCryptoList.layoutManager as LinearLayoutManager
                if (!isLoading) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == cryptoListToInsert.size - 1 && cryptoListToInsert.size < 99) {
                        loadMore()
                        isLoading = true
                    }

                }
            }
        })
    }

    private fun listItemClicked(selectedItem: CryptoItem) {
        if (Utils.isNetworkAvailable(requireContext())) {
            if (selectedItem.favourite) {
                homeViewModel.addUserCrypto(
                    UserCrypto(
                        0,
                        selectedItem.name!!,
                        selectedItem.currency!!,
                        true
                    )
                )
            } else {
                homeViewModel.deleteUserCrypto(selectedItem.currency!!)
            }
        } else {
            homeViewModel.updateErrorCall()
        }
    }

    private fun loadMore() {
        binding.rvCryptoList.let { rv ->
            cryptoListToInsert.add(CryptoItem(-1, "", "", "", 0.0, "", "", "", "", false))
            rv.adapter?.notifyItemInserted(cryptoListToInsert.size - 1)
            Handler(Looper.getMainLooper()).postDelayed({
                cryptoListToInsert.removeAt(cryptoListToInsert.size - 1)
                val scrollPosition: Int = cryptoListToInsert.size
                rv.adapter?.notifyItemRemoved(scrollPosition)
                var currentSize = scrollPosition
                val nextLimit = currentSize + 10
                while (currentSize < nextLimit) {
                    cryptoListToInsert.add(allCryptoList[currentSize])
                    currentSize++
                }
                rv.adapter?.notifyDataSetChanged()
                isLoading = false
            }, 2000)
        }
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = CustomProgressDialog.showLoadingDialog(this.requireContext())
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }

    private fun cryptoListEnabled(value: Boolean) {
        binding.apply {
            rvCryptoList.isVisible = value
            llError.isVisible = !value
        }
    }

}