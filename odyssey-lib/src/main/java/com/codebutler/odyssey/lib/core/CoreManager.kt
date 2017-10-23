/*
 * CoreManager.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.lib.core

import android.net.Uri
import android.os.Build
import io.reactivex.Single
import okio.Okio
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.util.zip.ZipInputStream

class CoreManager(retrofit: Retrofit, private val coresDir: File) {

    private val baseUri = Uri.parse("https://buildbot.libretro.com/")
    private val coresUri = baseUri.buildUpon()
            .appendEncodedPath("nightly/android/latest/")
            .appendPath(Build.SUPPORTED_ABIS.first())
            .build()

    private val api = retrofit.create(CoreManagerApi::class.java)

    init {
        coresDir.mkdirs()
    }

    fun downloadCore(zipFileName: String): Single<File> {
        val libFileName = zipFileName.substringBeforeLast(".zip")
        val destFile = File(coresDir, "lib$libFileName")

        if (destFile.exists()) {
            return Single.just(destFile)
        }

        val uri = coresUri.buildUpon()
                .appendPath(zipFileName)
                .build()

        return api.downloadZip(uri.toString())
                .map { response ->
                    if (response.isSuccessful) {
                        val zipStream = response.body()!!
                        while (true) {
                            val entry = zipStream.nextEntry ?: break
                            if (entry.name == libFileName) {
                                Okio.source(zipStream).use { zipSource ->
                                    Okio.sink(destFile).use { fileSink ->
                                        Okio.buffer(zipSource).readAll(fileSink)
                                        return@map destFile
                                    }
                                }
                            }
                        }
                        throw Exception("Library not found in zip")
                    } else {
                        throw Exception(response.errorBody()!!.string())
                    }
                }
    }

    private interface CoreManagerApi {

        @GET
        @Streaming
        fun downloadZip(@Url url: String): Single<Response<ZipInputStream>>
    }
}
