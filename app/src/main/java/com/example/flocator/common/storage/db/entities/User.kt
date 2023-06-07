package com.example.flocator.common.storage.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user")
open class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("location")
    var location: LatLng,
    @SerializedName("avatarUrl")
    val avatarUri: String?
)