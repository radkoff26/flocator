package com.example.flocator.common.storage.storage.user.info

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.sql.Timestamp


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Timestamp::class)
object TimestampSerializer: KSerializer<Timestamp> {
    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeString(value.toString())
    }
    override fun deserialize(decoder: Decoder): Timestamp {
        return Timestamp.valueOf(decoder.decodeString())
    }
}

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
    @kotlinx.serialization.Serializable(with = TimestampSerializer::class)
    @SerializedName("birthDate")
    val birthDate: Timestamp,
    @SerializedName("blockedUsers")
    val blockedUsers: List<Long>
) {

    companion object {
        val DEFAULT = UserInfo(
            0,
            "",
            "",
            "",
            null,
            Timestamp(0),
            List(0) { _ -> 0L }
        )
    }

}