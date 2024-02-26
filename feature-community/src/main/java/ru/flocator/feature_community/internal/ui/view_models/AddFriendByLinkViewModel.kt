package ru.flocator.feature_community.internal.ui.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.feature_community.internal.data.repository.FriendshipRepository
import javax.inject.Inject

internal class AddFriendByLinkViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    fun addFriendByLogin(login: String) {
        compositeDisposable.add(
            friendshipRepository.addFriendByLogin(login)
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

    fun checkLogin(login: String): Single<Boolean> {
        return friendshipRepository.checkLogin(login)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }


    companion object {
        const val TAG = "AddFriendByLinkViewModel_TAG"
    }
}
