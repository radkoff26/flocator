package ru.flocator.core.base.fragment

import androidx.fragment.app.Fragment
import ru.flocator.core.base.activity.BaseActivity

abstract class BaseFragment: Fragment() {

    fun activity(): BaseActivity = requireActivity() as BaseActivity
}