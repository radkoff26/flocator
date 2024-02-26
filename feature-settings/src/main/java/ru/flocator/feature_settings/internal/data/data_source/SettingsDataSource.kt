package ru.flocator.feature_settings.internal.data.data_source

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*
import ru.flocator.data.api.ApiPaths
import ru.flocator.data.data_store.info.UserInfo
import ru.flocator.data.database.entities.User
import ru.flocator.feature_settings.internal.data.model.privacy.PrivacyData
import ru.flocator.feature_settings.internal.data.model.privacy.PrivacyType
import java.sql.Timestamp

internal interface SettingsDataSource {

    @POST(ApiPaths.USER_BIRTH_DATE)
    fun setBirthDate(
        @Query("birthDate") birthDate: Timestamp
    ): Single<Boolean>

    @POST(ApiPaths.USER_NAME)
    fun changeName(
        @Query("firstName") firstName: String,
        @Query("lastName") lastName: String
    ): Single<Boolean>

    @POST(ApiPaths.USER_AVATAR)
    fun changeAvatar(
        photoUri: String
    ): Single<Boolean>

    @POST(ApiPaths.PHOTO_POST)
    @Multipart
    fun postPhoto(
        @Part("photos") photos: List<MultipartBody.Part>
    ): Single<List<String?>>

    @POST(ApiPaths.USER_CHANGE_PASSWORD)
    fun changePassword(
        @Query("newPassword") newPassword: String
    ): Single<Boolean>

    @GET(ApiPaths.USER_BLOCKED_BY)
    fun getBlocked(): Single<List<UserInfo>>

    @POST(ApiPaths.USER_BLOCK)
    fun blockUser(
        @Query("blockedId") blockedId: Long
    ): Completable

    @POST(ApiPaths.USER_UNBLOCK)
    fun unblockUser(
        @Query("blockedId") blockedId: Long
    ): Completable

    @GET(ApiPaths.FRIENDSHIP_PRIVACY)
    fun getPrivacyData(): Single<List<PrivacyData>>

    @POST(ApiPaths.FRIENDSHIP_CHANGE_PRIVACY)
    fun changePrivacyData(
        @Query("friendId") friendId: Long,
        @Query("status") privacyType: PrivacyType
    ): Completable

    @DELETE(ApiPaths.USER_DELETE)
    fun deleteAccount(
        @Query("password") password: String
    ): Single<Boolean>

    @GET(ApiPaths.USER_GET_FRIENDS_FOR_MAP)
    fun getUserFriendsLocated(
        @Query("userId") userId: Long? = null
    ): Single<List<User>>
}