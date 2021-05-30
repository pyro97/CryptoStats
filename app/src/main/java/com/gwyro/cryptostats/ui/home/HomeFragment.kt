package com.gwyro.cryptostats.ui.home

import android.app.Dialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.crazylegend.viewbinding.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.common.CustomProgressDialog
import com.gwyro.cryptostats.data.db.UserCrypto
import com.gwyro.cryptostats.databinding.FragmentHomeBinding
import com.gwyro.cryptostats.domain.entities.CryptoItem
import com.gwyro.cryptostats.ui.home.adapter.WatchListAdapterAdapter
import com.gwyro.cryptostats.utils.Utils
import com.gwyro.cryptostats.utils.workers.NotifyWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class HomeFragment : Fragment() {

    enum class HomeContentEnum {
        RECYCLER_VIEW, ERROR, EMPTY
    }

    private var progressDialog: Dialog? = null

    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var deleteIcon: Drawable
    private lateinit var colorDrawableBackground: ColorDrawable
    private var changeColor = false


    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.setDefaultValues()
        setHasOptionsMenu(true)
        deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!
        colorDrawableBackground = ColorDrawable(resources.getColor(R.color.red, null))
        if (homeViewModel.isNotificationEnabled()) {
            scheduleNotification()
        }
    }

    private fun scheduleNotification() {
        WorkManager.getInstance(requireContext()).cancelAllWork()
        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotifyWorker::class.java, 3, TimeUnit.HOURS)
                .setInitialDelay(3, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        WorkManager.getInstance(requireContext()).enqueue(periodicWorkRequest)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToCryptoList()
        )
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_app_bar, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclcer()
        createCryptoRecycler()
        homeViewModel.lunar.observe(viewLifecycleOwner) {
            homeViewModel.fillList()
        }

        homeViewModel.errorCall.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            hideProgress()
            if (!Utils.isNetworkAvailable(requireContext())) {
                setHomeContent(HomeContentEnum.ERROR)
            }
        }

        homeViewModel.emptyCrypto.observe(viewLifecycleOwner) {
            it?.let {
                initCryptoList(it)
            }
        }

        homeViewModel.updateCryptoList.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    createCryptoRecycler()
                }
            }
        }

        homeViewModel.userCrypto.observe(viewLifecycleOwner) {
            it?.let { list ->
                val stringBuilder = StringBuilder()
                for ((size, item) in list.withIndex()) {
                    if (size == 0) {
                        stringBuilder.append(item.currency)
                    } else {
                        stringBuilder.append("," + item.currency)
                    }
                }
                if (stringBuilder.toString()
                        .isNotEmpty() && Utils.isNetworkAvailable(requireContext())
                ) {
                    homeViewModel.getCryptoList(
                        stringBuilder.toString(),
                        homeViewModel.getCurrency(),
                        false
                    )
                } else {
                    homeViewModel.updateErrorCall()
                }
            }
        }
        binding.llError.setOnClickListener {
            if (Utils.isNetworkAvailable(requireContext())){
                homeViewModel.updateCryptoList()
            }
        }
    }

    private fun createCryptoRecycler() {
        homeViewModel.isUserCryptoEmpty()
    }


    private fun initCryptoList(emptyCrypto: Boolean) {
        if (Utils.isNetworkAvailable(requireContext())) {
            if (!emptyCrypto) {
                setHomeContent(HomeContentEnum.RECYCLER_VIEW)
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    showProgress()
                }
                homeViewModel.getUserCrypto()
            } else {
                setHomeContent(HomeContentEnum.EMPTY)
                binding.swipeRefreshLayout.isRefreshing = false
                hideProgress()
            }
        } else {
            setHomeContent(HomeContentEnum.ERROR)
        }

    }

    private fun initRecyclcer() {
        displayList()
    }

    private fun displayList() {
        homeViewModel.cryptoItem.observe(viewLifecycleOwner) {
            mIth.attachToRecyclerView(binding.rvWatch)

            binding.rvWatch.layoutManager = LinearLayoutManager(this.requireActivity())
            binding.swipeRefreshLayout.let { refreshLayout ->
                refreshLayout.setOnRefreshListener {
                    if (Utils.isNetworkAvailable(requireContext())) {
                        homeViewModel.updateScrollDown()
                    } else {
                        homeViewModel.updateErrorCall()
                    }
                }
            }
            binding.rvWatch.adapter = WatchListAdapterAdapter(
                it,
                homeViewModel.getCurrency()
            ) { selectedItem: CryptoItem ->
                listItemClicked(
                    selectedItem
                )
            }
            hideProgress()
            if (changeColor) {
                changeColor = false
                colorDrawableBackground.color = Color.RED
                deleteIcon.setTint(Color.WHITE)
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = CustomProgressDialog.showLoadingDialog(this.requireContext())
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }


    private fun listItemClicked(cryptoItem: CryptoItem) {
        if (Utils.isNetworkAvailable(requireContext())) {
            val action = HomeFragmentDirections.actionHomeToDetailCrypto()
            cryptoItem.currency?.let {
                action.currency = it
            }
            findNavController().navigate(
                action
            )
        } else {
            homeViewModel.updateErrorCall()
        }
    }

    private fun setHomeContent(content: HomeContentEnum) {
        binding.apply {
            rvWatch.isVisible = false
            llItems.isVisible = false
            llError.isVisible = false

            when (content) {
                HomeContentEnum.RECYCLER_VIEW -> rvWatch.isVisible = true
                HomeContentEnum.ERROR -> llError.isVisible = true
                HomeContentEnum.EMPTY -> llItems.isVisible = true
            }
        }
    }

    private var mIth = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMarginVertical =
                    (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX > 0) {
                    colorDrawableBackground.setBounds(
                        itemView.left,
                        itemView.top,
                        dX.toInt(),
                        itemView.bottom
                    )
                    deleteIcon.setBounds(
                        itemView.left + iconMarginVertical,
                        itemView.top + iconMarginVertical,
                        itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth,
                        itemView.bottom - iconMarginVertical
                    )
                } else {
                    colorDrawableBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    deleteIcon.setBounds(
                        itemView.right - iconMarginVertical - deleteIcon.intrinsicWidth,
                        itemView.top + iconMarginVertical,
                        itemView.right - iconMarginVertical,
                        itemView.bottom - iconMarginVertical
                    )
                    deleteIcon.level = 0
                }

                colorDrawableBackground.draw(c)

                c.save()

                if (dX > 0)
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                else
                    c.clipRect(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )

                deleteIcon.draw(c)

                c.restore()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder, target: ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                // remove from adapter
                changeColor = true
                colorDrawableBackground.color = Color.BLACK
                deleteIcon.setTint(Color.BLACK)
                if (Utils.isNetworkAvailable(requireContext())) {
                    val list = homeViewModel.cryptoItem.value
                    val item = list?.get(viewHolder.bindingAdapterPosition)
                    item?.let {
                        it.currency?.let { currency ->
                            viewHolder.itemView.background = colorDrawableBackground
                            homeViewModel.deleteUserCrypto(currency)
                            Snackbar.make(
                                viewHolder.itemView,
                                "${item.name} ${resources.getString(R.string.item_removed)}",
                                Snackbar.LENGTH_LONG
                            ).setAction(getString(R.string.undo_item)) {
                                if (Utils.isNetworkAvailable(requireContext())) {
                                    homeViewModel.addUserCrypto(
                                        UserCrypto(
                                            0,
                                            item.name!!,
                                            item.currency!!,
                                            true
                                        )
                                    )
                                } else {
                                    homeViewModel.updateErrorCall()
                                }
                            }.setActionTextColor(Color.BLACK).setTextColor(Color.BLACK)
                                .setBackgroundTint(Color.WHITE).show()
                        }
                    }
                } else {
                    homeViewModel.updateErrorCall()
                }
            }
        })

}