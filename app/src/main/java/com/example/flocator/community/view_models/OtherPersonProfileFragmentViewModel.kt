package com.example.flocator.community.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.UserExternal
import com.example.flocator.community.data_classes.UserExternalFriends
import com.example.flocator.community.fragments.ProfileFragment
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class OtherPersonProfileFragmentViewModel: ViewModel() {
    private val _friendsLiveData = MutableLiveData<MutableList<UserExternalFriends>?>()
    var friendsLiveData: MutableLiveData<MutableList<UserExternalFriends>?> = _friendsLiveData
    private val _currentUserLiveData = MutableLiveData<UserExternal>()
    val currentUserLiveData: LiveData<UserExternal> = _currentUserLiveData

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
    }

    private fun updateFriends(user: UserExternal) {
        _currentUserLiveData.value = user
        _friendsLiveData.value = user.friends
    }
}