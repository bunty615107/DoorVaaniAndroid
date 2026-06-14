package com.doorvaani.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doorvaani.domain.model.RecentCall
import com.doorvaani.domain.model.UserPreferences
import com.doorvaani.domain.repository.FakePreferencesRepository
import com.doorvaani.domain.repository.FakeRecentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Lightweight VM for HomeDashboard + Focus toggles (Phase 0).
 * Uses Fake repo for prefs + recents samples.
 */
class SimpleHomeViewModel(
    private val prefsRepo: FakePreferencesRepository = FakePreferencesRepository
) : ViewModel() {

    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> = _prefs.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.preferences.collect { _prefs.value = it }
        }
    }

    fun toggleDharma() {
        prefsRepo.toggleDharmaMode()
    }

    fun toggleVastu() {
        prefsRepo.toggleVastuDialing()
    }

    fun setTheme(isSangam: Boolean) {
        prefsRepo.setThemeSangam(isSangam)
    }

    // Phase 3 extensions
    fun toggleCloudSync() {
        prefsRepo.toggleCloudSync()
    }

    fun toggleCommunityFederation() {
        prefsRepo.toggleCommunityFederation()
    }

    fun togglePremiumAi() {
        prefsRepo.togglePremiumAi()
    }

    fun getRecentCalls(): List<RecentCall> = FakeRecentsRepository.getRecentCalls(5)

    fun getWeeklyFlow(): Int = FakeRecentsRepository.getWeeklyFlowCount()
}
