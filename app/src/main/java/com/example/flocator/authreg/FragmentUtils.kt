package com.example.flocator.authreg

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.flocator.R

class FragmentUtils {
    companion object {
        fun replaceFragment(fragmentManager: FragmentManager, fragment: Fragment) {
            fragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
                commit()
            }
        }
        fun addFragment(fragmentManager: FragmentManager, fragment: Fragment) {
            fragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }
}