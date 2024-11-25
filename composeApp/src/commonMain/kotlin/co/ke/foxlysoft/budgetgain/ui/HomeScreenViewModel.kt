package co.ke.foxlysoft.budgetgain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.foxlysoft.budgetgain.database.SettingsEntity
import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
private val settingsRepository: SettingsRepository
): ViewModel() {

    val firstTime: StateFlow<SettingsEntity> = settingsRepository.getSetting("firstTime")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    suspend fun setToFalse() {
        settingsRepository.setSetting("firstTime", "false")
    }

    init {
        viewModelScope.launch {
            settingsRepository.setSetting("firstTime", "true")
        }
    }
}