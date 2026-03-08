package com.marketpos.domain.model

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String?,
    val forceUpdate: Boolean
)
