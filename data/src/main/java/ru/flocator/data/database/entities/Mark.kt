package ru.flocator.data.database.entities

import androidx.room.*
import com.google.gson.annotations.SerializedName
import ru.flocator.data.models.location.Coordinates
import java.sql.Timestamp
import java.util.*

@Entity(tableName = "mark")
data class Mark(
    @PrimaryKey
    @SerializedName("markId")
    val markId: Long,
    @SerializedName("authorId")
    val authorId: Long,
    @SerializedName("point")
    val location: Coordinates,
    @SerializedName("text")
    val text: String,
    @SerializedName("isPublic")
    val isPublic: Boolean,
    @SerializedName("place")
    val place: String,
    @SerializedName("likesCount")
    var likesCount: Int,
    @SerializedName("hasUserLiked")
    var hasUserLiked: Boolean,
    @SerializedName("createdAt")
    val createdAt: Timestamp
) : java.io.Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Mark

        if (markId != other.markId) return false
        if (authorId != other.authorId) return false
        if (location != other.location) return false
        if (text != other.text) return false
        if (isPublic != other.isPublic) return false
        if (place != other.place) return false

        return true
    }

    override fun hashCode(): Int {
        var result = markId.hashCode()
        result = 31 * result + authorId.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + isPublic.hashCode()
        result = 31 * result + place.hashCode()
        return result
    }
}
