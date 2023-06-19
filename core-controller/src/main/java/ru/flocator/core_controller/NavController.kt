package ru.flocator.core_controller

import android.os.Bundle

interface NavController {

    fun toAuth(): TransactionCommitter

    fun toMain(): TransactionCommitter

    fun toProfile(): TransactionCommitter

    fun toSettings(): TransactionCommitter

    fun toFragment(bundle: Bundle?): TransactionCommitter

    fun back()
}
