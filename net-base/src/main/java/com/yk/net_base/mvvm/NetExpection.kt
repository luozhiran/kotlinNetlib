package com.yk.net_base.mvvm

import com.google.gson.JsonParseException
import com.yk.net_base.CustomToast
import org.json.JSONException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.text.ParseException

object NetExpection {
    private const val CONNECT_ERROR = "网络连接失败,请检查网络"
    private const val CONNECT_TIMEOUT = "连接超时,请稍后再试"
    private const val BAD_NETWORK = "服务器异常"
    private const val PARSE_ERROR = "解析服务器响应数据失败"
    private const val UNKNOWN_ERROR = "未知错误"
    private const val RESPONSE_RETURN_ERROR = "服务器返回数据失败"
    private fun onException(reason: ExceptionReason) {
        when (reason) {
            ExceptionReason.CONNECT_ERROR -> CustomToast.showToast(
                CONNECT_ERROR
            )
            ExceptionReason.CONNECT_TIMEOUT -> CustomToast.showToast(
                CONNECT_TIMEOUT
            )
            ExceptionReason.BAD_NETWORK -> CustomToast.showToast(
                BAD_NETWORK
            )
            ExceptionReason.PARSE_ERROR -> CustomToast.showToast(
                PARSE_ERROR
            )
            ExceptionReason.UNKNOWN_ERROR -> CustomToast.showToast(
                UNKNOWN_ERROR
            )
            else -> CustomToast.showToast(UNKNOWN_ERROR)
        }
    }

    /**
     * 网络异常提示
     * @param throwable
     */
    fun NetExceptionTrip(throwable: Throwable?) {
        if (throwable is HttpException) {
            //HTTP错误
            onException(ExceptionReason.BAD_NETWORK)
        } else if (throwable is ConnectException || throwable is UnknownHostException) {
            //连接错误
            onException(ExceptionReason.CONNECT_ERROR)
        } else if (throwable is InterruptedIOException) {
            //连接超时
            onException(ExceptionReason.CONNECT_TIMEOUT)
        } else if (throwable is JsonParseException || throwable is JSONException || throwable is ParseException) {
            //解析错误
            onException(ExceptionReason.PARSE_ERROR)
        } else {
            //其他错误
            onException(ExceptionReason.UNKNOWN_ERROR)
        }
    }

    enum class ExceptionReason {
        /**
         * 解析数据失败
         */
        PARSE_ERROR,

        /**
         * 网络问题
         */
        BAD_NETWORK,

        /**
         * 连接错误
         */
        CONNECT_ERROR,

        /**
         * 连接超时
         */
        CONNECT_TIMEOUT,

        /**
         * 未知错误
         */
        UNKNOWN_ERROR
    }
}