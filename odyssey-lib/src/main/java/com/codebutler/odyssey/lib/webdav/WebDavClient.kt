/*
 * WebDavClient.kt
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

package com.codebutler.odyssey.lib.webdav

import android.net.Uri
import com.codebutler.odyssey.common.xml.XmlPullParserUtil.readText
import com.codebutler.odyssey.common.xml.XmlPullParserUtil.skip
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PUT
import retrofit2.http.Streaming
import retrofit2.http.Url
import kotlin.coroutines.experimental.buildIterator

class WebDavClient(
        okHttpClient: OkHttpClient,
        private val xmlPullParserFactory: XmlPullParserFactory) {

    companion object {
        private const val NS = "DAV:"
    }

    private val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com")
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()

    private val api = retrofit.create(WebDavApi::class.java)

    fun propfind(url: String): Iterator<DavResponse> {
        val response = api.propfind(url).execute()

        val parser = xmlPullParserFactory.newPullParser()
        parser.setInput(response.body()!!.byteStream(), "UTF-8")
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.nextTag()

        return readMultiStatus(parser)
    }

    fun downloadFile(uri: Uri): Single<ByteArray>
            = api.downloadFile(uri.toString())
                .map { it.bytes() }

    fun uploadFile(uri: Uri, data: ByteArray): Completable {
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), data)
        return api.uploadFile(uri.toString(), requestBody)
    }

    private fun readResponse(parser: XmlPullParser): DavResponse {
        parser.require(XmlPullParser.START_TAG, NS, "response")

        var href: String? = null
        var propstat: DavPropStat? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            when (name) {
                "href" -> href = parser.nextText()
                "propstat" -> propstat = readPropstat(parser)
                else -> skip(parser)
            }
        }

        return DavResponse(href, propstat)
    }

    private fun readMultiStatus(parser: XmlPullParser): Iterator<DavResponse> {
        return buildIterator {
            parser.require(XmlPullParser.START_TAG, NS, "multistatus")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "response" -> yield(readResponse(parser))
                    else -> skip(parser)
                }
            }
        }
    }

    private fun readPropstat(parser: XmlPullParser): DavPropStat {
        parser.require(XmlPullParser.START_TAG, NS, "propstat")

        var prop: DavProp? = null
        var status: String? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "prop" -> prop = readProp(parser)
                "status" -> status = readText(parser)
                else -> skip(parser)
            }
        }

        return DavPropStat(prop, status)
    }

    private fun readProp(parser: XmlPullParser): DavProp {
        parser.require(XmlPullParser.START_TAG, NS, "prop")

        var creationDate: String? = null
        var displayName: String? = null
        var contentLength: Long = 0
        var resourceType: DavResourceType = DavResourceType.NONE

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "creationdate" -> creationDate = readText(parser)
                "displayname" -> displayName = readText(parser)
                "getcontentlength" -> contentLength = readText(parser).toLongOrNull() ?: 0
                "resourcetype" -> resourceType = readResourceType(parser)
                else -> skip(parser)
            }
        }

        return DavProp(creationDate, displayName, contentLength, resourceType)
    }

    private fun readResourceType(parser: XmlPullParser): DavResourceType {
        parser.require(XmlPullParser.START_TAG, NS, "resourcetype")

        var resourceType = DavResourceType.NONE

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "collection" -> resourceType = DavResourceType.COLLECTION
            }
            skip(parser)
        }
        return resourceType
    }

    enum class DavResourceType {
        COLLECTION,
        NONE
    }

    data class DavResponse(
            val href: String?,
            val propStat: DavPropStat?)

    data class DavPropStat(
            val prop: DavProp?,
            val status: String?)

    data class DavProp(
            val creationDate: String?,
            val displayName: String?,
            val contentLength: Long,
            val resourceType: DavResourceType)

    private interface WebDavApi {

        @HTTP(method = "PROPFIND")
        fun propfind(@Url url: String): Call<ResponseBody>

        @GET
        @Streaming
        fun downloadFile(@Url url: String): Single<ResponseBody>

        @PUT
        fun uploadFile(@Url url: String, @Body file: RequestBody): Completable
    }
}
