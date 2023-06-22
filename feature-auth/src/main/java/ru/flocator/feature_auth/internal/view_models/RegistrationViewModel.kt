package ru.flocator.feature_auth.internal.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_dto.auth.UserRegistrationDto
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
internal class RegistrationViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    private val _nameData = MutableLiveData<Pair<String, String>>() // Фамилия, Имя
    private val _loginEmailData = MutableLiveData<Pair<String, String>>() // Login, Email
    var nameData: LiveData<Pair<String, String>> = _nameData
    var loginEmailData: LiveData<Pair<String, String>> = _loginEmailData
    private val compositeDisposable = CompositeDisposable()
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun updateNameData(nameData: Pair<String, String>) {
        _nameData.postValue(nameData)
    }

    fun updateLoginEmail(loginEmail: Pair<String, String>) {
        _loginEmailData.postValue(loginEmail)
    }

    fun registerUser(userRegistrationDto: UserRegistrationDto): Single<Boolean> {
        return repository.restApi.registerUser(userRegistrationDto)
    }

    companion object {
        const val TAG = "RegisterFragment"
    }
}