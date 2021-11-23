package com.example.mobileattester.data.util.abs

import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.Exception

/**
 * Updater to run functions in, notifies on success.
 */
class Updater(
    private val notifier: Notifier,
) {
    private val job = Job()
    private val scope = CoroutineScope(job)

    fun <T> runUpdate(data: T, update: (data: T) -> Boolean) {
        scope.launch {
            try {
                val ok = update(data)
                if (ok) {
                    notifier.notifyAll(data)
                }
            } catch (e: Exception) {
                println("¤¤¤ Error updating element $e")
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

// Quick tests
//
//class Sub : UpdateSubscriber {
//    var string = "empty"
//        private set
//
//    override fun <T> onUpdate(data: T) {
//        println("OnUpdate")
//        if (data is String) {
//            string = data
//        } else {
//            println("¤¤¤ Not interested")
//        }
//    }
//}
//
//
//class Main {
//    init {
//        val sub = Sub()
//        println("¤¤¤ Start: ${sub.string}")
//        val notifier = Notifier().apply {
//            this.subscribe(sub)
//        }
//
//        val updater = Updater(notifier)
//
//        val data = "New String"
//
//        fun upd(data: String): Boolean {
//            println("¤¤¤ Running function with value: $data")
//            return true
//        }
//
//        val s = CoroutineScope(Dispatchers.Main)
//
//
//        fun int(data: Int): Boolean {
//            println("¤¤¤ Running function with value: $data")
//            return true
//        }
//
//        updater.runUpdate(data, ::upd)
//        updater.runUpdate(123, ::int)
//
//        s.launch {
//            delay(1000)
//            println("¤¤¤ End: ${sub.string}")
//
//            println("¤¤¤ SubCount: ${notifier.subCount()}")
//            notifier.unSubscribe(sub)
//            println("¤¤¤ SubCount: ${notifier.subCount()}")
//        }
//    }
//}