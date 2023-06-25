package ru.flocator.feature_settings.domain.friend

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@kotlinx.serialization.Serializable
data class Friend(
    var userId: Long,
    var avaURI: String?,
    var name: String,
    var isChecked: Boolean
)
object FriendListSerializer: KSerializer<List<Friend>> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("FriendLister", Friend.serializer().descriptor)

    override fun deserialize(decoder: Decoder): List<Friend> {
        val num = decoder.decodeInt()
        val ans = LinkedList<Friend>()
        for (i in 1..num){
           ans.add(decoder.decodeSerializableValue(Friend.serializer()))
        }
        return ans
    }

    override fun serialize(encoder: Encoder, value: List<Friend>) {
        encoder.encodeInt(value.size)
        for (friend in value) {
            encoder.encodeSerializableValue(Friend.serializer(),friend)
        }
    }

}