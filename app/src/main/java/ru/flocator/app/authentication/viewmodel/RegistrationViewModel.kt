package ru.flocator.app.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.app.authentication.authorization.AuthFragment
import ru.flocator.app.community.view_models.ProfileFragmentViewModel
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_dto.auth.UserRegistrationDto
import javax.inject.Inject

@HiltViewModel
@Suppress("UNCHECKED_CAST")
class RegistrationViewModel @Inject constructor(
    private val repository: MainRepository
)  : ViewModel() {
    val nameData = MutableLiveData<Pair<String, String>>() // Фамилия, Имя
    val loginEmailData = MutableLiveData<Pair<String, String>>() // Login, Email
    private val compositeDisposable = CompositeDisposable()
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun registerUser(userRegistrationDto: UserRegistrationDto) {
        compositeDisposable.add(
            repository.restApi.registerUser(userRegistrationDto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        return@subscribe
                    },
                    {
                        Log.e(TAG, "registration ERROR", it)
                    }
                )
        )
    }

    companion object {
        const val TAG = "RegisterFragment"
    }
}