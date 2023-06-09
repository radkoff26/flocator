package ru.flocator.app.main.deserializers

import ru.flocator.app.main.data.AddressResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class AddressDeserializer: JsonDeserializer<AddressResponse> {
    companion object {
        private val EMPTY = AddressResponse("Empty")
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AddressResponse {
        if (json == null) {
            return EMPTY
        }
        val featureMemberJsonArray = json.asJsonObject
            .getAsJsonObject("response")
            .getAsJsonObject("GeoObjectCollection")
            .getAsJsonArray("featureMember")

        if (featureMemberJsonArray.size() == 0) {
            return EMPTY
        }

        val firstObjectAddress = featureMemberJsonArray[0].asJsonObject
            .getAsJsonObject("GeoObject")
            .getAsJsonObject("metaDataProperty")
            .getAsJsonObject("GeocoderMetaData")
            .getAsJsonObject("Address")
            .get("formatted")
            .asString

        return AddressResponse(firstObjectAddress)
    }
}