package com.example.flocator.common.storage.storage.user

@kotlinx.serialization.Serializable
data class UserData(val userId: Long, val login: String, val password: String) {
    companion object {
        val DEFAULT = UserData(-1, "", "")
    }
}
