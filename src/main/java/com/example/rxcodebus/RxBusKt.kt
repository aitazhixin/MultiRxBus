package com.example.rxcodebus

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.*

class RxBusKt private constructor() {
    private val TAG = "RxBUS"
    private val rxBusUI: Subject<Any>
    private val rxBusData: Subject<Any>
    private val rxBusNotice: Subject<Any>
    private val rxBusEs: ExecutorService
    private val eventBuffer: ConcurrentLinkedDeque<Event>
    private val executor_number = 40

    private object RxBusBuilder {
        val RxBUS_INSTANCE = RxBusKt()
    }

    init {
        rxBusUI = PublishSubject.create<Any>().toSerialized()
        rxBusData = PublishSubject.create<Any>().toSerialized()
        rxBusNotice = PublishSubject.create<Any>().toSerialized()
        rxBusEs = Executors.newFixedThreadPool(executor_number)
        eventBuffer = ConcurrentLinkedDeque()
    }

    inner class Event(internal var event_id: Any, internal var message: Any)

    fun postUI(code: Any, obj: Any) {
        val event = Event(code, obj)
        synchronized(eventBuffer) {
            eventBuffer.add(event)
        }
        postEvent(event)
    }

    fun broadNotice(code: Any, obj: Any) {
        rxBusNotice.onNext(Event(code, obj))
    }

    fun post(code: Any, obj: Any) {
        rxBusData.onNext(Event(code, obj))
    }


    fun <T> toObservableWithUI(eventId: Any, eventType: Class<T>): Observable<T> {
        synchronized(eventBuffer) {
            val observable = rxBusUI.ofType<Event>(Event::class.java!!)
            Log.d(TAG, "buffer length " + eventBuffer.size)
            val event_top = eventBuffer.poll()
            Log.d(TAG, "buffer length " + eventBuffer.size)

            return if (event_top != null) {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .mergeWith(Observable.create(ObservableOnSubscribe<Event> { emitter -> emitter.onNext(event_top) })
                                .filter { event ->
                                    val current = Thread.currentThread()
                                    Log.d(TAG, "buffer filter: thread name " + current.name)
                                    event.event_id === eventId
                                }) as Observable<T>
            } else {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .filter { event ->
                            val current = Thread.currentThread()
                            Log.d(TAG, "normal filter: thread name " + current.name)
                            synchronized(eventBuffer) {
                                Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size)
                            }
                            synchronized(eventBuffer) {
                                eventBuffer.poll()
                            }
                            event.event_id === eventId
                        }
                        .map { event ->
                            Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                            event.message
                        }
                        .cast(eventType)
            }
        }
    }

    fun <T> toObservableWithUI(eventId: Any, eventType: Class<T>, consumer: Consumer<T>): Disposable {
        synchronized(eventBuffer) {
            val observable = rxBusUI.ofType<Event>(Event::class.java!!)
            Log.d(TAG, "buffer length " + eventBuffer.size)
            val event_top = eventBuffer.poll()
            Log.d(TAG, "buffer length " + eventBuffer.size)

            return if (event_top != null) {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .mergeWith(Observable.create(ObservableOnSubscribe<Event> { emitter -> emitter.onNext(event_top) })
                                .filter { event ->
                                    val current = Thread.currentThread()
                                    Log.d(TAG, "buffer filter: thread name " + current.name)
                                    event.event_id === eventId
                                })
                        .cast(eventType)
                        .subscribe(consumer) as Disposable
            } else {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .filter { event ->
                            val current = Thread.currentThread()
                            Log.d(TAG, "normal filter: thread name " + current.name)
                            synchronized(eventBuffer) {
                                Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size)
                            }
                            synchronized(eventBuffer) {
                                eventBuffer.poll()
                            }
                            event.event_id === eventId
                        }
                        .map { event ->
                            Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                            event.message
                        }
                        .cast(eventType)
                        .subscribe(consumer)
            }
        }
    }

    fun <T> toObservableWithUI(eventId: Any, eventType: Class<T>, consumerNext: Consumer<T>, consumerError: Consumer<Throwable>): Disposable {
        synchronized(eventBuffer) {
            val observable = rxBusUI.ofType<Event>(Event::class.java!!)
            Log.d(TAG, "buffer length " + eventBuffer.size)
            val event_top = eventBuffer.poll()
            Log.d(TAG, "buffer length " + eventBuffer.size)

            return if (event_top != null) {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .mergeWith(Observable.create(ObservableOnSubscribe<Event> { emitter -> emitter.onNext(event_top) })
                                .filter { event ->
                                    val current = Thread.currentThread()
                                    Log.d(TAG, "buffer filter: thread name " + current.name)
                                    event.event_id === eventId
                                })
                        .cast(eventType)
                        .subscribe(consumerNext, consumerError) as Disposable
            } else {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .filter { event ->
                            val current = Thread.currentThread()
                            Log.d(TAG, "normal filter: thread name " + current.name)
                            synchronized(eventBuffer) {
                                Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size)
                            }
                            synchronized(eventBuffer) {
                                eventBuffer.poll()
                            }
                            event.event_id === eventId
                        }
                        .map { event ->
                            Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                            event.message
                        }
                        .cast(eventType)
                        .subscribe(consumerNext, consumerError)
            }
        }
    }


    fun <T> toObservableWithUIWithinTimeout(eventId: Any, eventType: Class<T>, timeinterval: Long): Observable<T> {
        synchronized(eventBuffer) {
            val observable = rxBusUI.ofType<Event>(Event::class.java!!)
            Log.d(TAG, "buffer length " + eventBuffer.size)
            val event_top = eventBuffer.poll()
            Log.d(TAG, "buffer length " + eventBuffer.size)

            return if (event_top != null) {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, TimeUnit.MILLISECONDS)
                        .mergeWith(Observable.create(ObservableOnSubscribe<Event> { emitter -> emitter.onNext(event_top) })
                                .filter { event ->
                                    val current = Thread.currentThread()
                                    Log.d(TAG, "buffer filter: thread name " + current.name)
                                    event.event_id === eventId
                                }) as Observable<T>
            } else {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, TimeUnit.MILLISECONDS)
                        .filter { event ->
                            val current = Thread.currentThread()
                            Log.d(TAG, "normal filter: thread name " + current.name)
                            synchronized(eventBuffer) {
                                Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size)
                            }
                            synchronized(eventBuffer) {
                                eventBuffer.poll()
                            }
                            event.event_id === eventId
                        }
                        .map { event ->
                            Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                            event.message
                        }
                        .cast(eventType)
            }
        }
    }

    fun <T> toObservableWithUIWithinTimeout(eventId: Any, eventType: Class<T>, consumerNext: Consumer<T>, consumerError: Consumer<Throwable>, timeinterval: Long): Disposable {
        synchronized(eventBuffer) {
            val observable = rxBusUI.ofType<Event>(Event::class.java!!)
            Log.d(TAG, "buffer length " + eventBuffer.size)
            val event_top = eventBuffer.poll()
            Log.d(TAG, "buffer length " + eventBuffer.size)

            return if (event_top != null) {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, TimeUnit.MILLISECONDS)
                        .mergeWith(Observable.create(ObservableOnSubscribe<Event> { emitter -> emitter.onNext(event_top) })
                                .filter { event ->
                                    val current = Thread.currentThread()
                                    Log.d(TAG, "buffer filter: thread name " + current.name)
                                    event.event_id === eventId
                                })
                        .cast(eventType)
                        .subscribe(consumerNext, consumerError) as Disposable
            } else {
                observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, TimeUnit.MILLISECONDS)
                        .filter { event ->
                            val current = Thread.currentThread()
                            Log.d(TAG, "normal filter: thread name " + current.name)
                            synchronized(eventBuffer) {
                                Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size)
                            }
                            synchronized(eventBuffer) {
                                eventBuffer.poll()
                            }
                            event.event_id === eventId
                        }
                        .map { event ->
                            Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                            event.message
                        }
                        .cast(eventType)
                        .subscribe(consumerNext, consumerError)
            }
        }
    }

    fun <T> toObservableWithData(eventId: Any, eventType: Class<T>): Observable<T> {
        return rxBusData.ofType<Event>(Event::class.java!!)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
    }

    fun <T> toObservableWithData(eventId: Any, eventType: Class<T>, consumer: Consumer<T>): Disposable {
        return rxBusData.ofType<Event>(Event::class.java)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribe(consumer)
    }

    fun <T> toObservableWithData(eventId: Any, eventType: Class<T>, consumerNext: Consumer<T>, consumerError: Consumer<Throwable>): Disposable {
        return rxBusData.ofType<Event>(Event::class.java)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribe(consumerNext, consumerError)
    }

    fun <T> toObservableWithDataWithinTimeout(eventId: Any, eventType: Class<T>, timeinterval: Long): Observable<T> {
        return rxBusData.ofType<Event>(Event::class.java!!)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .timeout(timeinterval, TimeUnit.MILLISECONDS)
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
    }

    fun <T> toObservableWithDataWithinTimeout(eventId: Any, eventType: Class<T>, consumerNext: Consumer<T>, consumerError: Consumer<Throwable>, timeinterval: Long): Disposable {
        return rxBusData.ofType<Event>(Event::class.java)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .timeout(timeinterval, TimeUnit.MILLISECONDS)
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribe(consumerNext, consumerError)
    }

    fun <T> toObservableWithNotice(eventId: Any, eventType: Class<T>): Observable<T> {
        return rxBusNotice.ofType<Event>(Event::class.java!!)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
    }


    fun <T> toObservableWithNotice(eventId: Any, eventType: Class<T>, consumer: Consumer<T>): Disposable {
        return rxBusNotice.ofType<Event>(Event::class.java!!)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribe(consumer)
    }


    fun <T> toObservableWithNotice(eventId: Any, eventType: Class<T>, consumerNext: Consumer<T>, consumerError: Consumer<Throwable>): Disposable {
        return rxBusNotice.ofType<Event>(Event::class.java!!)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribe(consumerNext, consumerError)
    }

    fun <T> toObservableWithNoticeWithinTimeout(eventId: Any, eventType: Class<T>, timeinterval: Long): Observable<T> {
        return rxBusNotice.ofType<Event>(Event::class.java!!)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .timeout(timeinterval, TimeUnit.MILLISECONDS)
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
    }


    fun <T> toObservableWithNoticeWithinTimeout(eventId: Any, eventType: Class<T>, consumerNext: Consumer<T>, consumerError: Consumer<Throwable>, timeinterval: Long): Disposable {
        return rxBusNotice.ofType<Event>(Event::class.java!!)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .timeout(timeinterval, TimeUnit.MILLISECONDS)
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribe(consumerNext, consumerError)
    }

    internal inner class DisposeObserver : Disposable {
        private var disposable: Disposable? = null

        fun setDisposable(disp: Disposable) {
            this.disposable = disp
        }

        override fun dispose() {
            disposable!!.dispose()
        }

        override fun isDisposed(): Boolean {
            return disposable!!.isDisposed
        }
    }

    fun <T> toObservableWithBlocking(eventId: Any, eventType: Class<T>, timeinterval: Long): T? {
        val queue = LinkedBlockingQueue<Any>()
        val dispObserver = DisposeObserver()
        rxBusNotice.ofType(Event::class.java)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .timeout(timeinterval, TimeUnit.MILLISECONDS)
                .filter { event ->
                    val current = Thread.currentThread()
                    Log.d(TAG, "filter: thread name " + current.name + " event id " + eventId + " event " + event.event_id)
                    event.event_id === eventId
                }
                .map { event ->
                    Log.d(TAG, "map: thread name " + Thread.currentThread().name)
                    event.message
                }
                .cast(eventType)
                .subscribeWith<Observer<T>>(object : Observer<T> {
                    override fun onSubscribe(d: Disposable) {
                        dispObserver.setDisposable(d)
                    }

                    override fun onNext(t: T) {
                        queue.offer(t)
                    }

                    override fun onError(e: Throwable) {
                        queue.offer("error")
                        Log.e(TAG, "queue offer error")
                    }

                    override fun onComplete() {}
                })

        try {
            val ret = queue.take() as T
            dispObserver.dispose()
            return if ("error" == ret) null else ret
        } catch (e: Exception) {
            Log.e(TAG, "queue exp")
        }

        return null
    }


    private fun postEvent(event: Event) {
        rxBusUI.onNext(event)
    }

    companion object {

        val default: RxBusKt
            get() = RxBusBuilder.RxBUS_INSTANCE
    }


}
