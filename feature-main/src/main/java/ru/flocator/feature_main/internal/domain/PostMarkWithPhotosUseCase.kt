package ru.flocator.feature_main.internal.domain

import android.net.Uri
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.flocator.feature_main.internal.data.model.mark.AddMarkDto
import ru.flocator.feature_main.internal.data.model.mark.UploadingMarkWithPhotosDto
import ru.flocator.feature_main.internal.data.repository.MarkRepository
import ru.flocator.feature_main.internal.data.repository.PhotoRepository
import javax.inject.Inject

internal class PostMarkWithPhotosUseCase @Inject constructor(
    private val markRepository: MarkRepository,
    private val photoRepository: PhotoRepository
) {

    operator fun invoke(photos: Set<Map.Entry<Uri, ByteArray>>, markDto: AddMarkDto): Completable {
        return photoRepository.postPhotos(photos)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable {
                markRepository.postMark(
                    UploadingMarkWithPhotosDto(
                        markDto.location,
                        markDto.text,
                        markDto.isPublic,
                        markDto.place,
                        it
                    )
                )
            }
    }
}