package com.budjet.app

import android.app.Application
import com.budjet.app.data.BudRepository

class BudJetApplication : Application() {
    lateinit var repository: BudRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = BudRepository(this)
    }
}
