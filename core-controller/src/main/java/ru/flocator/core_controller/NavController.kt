package ru.flocator.core_controller

import android.os.Bundle
import androidx.fragment.app.Fragment

interface NavController {

    fun toAuth(): TransactionCommitter

    fun toMain(): TransactionCommitter

    fun toProfile(): TransactionCommitter

    fun toSettings(): TransactionCommitter

    fun toFragment(fragment: Fragment): TransactionCommitter

    fun back()
}
