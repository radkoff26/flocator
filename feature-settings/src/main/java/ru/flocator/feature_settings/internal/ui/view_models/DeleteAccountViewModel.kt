package ru.flocator.feature_settings.internal.ui.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.feature_settings.internal.data.repository.SettingsRepository
import javax.inject.Inject

internal class DeleteAccountViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _errorStatusLiveData: MutableLiveData<UiErrorStatus?> = MutableLiveData(null)
    val errorStatusLiveData: LiveData<UiErrorStatus?>
        get() = _errorStatusLiveData

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun deleteAccount(password: String, onSuccess: () -> Unit) {
        compositeDisposable.add(
            settingsRepository.deleteCurrentAccount(password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it) {
                            _errorStatusLiveData.value = null
                            onSuccess()
                        } else {
                            _errorStatusLiveData.value = UiErrorStatus.WRONG_PASSWORD
                        }
                    },
                    {
                        _errorStatusLiveData.value = UiErrorStatus.LOADING_ERROR
                    }
                )
        )
    }

    enum class UiErrorStatus {
        WRONG_PASSWORD,
        LOADING_ERROR
    }
}