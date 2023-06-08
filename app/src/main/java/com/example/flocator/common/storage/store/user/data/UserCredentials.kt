package com.example.flocator.common.storage.store.user.data

@kotlinx.serialization.Serializable
data class UserCredentials(val userId: Long, val login: String, val password: String) {
    companion object {
        val DEFAULT = UserCredentials(-1, "", "")
    }
}
