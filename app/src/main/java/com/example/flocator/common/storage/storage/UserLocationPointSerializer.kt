package com.example.flocator.common.storage.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.commons.lang3.SerializationException
import java.io.InputStream
import java.io.OutputStream

class UserLocationPointSerializer: Serializer<UserLocationPoint> {
    override val defaultValue: UserLocationPoint = UserLocationPoint(0.0, 0.0)

    override suspend fun readFrom(input: InputStream): UserLocationPoint {
        return try {
            Json.decodeFromString(UserLocationPoint.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Impossible to read data from file", e)
        }
    }

    override suspend fun writeTo(t: UserLocationPoint, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserLocationPoint.serializer(), t).encodeToByteArray()
            )
        }
    }
}
