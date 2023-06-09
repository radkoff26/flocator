package ru.flocator.app.community.data_classes

import com.google.gson.annotations.SerializedName

data class Friends(

    @SerializedName("userId")
    var userId: Long?,

    @SerializedName("firstName")
    var firstName: String?,

    @SerializedName("lastName")
    var lastName: String?,

    @SerializedName("avatarUri")
    var avatarUri: String?
)
