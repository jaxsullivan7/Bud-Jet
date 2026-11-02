package com.budjet.app

import android.app.Application
import com.budjet.app.data.AutoBackup
import com.budjet.app.data.BudRepository
import com.budjet.app.notifications.BudJetNotifications

class BudJetApplication : Application() {
    lateinit var repository: BudRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = BudRepository(this)
        runCatching { BudJetNotifications.schedule(this) }
        runCatching { AutoBackup.maybeCreate(this, repository.data.value) }
    }
}
