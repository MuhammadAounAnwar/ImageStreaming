package com.ono.imagestreaming.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val status: String
)
