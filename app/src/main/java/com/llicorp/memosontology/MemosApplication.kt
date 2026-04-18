package com.llicorp.memosontology

import android.app.Application
import com.llicorp.memosontology.data.AppContainer

class MemosApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
