package ru.flocator.feature_main.internal.data.data_source

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import ru.flocator.data.api.ApiPaths
import ru.flocator.data.database.entities.User
import ru.flocator.data.models.location.Coordinates
import ru.flocator.feature_main.internal.data.model.mark.MarkDto
import ru.flocator.feature_main.internal.data.model.user_name.UsernameDto

internal interface MainDataSource {

    @GET(ApiPaths.USER_GET_FRIENDS_FOR_MAP)
    fun getUserFriendsLocated(@Query("userId") userId: Long? = null): Single<List<User>>

    @GET(ApiPaths.MARK_FRIENDS)
    fun getUserAndFriendsMarks(): Single<List<MarkDto>>

    @Multipart
    @POST(ApiPaths.MARK_POST)
    fun postMark(
        @Part("mark") markDto: RequestBody
    ): Completable

    @Multipart
    @POST(ApiPaths.PHOTO_POST)
    fun postPhotos(
        @Part("photos") photos: List<MultipartBody.Part>
    ): Single<List<String>>

    @GET(ApiPaths.MARK_GET)
    fun getMark(
        @Path("markId") markId: Long
    ): Single<MarkDto>

    @POST(ApiPaths.USER_LOCATION)
    fun updateLocation(@Body coordinates: Coordinates): Single<Boolean>

    @POST(ApiPaths.MARK_LIKE)
    fun likeMark(@Query("markId") markId: Long): Completable

    @POST(ApiPaths.MARK_UNLIKE)
    fun unlikeMark(@Query("markId") markId: Long): Completable

    @GET(ApiPaths.USER_GET_USERNAME)
    fun getUsername(@Path("userId") userId: Long): Single<UsernameDto>

    @POST(ApiPaths.USER_ONLINE)
    fun goOnline(): Completable

    @POST(ApiPaths.USER_OFFLINE)
    fun goOffline(): Completable
}
