package ru.flocator.core_dto.user

import com.google.gson.annotations.SerializedName

data class FriendRequests(

    @SerializedName("userId")
    var userId: Long?,

    @SerializedName("firstName")
    var firstName: String?,

    @SerializedName("lastName")
    var lastName: String?,

    @SerializedName("avatarUri")
    var avatarUri: String?
)
