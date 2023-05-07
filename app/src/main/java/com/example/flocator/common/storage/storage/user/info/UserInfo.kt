package com.example.flocator.common.storage.storage.user.info

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

@kotlinx.serialization.Serializable
data class UserInfo(
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("login")
    val login: String,
    @SerializedName("avatarUrl")
    val avatarUri: String?,
    @SerializedName("birthDate")
    val birthDate: Timestamp,
    @SerializedName("blockedUsers")
    val blockedUsers: List<Boolean>
) {

    companion object {
        val DEFAULT = UserInfo(
            0,
            "",
            "",
            "",
            null
        )
    }

}

object TimestampSerializer: KSerializer<> {

}