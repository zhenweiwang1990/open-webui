package ai.gbox.chatdroid

import android.app.Application
import ai.gbox.chatdroid.datastore.AuthPreferences
import ai.gbox.chatdroid.datastore.AppPreferences

class ChatDroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthPreferences.init(this)
        AppPreferences.init(this)
    }
} 