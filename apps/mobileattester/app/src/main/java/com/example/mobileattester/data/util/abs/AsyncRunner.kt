package com.example.mobileattester.data.util.abs

import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.Exception

/**
 * Class to run async functions in, notifies subscribers if the provided
 * function was successfully completed.
 * TODO errors
 */
class AsyncRunner(
    private val notifier: Notifier,
) {
    private val job = Job()
    private val scope = CoroutineScope(job)

    fun <T> run(data: T, update: suspend (data: T) -> Boolean) {
        scope.launch {
            try {
                val ok = update(data)
                if (ok) {
                    notifier.notifyAll(data)
                }
            } catch (e: Exception) {
                println("Error running function $e")
            }
        }
    }
}

/**
 * Implement on classes which are to be registered for notifications.
 */
interface NotifySubscriber {
    /** Gets called when an operation with param data was successfully completed */
    fun <T> notify(data: T)
}

class Notifier(
    private val subs: MutableList<WeakReference<NotifySubscriber>> = mutableListOf(),
) {
    fun <T> notifyAll(data: T) {
        for (sub in subs) {
            sub.get()?.notify(data)
        }
    }

    fun subscribe(sub: NotifySubscriber) {
        subs.add(WeakReference(sub))
    }

    fun unSubscribe(sub: NotifySubscriber) {
        val rm = subs.find {
            sub == it.get()
        }
        subs.remove(rm)
    }

    fun subCount() = subs.size
}