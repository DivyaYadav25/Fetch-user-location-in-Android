package com.example.telyport

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.*

class AppApplication : Application() {
    val TAG = AppApplication::class.java.simpleName
    private var locale: Locale? = null

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }

    companion object {
        lateinit var mContext: Context

        fun string(resId: Int): String {
            return mContext!!.getString(resId)
        }

        fun context(): Context? {
            return mContext
        }
    }

}
