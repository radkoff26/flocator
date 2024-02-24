package ru.flocator.core.base.view_model

sealed class UiState<S> {

    class Loading<S> : UiState<S>()

    data class Loaded<S>(val data: S): UiState<S>()

    data class Failed<S>(val throwable: Throwable): UiState<S>()
}
