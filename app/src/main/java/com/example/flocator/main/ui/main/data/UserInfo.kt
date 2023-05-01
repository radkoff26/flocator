package com.example.flocator.main.ui.main.data

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

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
    val birthDate: Timestamp?,
    @SerializedName("blockedUsers")
    val blockedUsers: Array<Long>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserInfo

        if (userId != other.userId) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (login != other.login) return false
        if (avatarUri != other.avatarUri) return false
        if (birthDate != other.birthDate) return false
        if (!blockedUsers.contentEquals(other.blockedUsers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + login.hashCode()
        result = 31 * result + (avatarUri?.hashCode() ?: 0)
        result = 31 * result + (birthDate?.hashCode() ?: 0)
        result = 31 * result + blockedUsers.contentHashCode()
        return result
    }
}
