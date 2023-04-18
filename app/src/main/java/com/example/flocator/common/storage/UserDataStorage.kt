package com.example.flocator.common.storage

import com.example.flocator.common.models.UserData

interface UserDataStorage {

    fun getUserId(): Long?

    fun getLogin(): String?

    fun getPassword(): String?

    fun hasUserData(): Boolean

    fun saveUserData(userData: UserData)
}
