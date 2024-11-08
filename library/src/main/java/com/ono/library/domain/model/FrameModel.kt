package com.ono.library.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FrameModel(
    val id: Int = 0,
    val frameData: ByteArray,
    val status: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrameModel

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
