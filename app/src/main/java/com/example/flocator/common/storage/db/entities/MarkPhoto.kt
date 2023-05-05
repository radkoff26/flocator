package com.example.flocator.common.storage.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "mark_photo",
    foreignKeys = [
        ForeignKey(
            entity = Mark::class,
            parentColumns = ["markId"],
            childColumns = ["markId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MarkPhoto(
    @PrimaryKey
    val uri: String,
    val markId: Long
)
