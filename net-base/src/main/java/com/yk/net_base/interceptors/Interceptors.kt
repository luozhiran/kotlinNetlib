package com.yk.net_base.interceptors

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.HttpHeaders
import okhttp3.internal.platform.Platform
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


/**
 * 在控制台中打印日志
 */
open class HttpLoggingInterceptor : Interceptor {

    val UTF_8 = Charsets.UTF_8
    var level: Level = Level.NONE

    val DEFAULT_LOGGER = object : Logger {
        override fun log(msg: String) {
            Platform.get().log(Platform.INFO, msg, null)
        }
    }

    enum class Level { NONE, BASIC, HEADERS, BODY }


    fun setPrintAction(level: Level): HttpLoggingInterceptor {
        this.level = level
        return this
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val currentLevel = level
        val request = chain.request()
        if (currentLevel == Level.NONE) return chain.proceed(request)
        var printBody = currentLevel == Level.BODY //如果设置打印实体，肯定打印头信息
        var printHeader = printBody || currentLevel == Level.HEADERS
        val requestBody = request.body()
        val hasRequestBody = requestBody != null
        val connection = chain.connection()
        val stringBuild = StringBuilder()
        stringBuild.append("---->${request.method()} ${request.url()}  ${connection?.protocol()} ")
        if (!printHeader && hasRequestBody) {
            stringBuild.append("(${requestBody?.contentLength()}-byte body")
        }
        DEFAULT_LOGGER.log(stringBuild.toString())
        if (printHeader) {
            if (hasRequestBody) {
                if (requestBody!!.contentType() != null) {
                    DEFAULT_LOGGER.log("Content-Type: ${requestBody.contentType()}")
                }
                if (requestBody!!.contentLength() != -1L) {
                    DEFAULT_LOGGER.log("Content-Length: ${requestBody.contentLength()}")
                }
            }
            val headers = request.headers()
            for (i in headers.names().indices) {
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(headers.name(i), ignoreCase = true) && !"Content-Length".equals(headers.value(i), ignoreCase = true)) {
                    DEFAULT_LOGGER.log("${headers.name(i)}: ${headers.value(i)}")
                }
            }
            if (!printBody || !hasRequestBody) {
                DEFAULT_LOGGER.log("-->END ${request.method()}")
            } else if (bodyHasUnknownEncoding(request.headers())) {
                DEFAULT_LOGGER.log("-->END ${request.method()} (encoded body omitted)")
            } else {
                val buffer = Buffer()
                requestBody!!.writeTo(buffer)
                var charset: Charset = UTF_8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(charset)!!
                }
                DEFAULT_LOGGER.log(" ")
                if (isPlaintext(buffer)) {
                    DEFAULT_LOGGER.log(buffer.readString(charset))
                    DEFAULT_LOGGER.log("-->END ${request.method()} （${requestBody.contentLength()}-byte body)")
                } else {
                    DEFAULT_LOGGER.log("-->END ${request.method()} （binary ${requestBody.contentLength()}-byte body omitted)")
                }
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            DEFAULT_LOGGER.log("<-- HTTP FAILED ${e.printStackTrace()}")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body()
        val contentLength = responseBody!!.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        val responseStringBuilder = StringBuilder()
        responseStringBuilder
            .append("<--")
            .append(response.code())
            .append(if (response.message().isEmpty()) "" else ' '.toString() + response.message())
            .append(' ')
            .append(response.request().url())
            .append("(").append(tookMs).append("ms")
            .append(if (!printHeader) ", $bodySize body" else "").append(")")
        DEFAULT_LOGGER.log(responseStringBuilder.toString())

        if (printHeader) {
            val headers = response.headers()
            for (i in headers.names().indices) {
                DEFAULT_LOGGER.log("${headers.name(i)}: ${headers.value(i)}")
            }
            if (!printBody || !HttpHeaders.hasBody(response)) {
                DEFAULT_LOGGER.log("<-- END HTTP")
            } else if (bodyHasUnknownEncoding(response.headers())) {
                DEFAULT_LOGGER.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)// Buffer the entire body.
                var buffer = source.buffer()
                var gzippedLength: Long? = null
                if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                    gzippedLength = buffer.size()
                    var gzippedResponseBody: GzipSource? = null
                    try {
                        gzippedResponseBody = GzipSource(buffer.clone())
                        buffer = Buffer()
                        buffer.writeAll(gzippedResponseBody)
                    } finally {
                        gzippedResponseBody?.close()
                    }
                }
                var charset = UTF_8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF_8)!!
                }
                if (!isPlaintext(buffer)) {
                    DEFAULT_LOGGER.log("")
                    DEFAULT_LOGGER.log("<-- END HTTP (binary ${buffer.size()}-byte body omitted)")
                    return response
                }

                if (contentLength != 0L) {
                    DEFAULT_LOGGER.log("")
                    DEFAULT_LOGGER.log(buffer.clone().readString(charset))
                }

                if (gzippedLength != null) {
                    DEFAULT_LOGGER.log(
                        "<-- END HTTP (${buffer.size()}-byte, ${gzippedLength}-gzipped-byte body)"
                    )
                } else {
                    DEFAULT_LOGGER.log("<-- END HTTP (" + buffer.size() + "-byte body)")
                }
            }
        }
        return response
    }

    open fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return (contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
                && !contentEncoding.equals("gzip", ignoreCase = true))
    }
}

fun isPlaintext(buffer: Buffer): Boolean {
    return try {
        val prefix = Buffer()
        val byteCount = if (buffer.size() < 64) buffer.size() else 64
        buffer.copyTo(prefix, 0, byteCount)
        for (i in 0..15) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(
                    codePoint
                )
            ) {
                return false
            }
        }
        true
    } catch (e: EOFException) {
        false // Truncated UTF-8 sequence.
    }
}

interface Logger {
    fun log(msg: String)
}