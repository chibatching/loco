package com.sys1yagi.loco.sample

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.sys1yagi.loco.core.*
import com.sys1yagi.loco.core.internal.SmashedLog
import com.sys1yagi.loco.sample.log.ClickLog
import com.sys1yagi.loco.sample.log.ScreenLog
import com.sys1yagi.loco.store.android.sqlite.LocoAndroidSqliteStore
import kotlinx.coroutines.delay

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Loco.start(
            LocoConfig(
                // store = InMemoryStore(),
                store = LocoAndroidSqliteStore(this),
                smasher = GsonSmasher(Gson()),
                senders = listOf(
                    LogcatSender()
                ),
                scheduler = IntervalSendingScheduler(5000)
            ) {
                logToSender[LogcatSender::class] = listOf(
                    ClickLog::class,
                    ScreenLog::class
                )
            }
        )
    }

    class GsonSmasher(val gson: Gson) : Smasher {
        override fun smash(log: LocoLog): String {
            return gson.toJson(log)
        }
    }

    class LogcatSender : Sender {
        override suspend fun send(logs: List<SmashedLog>): SendingResult {
            logs.forEach {
                Log.d("LogcatSender", it.smashedLog)
            }
            return SendingResult.SUCCESS
        }
    }

    class IntervalSendingScheduler(private val interval: Long) : SendingScheduler {
        override suspend fun schedule(
            latestResults: List<Pair<Sender, SendingResult>>,
            config: LocoConfig,
            offer: () -> Unit
        ) {
            delay(interval)
            offer()
        }
    }

    class InMemoryStore : Store {
        val storage = mutableListOf<SmashedLog>()
        override suspend fun store(log: SmashedLog) {
            storage.add(log)
        }

        override suspend fun load(size: Int): List<SmashedLog> {
            return storage.take(size)
        }

        override suspend fun delete(logs: List<SmashedLog>) {
            storage.removeAll(logs)
        }
    }
}
