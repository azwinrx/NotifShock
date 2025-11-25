package com.azwin.notifshock.ViewModel

import androidx.lifecycle.ViewModel
import com.azwin.notifshock.Model.AlarmModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmViewModel(private val repository: AlarmModel) : ViewModel() {

    private val _keyword = MutableStateFlow("")
    val keyword = _keyword.asStateFlow()

    private val _isTelegramEnabled = MutableStateFlow(true)
    val isTelegramEnabled = _isTelegramEnabled.asStateFlow()

    private val _isWhatsappEnabled = MutableStateFlow(true)
    val isWhatsappEnabled = _isWhatsappEnabled.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    init {
        // Initialize state with persisted data
        _keyword.value = repository.getTargetKeyword()
        _isTelegramEnabled.value = repository.isAppEnabled("APP_TELEGRAM")
        _isWhatsappEnabled.value = repository.isAppEnabled("APP_WHATSAPP")
        _isLocked.value = repository.getLockState()
    }

    fun updateKeyword(newKeyword: String) {
        _keyword.value = newKeyword
    }

    fun saveKeyword() {
        repository.saveTargetKeyword(_keyword.value)
        setLocked(true) // Lock when saved
    }

    fun setLocked(locked: Boolean) {
        _isLocked.value = locked
        repository.saveLockState(locked)
    }

    fun toggleTelegram(enabled: Boolean) {
        _isTelegramEnabled.value = enabled
        repository.setAppEnabled("APP_TELEGRAM", enabled)
    }

    fun toggleWhatsapp(enabled: Boolean) {
        _isWhatsappEnabled.value = enabled
        repository.setAppEnabled("APP_WHATSAPP", enabled)
    }
}
