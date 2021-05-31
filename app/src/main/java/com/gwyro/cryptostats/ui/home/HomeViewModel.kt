package com.gwyro.cryptostats.ui.home

import androidx.lifecycle.*
import com.gwyro.cryptostats.data.db.UserCrypto
import com.gwyro.cryptostats.data.db.UserCryptoRepo
import com.gwyro.cryptostats.data.model.LunarDetailItem
import com.gwyro.cryptostats.data.model.LunarItem
import com.gwyro.cryptostats.data.model.NomicsItem
import com.gwyro.cryptostats.domain.entities.CryptoItem
import com.gwyro.cryptostats.domain.storage.SharedPreferenceStorage
import com.gwyro.cryptostats.domain.usecase.UseCaseCryptoInfo
import com.gwyro.cryptostats.utils.KEY_CURRENCIES
import com.gwyro.cryptostats.utils.KEY_NOTIFICATIONS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val useCaseCryptoInfo: UseCaseCryptoInfo,
    private val userCryptoRepo: UserCryptoRepo,
    private val sharedPreferencesStorage: SharedPreferenceStorage,
) : ViewModel(), LifecycleObserver {

    private val _cryptoItem = MutableLiveData<MutableList<CryptoItem>>()
    val cryptoItem: LiveData<MutableList<CryptoItem>>
        get() = _cryptoItem

    private val _dayCrypto = MutableLiveData<MutableList<CryptoItem>>()
    val dayCrypto: LiveData<MutableList<CryptoItem>>
        get() = _dayCrypto

    private var nomics: List<NomicsItem> = listOf()

    private var nomicsDay: MutableList<NomicsItem> = mutableListOf()

    private val _allCrypto = MutableLiveData<MutableList<CryptoItem>>()
    val allCrypto: LiveData<MutableList<CryptoItem>>
        get() = _allCrypto

    private val _lunar = MutableLiveData<LunarItem>()
    val lunar: LiveData<LunarItem>
        get() = _lunar

    private val _lunarDay = MutableLiveData<LunarItem>()
    val lunaraDay: LiveData<LunarItem>
        get() = _lunarDay

    val _errorCall = MutableLiveData<Boolean>()
    val errorCall: LiveData<Boolean>
        get() = _errorCall

    private val _emptyCrypto = MutableLiveData<Boolean>()
    val emptyCrypto: LiveData<Boolean>
        get() = _emptyCrypto

    private val _userCrypto = MutableLiveData<MutableList<UserCrypto>>()
    val userCrypto: LiveData<MutableList<UserCrypto>>
        get() = _userCrypto

    private val _updateCryptoList = MutableLiveData<Boolean>()
    val updateCryptoList: LiveData<Boolean>
        get() = _updateCryptoList

    private val _detailCryptoItem = MutableLiveData<LunarDetailItem>()
    val detailCryptoItem: LiveData<LunarDetailItem>
        get() = _detailCryptoItem

    fun fillList() {
        _cryptoItem.value?.clear()
        val lista = mutableListOf<CryptoItem>()
        nomics.let { nm ->
            nm.let {
                for (item in nm) {
                    val crytpos = CryptoItem(
                        0,
                        item.name,
                        item.currency,
                        item.price,
                        0.0,
                        item.logo_url,
                        item.circulating_supply,
                        item.market_cap,
                        item.max_supply,
                        true
                    )
                    _lunar.let { lun ->
                        val lunData = lun.value?.data
                        lunData?.let { dataLun ->
                            for (lunarItem in dataLun) {
                                if (lunarItem.symbol == crytpos.currency) {
                                    crytpos.percent_change_24h = lunarItem.percent_change_24h
                                }
                            }
                        }

                    }
                    lista.add(crytpos)
                }
                _cryptoItem.value = lista

            }

        }
    }

    fun fillListCoiOfTheDay() {
        val lista = mutableListOf<CryptoItem>()
        nomicsDay.let { nm ->
            nm.let {
                for (item in nm) {
                    val crytpos = CryptoItem(
                        0,
                        item.name,
                        item.currency,
                        item.price,
                        0.0,
                        item.logo_url,
                        item.circulating_supply,
                        item.market_cap,
                        item.max_supply,
                        true
                    )
                    _lunarDay.let { lun ->
                        val lunData = lun.value?.data
                        lunData?.let { dataLun ->
                            for (lunarItem in dataLun) {
                                if (lunarItem.symbol == crytpos.currency) {
                                    crytpos.percent_change_24h = lunarItem.percent_change_24h
                                }
                            }
                        }

                    }
                    lista.add(crytpos)
                }
                _dayCrypto.value = lista
            }

        }
    }

    fun getCryptoList(symbols: String, currency: String, isCoinOfTheDay: Boolean) {
        viewModelScope.launch {
            when (val result = useCaseCryptoInfo.getCryptoValueNomics(symbols, currency)) {
                is com.gwyro.cryptostats.utils.Result.Success -> {
                    result.data.let {
                        if (isCoinOfTheDay) {
                            nomicsDay = it.toMutableList()
                        } else {
                            nomics = it
                        }
                    }
                }
                else -> _errorCall.postValue(true)
            }

            when (val resultLunar = useCaseCryptoInfo.getCryptoValueLunar(symbols)) {
                is com.gwyro.cryptostats.utils.Result.Success -> {
                    resultLunar.data.let {
                        if (isCoinOfTheDay) {
                            _lunarDay.postValue(it)
                        } else {
                            _lunar.postValue(it)
                        }
                    }
                }
                else -> {
                    _errorCall.postValue(true)
                    if (isCoinOfTheDay) {
                        fillList()
                    } else {
                        fillListCoiOfTheDay()
                    }
                }
            }
        }
    }

    fun isUserCryptoEmpty() {
        viewModelScope.launch {
            _emptyCrypto.postValue(userCryptoRepo.getAllCrypto().isEmpty())
        }
    }

    fun addUserCrypto(cryptoItem: UserCrypto) {
        viewModelScope.launch {
            if (userCryptoRepo.insertCrypto(cryptoItem) > -1) {
                _updateCryptoList.postValue(true)
            } else {
                _updateCryptoList.postValue(false)
            }
        }
    }

    fun updateUserCrypto(cryptoItem: UserCrypto) {
        viewModelScope.launch {
            if (userCryptoRepo.updateCrypto(cryptoItem) > 0) {
                _updateCryptoList.postValue(true)
            } else {
                _updateCryptoList.postValue(false)
            }
        }
    }

    fun getUserCrypto() {
        _userCrypto.value?.clear()
        viewModelScope.launch {
            _userCrypto.postValue(userCryptoRepo.getAllCrypto().toMutableList())
        }
    }

    fun deleteUserCrypto(cryptoCurrency: String) {
        viewModelScope.launch {
            if (userCryptoRepo.deleteCrypto(cryptoCurrency) > 0) {
                _updateCryptoList.postValue(true)
            } else {
                _updateCryptoList.postValue(false)
            }
        }
    }

    fun updateScrollDown() {
        _updateCryptoList.value = true
    }

    fun getCryptoDetails(symbols: String) {
        viewModelScope.launch {
            when (val result = useCaseCryptoInfo.getCryptoDetail(symbols)) {
                is com.gwyro.cryptostats.utils.Result.Success -> {
                    result.data.let {
                        _detailCryptoItem.postValue(it)
                    }
                }
                else -> _errorCall.postValue(true)
            }
        }
    }

    fun getAllCrypto(convert: String) {
        _allCrypto.value?.clear()
        viewModelScope.launch {
            when (val result = useCaseCryptoInfo.getCryptoList(convert)) {
                is com.gwyro.cryptostats.utils.Result.Success -> {
                    val list = result.data
                    val lista = mutableListOf<CryptoItem>()
                    for (item in list) {
                        val crytpos = CryptoItem(
                            0,
                            item.name,
                            item.currency,
                            item.price,
                            0.0,
                            item.logo_url,
                            "",
                            "",
                            "",
                            false
                        )
                        lista.add(crytpos)
                    }
                    _allCrypto.postValue(lista)
                }
                else -> _errorCall.postValue(true)
            }
        }
    }

    fun getCoinOfTheDay() {
        _dayCrypto.value?.clear()
        nomicsDay.clear()
        _errorCall.value = false
        viewModelScope.launch {
            when (val result = useCaseCryptoInfo.getCryptoOfTheDay()) {
                is com.gwyro.cryptostats.utils.Result.Success -> {
                    getCryptoList(result.data.data.symbol, getCurrency(), true)
                }
                else -> _errorCall.postValue(true)
            }
        }
    }

    fun updateErrorCall() {
        _errorCall.value = true
    }

    fun updateCryptoList() {
        _updateCryptoList.value = true
    }

    fun getCurrency(): String {
        var curr = "USD"
        sharedPreferencesStorage.getString(KEY_CURRENCIES)?.let {
            curr = it
        }
        return curr
    }

    fun isNotificationEnabled(): Boolean {
        return sharedPreferencesStorage.getBoolean(KEY_NOTIFICATIONS)
    }

    fun setDefaultValues() {
        sharedPreferencesStorage.checkDefaultValues()
    }

}