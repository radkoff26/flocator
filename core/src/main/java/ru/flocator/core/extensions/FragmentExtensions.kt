package ru.flocator.core.extensions

import androidx.fragment.app.Fragment
import ru.flocator.core.base.activity.BaseActivity

fun Fragment.baseActivity(): BaseActivity = requireActivity() as BaseActivity