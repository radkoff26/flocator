package com.example.flocator.community.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.community.CommunitySection
import com.example.flocator.main.MainSection
import com.example.flocator.main.ui.mark.MarkFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AddFriendByLinkFragmentViewModel constructor(
    private val repository: MainRepository,
) : ViewModel(), CommunitySection {

    private val compositeDisposable = CompositeDisposable()

    fun addFriendByLogin(userId: Long, login: String) {
        compositeDisposable.add(
            repository.restApi.addFriendByLogin(userId, login)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "addFriendByLogin ERROR", it)
                    }
                )
        )
    }
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }


    companion object{
        const val TAG = "AddFriendByLogin"
    }
}
