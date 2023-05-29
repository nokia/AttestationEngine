package com.example.mobileattester.data.util.abs

import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.Exception

/**
 * Class to run async functions in, notifies subscribers if the provided
 * function was successfully completed.
 */
class AsyncRunner(
    private val notifier: Notifier,
) {
    private val job = Job()
    private val scope = CoroutineScope(job)

    /**
     * Run a function in the scope this class has created.
     * @param function Function to run. Should return true if the operation was successful. Notifies
     * if true is returned.
     */
    fun <T> run(data: T, function: suspend (data: T) -> Boolean, onException: (Exception) -> Unit) {
        scope.launch {
            try {
                val ok = function(data)
                if (ok) {
                    notifier.notifyAll(data)
                }
            } catch (e: Exception) {
                println("Error running function $e")
                onException(e)
            }
        }
    }
}

/**
 * Implement on classes which are to be registered for notifications.
 */
interface NotificationSubscriber {
    /** Gets called when an operation with param data was successfully completed */
    fun <T> notify(data: T)
}

class Notifier(
    private val subs: MutableList<WeakReference<NotificationSubscriber>> = mutableListOf(),
) {
    fun <T> notifyAll(data: T) {
        for (sub in subs) {
            sub.get()?.notify(data)
        }
    }

    fun addSubscriber(sub: NotificationSubscriber) {
        subs.add(WeakReference(sub))
    }

    fun removeSubscriber(sub: NotificationSubscriber) {
        val rm = subs.find {
            sub == it.get()
        }
        subs.remove(rm)
    }

    fun subCount() = subs.size
}