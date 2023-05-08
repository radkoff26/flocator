package com.example.flocator.common.storage.store.user.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserDataSerializer : Serializer<UserData> {
    override val defaultValue: UserData = UserData.DEFAULT

    override suspend fun readFrom(input: InputStream): UserData {
        return try {
            Json.decodeFromString(UserData.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to parse this UserData object", e)
        }
    }

    override suspend fun writeTo(t: UserData, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserData.serializer(), t).encodeToByteArray()
            )
        }
    }
}