package com.gwyro.cryptostats.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.gwyro.cryptostats.R
import com.gwyro.cryptostats.utils.KEY_NOTIFICATIONS
import com.gwyro.cryptostats.work.NotifyWorker
import java.util.concurrent.TimeUnit

class SettingsFragment : PreferenceFragmentCompat() {

    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        context?.theme?.applyStyle(R.style.Preferences, true)
        preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (val preference = findPreference<Preference>(key)) {
                    is SwitchPreferenceCompat -> {
                        if (key == KEY_NOTIFICATIONS) {
                            if (!preference.isChecked) {
                                WorkManager.getInstance(requireContext()).cancelAllWork()
                            } else {
                                WorkManager.getInstance(requireContext()).cancelAllWork()
                                val periodicWorkRequest =
                                    PeriodicWorkRequest.Builder(
                                        NotifyWorker::class.java,
                                        3,
                                        TimeUnit.HOURS
                                    )
                                        .setInitialDelay(1, TimeUnit.HOURS)
                                        .setConstraints(
                                            Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build()
                                        )
                                        .build()
                                WorkManager.getInstance(requireContext())
                                    .enqueue(periodicWorkRequest)
                            }
                        }
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}