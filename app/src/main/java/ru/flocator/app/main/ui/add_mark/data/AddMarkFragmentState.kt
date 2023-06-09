package ru.flocator.app.main.ui.add_mark.data


sealed class AddMarkFragmentState {
    object Editing: AddMarkFragmentState()
    object Saving: AddMarkFragmentState()
    object Saved: AddMarkFragmentState()
    class Failed(val cause: Throwable): AddMarkFragmentState()
}