package com.example.flocator.authreg

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.flocator.R

class FragmentUtil {
    companion object {
        fun replaceFragment(transaction: FragmentTransaction, fragment: Fragment) {
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}