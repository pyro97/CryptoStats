package com.gwyro.cryptostats.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gwyro.cryptostats.data.db.UserCryptoRepo
import com.gwyro.cryptostats.data.model.DataNewsLunarItem
import com.gwyro.cryptostats.domain.storage.SharedPreferenceStorage
import com.gwyro.cryptostats.domain.usecase.UseCaseCryptoInfo
import com.gwyro.cryptostats.ui.home.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val useCaseCryptoInfo: UseCaseCryptoInfo,
    userCryptoRepo: UserCryptoRepo,
    sharedPreferencesStorage: SharedPreferenceStorage,
) : HomeViewModel(useCaseCryptoInfo, userCryptoRepo, sharedPreferencesStorage) {

    private val _news = MutableLiveData<MutableList<DataNewsLunarItem>>()
    val news: LiveData<MutableList<DataNewsLunarItem>>
        get() = _news

    fun getCryptoNews() {
        _news.value?.clear()
        viewModelScope.launch {
            when (val result = useCaseCryptoInfo.getCryptoNews()) {
                is com.gwyro.cryptostats.utils.Result.Success -> {
                    result.data.let {
                        it.data.let { list ->
                            _news.postValue(list.toMutableList())
                        }
                    }
                }
                else -> _errorCall.postValue(true)
            }
        }
    }

}