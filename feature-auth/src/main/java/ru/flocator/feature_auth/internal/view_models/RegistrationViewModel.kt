package ru.flocator.feature_auth.internal.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core_dto.auth.UserRegistrationDto
import ru.flocator.feature_auth.internal.di.annotations.FragmentScope
import ru.flocator.feature_auth.internal.repository.AuthRepository
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
@FragmentScope
internal class RegistrationViewModel @Inject constructor(
    private val repository: AuthRepository
    ) : ViewModel() {
    private val _nameData = MutableLiveData<Pair<String, String>?>() // Фамилия, Имя
    private val _loginEmailData = MutableLiveData<Pair<String, String>?>() // Login, Email
    var nameData: MutableLiveData<Pair<String, String>?> = _nameData
    var loginEmailData: MutableLiveData<Pair<String, String>?> = _loginEmailData
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

    fun clear(){
        _nameData.postValue(null)
        _loginEmailData.postValue(null)
    }

    fun registerUser(userRegistrationDto: UserRegistrationDto): Single<Boolean> {
        return repository.registerUser(userRegistrationDto)
    }

    fun isLoginAvailable(login: String): Single<Boolean>{
        return repository.isLoginAvailable(login)
    }

    fun isEmailAvailable(email: String): Single<Boolean>{
        return repository.isEmailAvailable(email)
    }
}