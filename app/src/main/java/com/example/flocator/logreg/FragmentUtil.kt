package com.example.flocator.logreg

import android.view.SurfaceControl.Transaction
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.flocator.R
import com.example.flocator.logreg.fragments.RegNameFragment

class FragmentUtil {
    companion object {
        fun openFragment(transaction: FragmentTransaction, fragment: Fragment) {
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}