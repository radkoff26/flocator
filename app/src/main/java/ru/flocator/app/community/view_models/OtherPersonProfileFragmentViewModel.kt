package ru.flocator.app.community.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.flocator.app.common.repository.MainRepository
import ru.flocator.app.community.api.UserApi
import ru.flocator.app.community.data_classes.UserExternal
import ru.flocator.app.community.data_classes.UserExternalFriends
import ru.flocator.app.community.fragments.ProfileFragment
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class OtherPersonProfileFragmentViewModel constructor(
    private val repository: MainRepository,
) : ViewModel() {
    private val _friendsLiveData = MutableLiveData<MutableList<UserExternalFriends>?>()
    var friendsLiveData: MutableLiveData<MutableList<UserExternalFriends>?> = _friendsLiveData
    private val _currentUserLiveData = MutableLiveData<UserExternal>()
    val currentUserLiveData: LiveData<UserExternal> = _currentUserLiveData
    private val compositeDisposable = CompositeDisposable()

    private val userApi: UserApi by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://kernelpunik.ru:8080/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        retrofit.create()
    }

    fun fetchUser(userId: Long, currentUserId: Long) {
        compositeDisposable.add(
            userApi.getUserExternal(currentUserId , userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        updateFriends(it)
                    },
                    {
                        Log.e(ProfileFragment.TAG, it.message, it)
                    })
        )
    }

    fun addOtherUserToFriend(userId: Long, friendId: Long){
        compositeDisposable.add(
            repository.restApi.addNewFriendByBtn(userId, friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "addFriend ERROR", it)
                    }
                )
        )
    }

    fun deleteMyFriend(userId: Long, friendId: Long){
        compositeDisposable.add(
            repository.restApi.deleteFriendByBtn(userId, friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "deleteFriend ERROR", it)
                    }
                )
        )
    }


    fun acceptFriend(userId: Long, friendId: Long) {
        compositeDisposable.add(
            repository.restApi.acceptNewFriend(userId, friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(ProfileFragmentViewModel.TAG, "acceptFriend ERROR", it)
                    }
                )
        )
    }

    fun block(blockerId: Long, blockedId: Long){
        compositeDisposable.add(
            repository.restApi.blockUserByBtn(blockerId, blockedId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(ProfileFragmentViewModel.TAG, "blockUser ERROR", it)
                    }
                )
        )
    }

    fun unblock(blockerId: Long, blockedId: Long){
        compositeDisposable.add(
            repository.restApi.unblockUserByBtn(blockerId, blockedId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(ProfileFragmentViewModel.TAG, "unblockUser ERROR", it)
                    }
                )
        )
    }

    fun cancelFriendRequest(userId: Long, friendId: Long){
        compositeDisposable.add(
            repository.restApi.rejectNewFriend(userId, friendId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "cancelFriendRequest ERROR", it)
                    }
                )
        )
    }

    private fun updateFriends(user: UserExternal) {
        _currentUserLiveData.value = user
        _friendsLiveData.value = user.friends
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object{
        const val TAG = "AddFriend"
    }
}