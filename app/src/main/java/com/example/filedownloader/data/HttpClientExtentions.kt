package com.example.filedownloader.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.filedownloader.utils.DownloadResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import kotlin.math.roundToInt


lateinit var file: OutputStream
private lateinit var outputPath: String


suspend fun HttpClient.downloadFile(
    url: String,
    mContext: Context
): Flow<DownloadResult> {
    return flow {
        try {

            val filename: String = url.substring(url.lastIndexOf("/") + 1)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                var destURL =
                    Environment.getExternalStorageDirectory().path + File.separator + Constants.outputDir
                var desFile = File(destURL)
                if (!desFile.exists()) {
                    desFile.mkdir()
                }
                destURL = destURL + File.separator + Environment.DIRECTORY_DOWNLOADS
                desFile = File(destURL)
                if (!desFile.exists()) {
                    desFile.mkdir()
                }

                outputPath = destURL

                destURL = destURL + File.separator + filename
                file = java.io.FileOutputStream(destURL)

            } else {

                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                var desDirectory = Environment.DIRECTORY_DOWNLOADS
                desDirectory = desDirectory + File.separator + Constants.outputDir
                val desFile = File(desDirectory)
                if (!desFile.exists()) {
                    desFile.mkdir()
                }

                outputPath = desDirectory + File.separator + filename


                values.put(MediaStore.MediaColumns.RELATIVE_PATH, desDirectory)
                val uri = mContext.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                if (uri != null) {
                    file = mContext.contentResolver.openOutputStream(uri)!!
                }

            }


            val response = call {
                url(url)
                method = HttpMethod.Get
            }.response

            val data = ByteArray(response.contentLength()!!.toInt())
            var offset = 0

            do {
                val currentRead = response.content.readAvailable(data, offset, data.size)
                offset += currentRead
                val progress = (offset * 100f / data.size).roundToInt()
                emit(DownloadResult.Progress(progress))
            } while (currentRead > 0)

            response.close()

            if (response.status.isSuccess()) {
                withContext(Dispatchers.IO) {
                    file.write(data)
                }
                emit(DownloadResult.Success(outputPath))
            } else {
                emit(DownloadResult.Error("File not downloaded"))
            }

        } catch (e: TimeoutCancellationException) {
            emit(DownloadResult.Error("Connection timed out", e))
        } catch (t: Throwable) {
            emit(DownloadResult.Error("Failed to connect"))
        }
    }
}
