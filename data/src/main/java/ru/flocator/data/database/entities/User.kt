package ru.flocator.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import ru.flocator.data.models.location.Coordinates

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val userId: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("location")
    var location: Coordinates,
    @SerializedName("avatarUrl")
    val avatarUri: String?
)
