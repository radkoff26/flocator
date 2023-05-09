package com.example.flocator.common.storage.store.user.info

import com.google.gson.annotations.SerializedName

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
    val avatarUri: String?
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
