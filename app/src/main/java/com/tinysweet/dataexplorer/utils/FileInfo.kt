package com.tinysweet.dataexplorer.utils

/**
 * Thông tin file/folder
 */
data class FileInfo(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val permissions: String
)