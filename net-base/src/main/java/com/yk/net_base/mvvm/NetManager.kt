package com.yk.net_base.mvvm

import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.yk.net_base.RxScheduler
import com.yk.net_base.mvvm.datas.Repo
import com.yk.net_base.mvvm.interfaces.OnEmpty
import com.yk.net_base.mvvm.interfaces.OnResult
import io.reactivex.Flowable

class NetManager {

    companion object {
        @JvmField
        val ERROR_ = -1111

        @JvmField
        val EMPTY_ = 1111

        @JvmStatic
        fun isUiThread(): Boolean {
            return Thread.currentThread() === Looper.getMainLooper().thread
        }

        @JvmStatic
        fun <T> progressFlowableBody(
            flowable: Flowable<T>,
            lifecycleOwner: LifecycleOwner,
            onResult: OnResult<T>,
            onEmpty: OnEmpty?=null,
            netLoading: NetLoading? = null
        ) {
            netLoading?.showLoading()
            flowable.compose(RxScheduler.Flo_io_main())
                .`as`(
                    AutoDispose.autoDisposable(
                        AndroidLifecycleScopeProvider.from(
                            lifecycleOwner,
                            Lifecycle.Event.ON_DESTROY
                        )
                    )
                )
                .subscribe({ stringRepo ->
                    stringRepo?.let {
                        onResult.result(stringRepo)
                    }?:onEmpty?.empty(EMPTY_,"服务器没有数据返回")

                    netLoading?.hideLoading()
                }, { throwable -> //网络异常提示
                    NetExpection.NetExceptionTrip(throwable)
                    onEmpty?.empty(ERROR_,throwable.message)
                    netLoading?.hideLoading()
                })
        }

        /**
         * 处理Repo类请求
         * @param flowable
         * @param onResult
         * @param onEmpty
         * @param <T>
        </T> */
        @JvmStatic
        fun <T> progressFlowableRepo(
            flowable: Flowable<Repo<T>?>,
            lifecycleOwner: LifecycleOwner,
            onResult: OnResult<T>,
            onEmpty: OnEmpty?=null,
            netLoading: NetLoading? = null
        ) {
            netLoading?.showLoading()
            flowable.compose(RxScheduler.Flo_io_main())
                .`as`(
                    AutoDispose.autoDisposable(
                        AndroidLifecycleScopeProvider.from(
                            lifecycleOwner,
                            Lifecycle.Event.ON_DESTROY
                        )
                    )
                )
                .subscribe({ repo ->
                    repo?.let {
                        if (repo.code == 0){
                            repo.data?.let {
                                onResult.result(it)
                            } ?:onEmpty?.empty(repo.code,repo.msg)

                        }else{
                            onEmpty?.empty(repo.code,repo.msg)
                        }
                    } ?: onEmpty?.empty(EMPTY_,"服务器没有数据返回")
                    netLoading?.hideLoading()
                }, { throwable -> //网络异常提示
                    NetExpection.NetExceptionTrip(throwable)
                    onEmpty?.empty(ERROR_,throwable.message)
                    netLoading?.hideLoading()
                })
        }


        /**
         * 处理普通类请求
         * @param flowable
         * @param onResult
         * @param onEmpty
         * @param <T>
        </T> */
        @JvmStatic
        fun <T> progressFlowableCommon(
            flowable: Flowable<T>,
            lifecycleOwner: LifecycleOwner,
            onResult: OnResult<T>,
            onEmpty: OnEmpty?=null,
            netLoading: NetLoading? = null
        ) {
            netLoading?.showLoading()
            flowable.compose(RxScheduler.Flo_io_main())
                .`as`(
                    AutoDispose.autoDisposable(
                        AndroidLifecycleScopeProvider.from(
                            lifecycleOwner,
                            Lifecycle.Event.ON_DESTROY
                        )
                    )
                )
                .subscribe({ result ->
                    result?.let {
                        onResult.result(result)
                    }?:onEmpty?.empty(EMPTY_,"服务器没有数据返回")
                    netLoading?.hideLoading()
                }, { throwable -> //网络异常提示
                    NetExpection.NetExceptionTrip(throwable)
                    onEmpty?.empty(ERROR_,throwable.message)
                    netLoading?.hideLoading()
                })
        }

    }
}