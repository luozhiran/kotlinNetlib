package com.yk.net_base

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.widget.Toast

object CustomToast {
    private var mContext: Context? = null
    private var mToast: Toast? = null
    private var time: Long = 0

    @SuppressLint("ShowToast")
    fun register(context: Context?) {
        mContext = context
        mToast = Toast.makeText(mContext, "init", Toast.LENGTH_SHORT)
    }

    fun showToast(msg: String?) {
        if (System.currentTimeMillis() - time > 800 && !TextUtils.isEmpty(msg)) {
            mToast?.setText(msg)
            mToast?.show()
            time = System.currentTimeMillis()
        } else {
            mToast?.cancel()
        }
    }
}