package com.ono.imagestreaming.domain.model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("status") var status: Int? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("key") var key: String? = null,
    @SerializedName("path") var path: String? = null,
    @SerializedName("nodeType") var nodeType: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("size") var size: Int? = null,
    @SerializedName("link") var link: String? = null,
    @SerializedName("private") var private: Boolean? = null,
    @SerializedName("expires") var expires: String? = null,
    @SerializedName("downloads") var downloads: Int? = null,
    @SerializedName("maxDownloads") var maxDownloads: Int? = null,
    @SerializedName("autoDelete") var autoDelete: Boolean? = null,
    @SerializedName("planId") var planId: Int? = null,
    @SerializedName("screeningStatus") var screeningStatus: String? = null,
    @SerializedName("mimeType") var mimeType: String? = null,
    @SerializedName("created") var created: String? = null,
    @SerializedName("modified") var modified: String? = null

)
