package ru.flocator.core.base.view_model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<S> : ViewModel() {
    private val mutableUiStateFlow: MutableStateFlow<UiState<S>> =
        MutableStateFlow(UiState.Loading())
    val uiState: StateFlow<UiState<S>> = mutableUiStateFlow

    abstract fun loadData()

    protected fun toLoading() {
        mutableUiStateFlow.value = UiState.Loading()
    }

    protected fun toLoaded(data: S) {
        mutableUiStateFlow.value = UiState.Loaded(data)
    }

    protected fun toFailed(throwable: Throwable) {
        mutableUiStateFlow.value = UiState.Failed(throwable)
    }
}