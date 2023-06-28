package ru.flocator.feature_community.internal.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import ru.flocator.core_api.api.MainRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_sections.CommunitySection
import ru.flocator.feature_community.repository.CommunityRepository

internal class AddFriendByLinkFragmentViewModel constructor(
    private val repository: CommunityRepository,
) : ViewModel(), CommunitySection {

    private val compositeDisposable = CompositeDisposable()

    fun addFriendByLogin(userId: Long, login: String) {
        compositeDisposable.add(
            repository.addFriendByLogin(userId, login)
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
