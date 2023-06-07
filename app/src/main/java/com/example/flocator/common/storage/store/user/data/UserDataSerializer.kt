package com.example.flocator.common.storage.store.user.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserDataSerializer : Serializer<UserCredentials> {
    override val defaultValue: UserCredentials = UserCredentials.DEFAULT

    override suspend fun readFrom(input: InputStream): UserCredentials {
        return try {
            Json.decodeFromString(UserCredentials.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to parse this UserData object", e)
        }
    }

    override suspend fun writeTo(t: UserCredentials, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserCredentials.serializer(), t).encodeToByteArray()
            )
        }
    }
}