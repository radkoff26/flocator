package ru.flocator.feature_main.internal.data.repository

import android.util.Log
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import ru.flocator.data.database.ApplicationDatabase
import ru.flocator.data.database.dao.MarkDao
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.feature_main.internal.data.data_source.MainDataSource
import ru.flocator.feature_main.internal.data.model.mark.MarkDto
import ru.flocator.feature_main.internal.data.model.mark.UploadingMarkWithPhotosDto
import javax.inject.Inject

internal class MarkRepository @Inject constructor(
    private val mainDataSource: MainDataSource,
    private val database: ApplicationDatabase,
    private val markDao: MarkDao
) {

    fun getMarksFromCache(): Single<List<MarkWithPhotos>> =
        markDao.getAllMarks().subscribeOn(Schedulers.io())

    fun getMarksForUser(): Single<List<MarkWithPhotos>> =
        mainDataSource.getUserAndFriendsMarks()
            .subscribeOn(Schedulers.io())
            .doAfterSuccess {
                saveNewMarksToCache(it)
            }.map {
                it.map(MarkDto::toMarkWithPhotos)
            }

    private fun saveNewMarksToCache(newMarks: List<MarkDto>) {
        val compositeDisposable = CompositeDisposable()
        val marks = newMarks.map(MarkDto::toMarkWithPhotos)
        val photos = marks.map(MarkWithPhotos::photos).flatten()
        database.updateMarks(
            marks.map(MarkWithPhotos::mark),
            photos
        )
            .subscribeOn(Schedulers.io())
            .doOnError { throwable ->
                Log.e(
                    TAG,
                    "getAllFriendsOfUser: error while saving marks to cache!",
                    throwable
                )
            }
            .doFinally {
                compositeDisposable.dispose()
            }
            .subscribe()
    }

    fun postMark(markDto: UploadingMarkWithPhotosDto): Completable {
        val addMarkDtoToPost: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            Gson().toJson(markDto)
        )
        return mainDataSource.postMark(addMarkDtoToPost).subscribeOn(Schedulers.io())
    }

    fun getMark(markId: Long): Single<MarkDto> =
        mainDataSource.getMark(markId).subscribeOn(Schedulers.io())

    fun likeMark(markId: Long): Completable {
        return mainDataSource.likeMark(markId).subscribeOn(Schedulers.io())
    }

    fun unlikeMark(markId: Long): Completable {
        return mainDataSource.unlikeMark(markId).subscribeOn(Schedulers.io())
    }

    companion object {
        private const val TAG = "MarkRepository_TAG"
    }
}