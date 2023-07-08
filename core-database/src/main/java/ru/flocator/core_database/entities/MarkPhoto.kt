package ru.flocator.core_database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "mark_photo")
data class MarkPhoto(
    @PrimaryKey
    val uri: String,
    val markId: Long
) : java.io.Serializable
