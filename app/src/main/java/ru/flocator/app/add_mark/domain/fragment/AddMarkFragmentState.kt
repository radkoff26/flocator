package ru.flocator.app.add_mark.domain.fragment


sealed class AddMarkFragmentState {
    object Editing: AddMarkFragmentState()
    object Saving: AddMarkFragmentState()
    object Saved: AddMarkFragmentState()
    class Failed(val cause: Throwable): AddMarkFragmentState()
}