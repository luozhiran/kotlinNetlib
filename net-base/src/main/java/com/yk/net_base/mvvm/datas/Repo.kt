package com.yk.net_base.mvvm.datas

class Repo<T> {
    var code = -1000
    var msg: String? = null
    var fail: String? = null
    var success = false
    var data: T? = null
}