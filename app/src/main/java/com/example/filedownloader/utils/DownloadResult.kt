package com.example.filedownloader.utils

sealed class DownloadResult {
    data class Success (val path: String): DownloadResult()

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val progress: Int): DownloadResult()
}
