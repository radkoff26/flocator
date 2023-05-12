package com.example.flocator.community.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.UserExternal
import com.example.flocator.community.data_classes.UserExternalFriends
import com.example.flocator.community.fragments.ProfileFragment
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
            repository.restApi.acceptNewFriend(userId, friendId)
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