package ru.flocator.feature_community.internal.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_sections.CommunitySection
import ru.flocator.feature_community.internal.repository.CommunityRepository
import javax.inject.Inject

internal class AddFriendByLinkFragmentViewModel @Inject constructor(
    private val repository: CommunityRepository
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

    fun checkLogin(login: String): Single<Boolean> {
        return repository.checkLogin(login)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }


    companion object {
        const val TAG = "AddFriendByLogin"
    }
}
