package com.gestormei

import android.app.Application
import com.gestormei.data.AppContainer

class GestorMeiApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
