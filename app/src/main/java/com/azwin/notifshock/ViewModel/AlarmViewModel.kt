package com.azwin.notifshock.ViewModel

import androidx.lifecycle.ViewModel
import com.azwin.notifshock.Model.AlarmModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmViewModel(private val repository: AlarmModel) : ViewModel() {

    private val _keyword = MutableStateFlow("")
    val keyword = _keyword.asStateFlow()

    init {
        // Initialize state with persisted data
        _keyword.value = repository.getTargetKeyword()
    }

    fun updateKeyword(newKeyword: String) {
        _keyword.value = newKeyword
    }

    fun saveKeyword() {
        repository.saveTargetKeyword(_keyword.value)
    }
}
