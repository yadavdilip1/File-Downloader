package com.example.filedownloader.data

import android.Manifest

object Constants {

    val storagePermission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    const val outputDir = "FileDownloader"

    const val fileURL = "https://file-examples-com.github.io/uploads/2017/10/file-example_PDF_1MB.pdf"



}
