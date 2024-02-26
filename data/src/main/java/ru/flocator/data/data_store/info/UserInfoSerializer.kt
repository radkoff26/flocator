package ru.flocator.data.data_store.info

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class UserInfoSerializer : Serializer<UserInfo> {
    override val defaultValue: UserInfo = UserInfo.DEFAULT

    override suspend fun readFrom(input: InputStream): UserInfo {
        return try {
            Json.decodeFromString(UserInfo.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to parse this UserData object", e)
        }
    }

    override suspend fun writeTo(t: UserInfo, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserInfo.serializer(), t).encodeToByteArray()
            )
        }
    }
}