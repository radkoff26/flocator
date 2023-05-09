package com.example.flocator.community.view_models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.Friends
import com.example.flocator.community.data_classes.User
import com.example.flocator.community.fragments.ProfileFragment
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Inject

class OtherPersonProfileFragmentViewModel: ViewModel() {
    private val _friendsLiveData = MutableLiveData<MutableList<Friends>?>()
    var friendsLiveData: MutableLiveData<MutableList<Friends>?> = _friendsLiveData

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

    fun fetchUser(userId: Long) {
        userApi.getUser(userId)
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

    private fun updateFriends(user: User) {
        _friendsLiveData.value = user.friends
    }
}