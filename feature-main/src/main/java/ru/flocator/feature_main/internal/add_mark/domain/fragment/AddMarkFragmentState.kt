package ru.flocator.feature_main.internal.add_mark.domain.fragment

internal sealed class AddMarkFragmentState {
    object Editing: AddMarkFragmentState()
    object Saving: AddMarkFragmentState()
    object Saved: AddMarkFragmentState()
    class Failed(val cause: Throwable): AddMarkFragmentState()
}