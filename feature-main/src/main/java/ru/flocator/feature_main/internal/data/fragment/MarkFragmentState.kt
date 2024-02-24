package ru.flocator.feature_main.internal.data.fragment

internal sealed class MarkFragmentState {
    object Loading: MarkFragmentState()
    object Loaded: MarkFragmentState()
    class Failed(val cause: Throwable): MarkFragmentState()
}