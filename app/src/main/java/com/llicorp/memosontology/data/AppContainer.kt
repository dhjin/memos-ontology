package com.llicorp.memosontology.data

import android.content.Context

class AppContainer(context: Context) {
    val serverConfig = ServerConfig(context)
    private val db = AppDatabase.getInstance(context)

    val memoRepository = MemoRemoteRepository(serverConfig, db)
    val nlqRepository = NlqRepository(serverConfig)
    val fusekiRepository = FusekiRepository(serverConfig)
}
