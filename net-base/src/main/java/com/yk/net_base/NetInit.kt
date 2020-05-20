package com.yk.net_base

import android.app.Application

class NetInit {
    companion object {

        @JvmField
        var app: Application? = null

        @JvmStatic
        fun init(app: Application) {
            this.app = app
            CustomToast.register(app)
        }
    }
}