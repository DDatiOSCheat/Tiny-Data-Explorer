package com.tinysweet.dataexplorer

import android.app.Application
import com.topjohnwu.superuser.Shell

class App : Application() {
    companion object {
        init {
            // Set default shell builder BEFORE any shell is created.
            // FLAG_MOUNT_MASTER is critical for Android 11+ with Magisk
            // so the root shell can see /data/data across mount namespaces.
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(10)
            )
        }
    }
}
