package ru.flocator.feature_settings.internal.usecases

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import ru.flocator.feature_settings.internal.repository.PhotoRepository
import ru.flocator.feature_settings.internal.repository.SettingsRepository
import javax.inject.Inject

internal class ChangeAvatarPhotoUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(photo: MultipartBody.Part): Single<Boolean> =
        photoRepository.postPhoto(photo).subscribeOn(Schedulers.io())
            .flatMap {
                settingsRepository.changeCurrentUserAvatar(it)
            }.subscribeOn(Schedulers.io())
}