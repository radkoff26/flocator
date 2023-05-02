package com.example.flocator.common.storage.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("location")
    var location: Point,
    @SerializedName("avatarUrl")
    val avatarUrl: String?
)