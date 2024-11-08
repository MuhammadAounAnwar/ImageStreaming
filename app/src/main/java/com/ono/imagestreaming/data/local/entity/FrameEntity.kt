package com.ono.imagestreaming.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ono.imagestreaming.domain.model.FrameModel

@Entity(tableName = "frames")
data class FrameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val frameData: ByteArray,
    val status: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameEntity

        if (id != other.id) return false
        if (!frameData.contentEquals(other.frameData)) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + frameData.contentHashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}

fun FrameEntity.toDomainModel() = FrameModel(id, frameData, status)

fun FrameModel.toEntity() = FrameEntity(id, frameData, status)

fun List<FrameEntity>.toDomainModel() = map { it.toDomainModel() }

fun List<FrameModel>.toEntity() = map { it.toEntity() }