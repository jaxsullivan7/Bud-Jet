package com.budjet.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class BudRepository(context: Context) {

    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, "budjet.json")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
    private val writeMutex = Mutex()

    private val _data = MutableStateFlow(load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private fun load(): AppData {
        if (!file.exists()) {
            val demo = DemoData.build()
            runCatching { file.writeText(json.encodeToString(AppData.serializer(), demo)) }
            return demo
        }
        return runCatching {
            json.decodeFromString(AppData.serializer(), file.readText())
        }.getOrElse { DemoData.build() }
    }

    suspend fun update(transform: (AppData) -> AppData) {
        writeMutex.withLock {
            val next = transform(_data.value)
            _data.value = next
            withContext(Dispatchers.IO) { persist(next) }
        }
    }

    suspend fun replaceAll(data: AppData) {
        writeMutex.withLock {
            _data.value = data
            withContext(Dispatchers.IO) { persist(data) }
        }
    }

    private fun persist(data: AppData) {
        runCatching {
            val temp = File(appContext.filesDir, "budjet.json.tmp")
            temp.writeText(json.encodeToString(AppData.serializer(), data))
            if (!temp.renameTo(file)) {
                file.writeText(temp.readText())
                temp.delete()
            }
        }
    }
}
