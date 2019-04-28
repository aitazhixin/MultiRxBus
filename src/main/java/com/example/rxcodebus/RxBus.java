package com.example.rxcodebus;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {
    private final String TAG = "RxBUS";
    private final Subject<Object>                       rxBusUI;
    private final Subject<Object>                       rxBusData;
    private final Subject<Object>                       rxBusNotice;
    private       ExecutorService                       rxBusEs;
    private       ConcurrentLinkedDeque<Event>          eventBuffer;
    private       int                                   executor_number = 40;

    private static class RxBusBuilder{
        private static volatile RxBus RxBUS_INSTANCE = new RxBus();
    }

    public static RxBus getDefault()
    {
        return RxBusBuilder.RxBUS_INSTANCE;
    }

    private RxBus(){
        rxBusUI = PublishSubject.create().toSerialized();
        rxBusData = PublishSubject.create().toSerialized();
        rxBusNotice = PublishSubject.create().toSerialized();
        rxBusEs = Executors.newFixedThreadPool(executor_number);
        eventBuffer = new ConcurrentLinkedDeque<>();
    }

    public class Event{
        Object event_id;
        Object message;

        public Event(Object et, Object msg){
            this.event_id = et;
            this.message = msg;
        }
    }

    public void postUI(Object code, Object obj){
        Event event = new Event(code, obj);
        synchronized (eventBuffer){
            eventBuffer.add(event);
        }
        postEvent(event);
    }

    public void broadNotice(Object code, Object obj)
    {
        rxBusNotice.onNext(new Event(code, obj));
    }

    public void post(Object code, Object obj)
    {
        rxBusData.onNext(new Event(code, obj));
    }


    public <T>Observable<T> toObservableWithUI(final Object eventId, Class<T> eventType){
        synchronized (eventBuffer){
            Observable<Event> observable = rxBusUI.ofType(Event.class);
            Log.d(TAG, "buffer length " + eventBuffer.size());
            final Event event_top = eventBuffer.poll();
            Log.d(TAG, "buffer length " + eventBuffer.size());

            if (event_top != null)
            {
                return (Observable<T>)observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .mergeWith(Observable.create(new ObservableOnSubscribe<Event>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<Event> emitter) throws Exception {
                                        emitter.onNext(event_top);
                                    }
                                })
                                .filter(new Predicate<Event>() {
                                    @Override
                                    public boolean test(Event event) throws Exception {
                                        Thread current = Thread.currentThread();
                                        Log.d(TAG, "buffer filter: thread name " + current.getName());
                                        return event.event_id == eventId;
                                    }
                                }));
            }
            else
            {
                return observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .filter(new Predicate<Event>() {
                            @Override
                            public boolean test(Event event) throws Exception {
                                Thread current = Thread.currentThread();
                                Log.d(TAG, "normal filter: thread name " + current.getName());
                                synchronized (eventBuffer){
                                    Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size());
                                }
                                synchronized (eventBuffer){
                                    eventBuffer.poll();
                                }
                                return event.event_id == eventId;
                            }
                        })
                        .map(new Function<Event, Object>() {
                            @Override
                            public Object apply(Event event) throws Exception {
                                Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                                return event.message;
                            }
                        })
                        .cast(eventType);
            }
        }
    }

    public <T>Disposable toObservableWithUI(final Object eventId, Class<T> eventType, final Consumer consumer /*onNext*/){
        synchronized (eventBuffer){
            Observable<Event> observable = rxBusUI.ofType(Event.class);
            Log.d(TAG, "buffer length " + eventBuffer.size());
            final Event event_top = eventBuffer.poll();
            Log.d(TAG, "buffer length " + eventBuffer.size());

            if (event_top != null)
            {
                return (Disposable)observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .mergeWith(Observable.create(new ObservableOnSubscribe<Event>() {
                            @Override
                            public void subscribe(ObservableEmitter<Event> emitter) throws Exception {
                                emitter.onNext(event_top);
                            }
                        })
                                .filter(new Predicate<Event>() {
                                    @Override
                                    public boolean test(Event event) throws Exception {
                                        Thread current = Thread.currentThread();
                                        Log.d(TAG, "buffer filter: thread name " + current.getName());
                                        return event.event_id == eventId;
                                    }
                                }))
                        .subscribe(consumer);
            }
            else
            {
                return observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .filter(new Predicate<Event>() {
                            @Override
                            public boolean test(Event event) throws Exception {
                                Thread current = Thread.currentThread();
                                Log.d(TAG, "normal filter: thread name " + current.getName());
                                synchronized (eventBuffer){
                                    Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size());
                                }
                                synchronized (eventBuffer){
                                    eventBuffer.poll();
                                }
                                return event.event_id == eventId;
                            }
                        })
                        .map(new Function<Event, Object>() {
                            @Override
                            public Object apply(Event event) throws Exception {
                                Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                                return event.message;
                            }
                        })
                        .cast(eventType)
                        .subscribe(consumer);
            }
        }
    }

    public <T>Disposable toObservableWithUI(final Object eventId, Class<T> eventType, final Consumer consumerNext /*onNext*/,
                                            final Consumer consumerError /*onError*/)
    {
        synchronized (eventBuffer){
            Observable<Event> observable = rxBusUI.ofType(Event.class);
            Log.d(TAG, "buffer length " + eventBuffer.size());
            final Event event_top = eventBuffer.poll();
            Log.d(TAG, "buffer length " + eventBuffer.size());

            if (event_top != null)
            {
                return (Disposable)observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .mergeWith(Observable.create(new ObservableOnSubscribe<Event>() {
                            @Override
                            public void subscribe(ObservableEmitter<Event> emitter) throws Exception {
                                emitter.onNext(event_top);
                            }
                        })
                                .filter(new Predicate<Event>() {
                                    @Override
                                    public boolean test(Event event) throws Exception {
                                        Thread current = Thread.currentThread();
                                        Log.d(TAG, "buffer filter: thread name " + current.getName());
                                        return event.event_id == eventId;
                                    }
                                }))
                        .subscribe(consumerNext, consumerError);
            }
            else
            {
                return observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .filter(new Predicate<Event>() {
                            @Override
                            public boolean test(Event event) throws Exception {
                                Thread current = Thread.currentThread();
                                Log.d(TAG, "normal filter: thread name " + current.getName());
                                synchronized (eventBuffer){
                                    Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size());
                                }
                                synchronized (eventBuffer){
                                    eventBuffer.poll();
                                }
                                return event.event_id == eventId;
                            }
                        })
                        .map(new Function<Event, Object>() {
                            @Override
                            public Object apply(Event event) throws Exception {
                                Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                                return event.message;
                            }
                        })
                        .cast(eventType)
                        .subscribe(consumerNext, consumerError);
            }
        }
    }

    public <T>Observable<T> toObservableWithUIWithinTimeout(final Object eventId, Class<T> eventType, long timeinterval, TimeUnit unit){
        synchronized (eventBuffer){
            Observable<Event> observable = rxBusUI.ofType(Event.class);
            Log.d(TAG, "buffer length " + eventBuffer.size());
            final Event event_top = eventBuffer.poll();
            Log.d(TAG, "buffer length " + eventBuffer.size());

            if (event_top != null)
            {
                return (Observable<T>)observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, unit)
                        .mergeWith(Observable.create(new ObservableOnSubscribe<Event>() {
                            @Override
                            public void subscribe(ObservableEmitter<Event> emitter) throws Exception {
                                emitter.onNext(event_top);
                            }
                        })
                                .filter(new Predicate<Event>() {
                                    @Override
                                    public boolean test(Event event) throws Exception {
                                        Thread current = Thread.currentThread();
                                        Log.d(TAG, "buffer filter: thread name " + current.getName());
                                        return event.event_id == eventId;
                                    }
                                }));
            }
            else
            {
                return observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, unit)
                        .filter(new Predicate<Event>() {
                            @Override
                            public boolean test(Event event) throws Exception {
                                Thread current = Thread.currentThread();
                                Log.d(TAG, "normal filter: thread name " + current.getName());
                                synchronized (eventBuffer){
                                    Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size());
                                }
                                synchronized (eventBuffer){
                                    eventBuffer.poll();
                                }
                                return event.event_id == eventId;
                            }
                        })
                        .map(new Function<Event, Object>() {
                            @Override
                            public Object apply(Event event) throws Exception {
                                Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                                return event.message;
                            }
                        })
                        .cast(eventType);
            }
        }
    }

    public <T>Disposable toObservableWithUIWithinTimeout(final Object eventId, Class<T> eventType, final Consumer consumerNext /*onNext*/,
                                            final Consumer consumerError /*onError*/, long timeinterval, TimeUnit unit)
    {
        synchronized (eventBuffer){
            Observable<Event> observable = rxBusUI.ofType(Event.class);
            Log.d(TAG, "buffer length " + eventBuffer.size());
            final Event event_top = eventBuffer.poll();
            Log.d(TAG, "buffer length " + eventBuffer.size());

            if (event_top != null)
            {
                return (Disposable)observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, unit)
                        .mergeWith(Observable.create(new ObservableOnSubscribe<Event>() {
                            @Override
                            public void subscribe(ObservableEmitter<Event> emitter) throws Exception {
                                emitter.onNext(event_top);
                            }
                        })
                                .filter(new Predicate<Event>() {
                                    @Override
                                    public boolean test(Event event) throws Exception {
                                        Thread current = Thread.currentThread();
                                        Log.d(TAG, "buffer filter: thread name " + current.getName());
                                        return event.event_id == eventId;
                                    }
                                }))
                        .subscribe(consumerNext, consumerError);
            }
            else
            {
                return observable.observeOn(Schedulers.single())
                        .subscribeOn(Schedulers.single())
                        .timeout(timeinterval, unit)
                        .filter(new Predicate<Event>() {
                            @Override
                            public boolean test(Event event) throws Exception {
                                Thread current = Thread.currentThread();
                                Log.d(TAG, "normal filter: thread name " + current.getName());
                                synchronized (eventBuffer){
                                    Log.d(TAG, "buffer lenght in normal fileter " + eventBuffer.size());
                                }
                                synchronized (eventBuffer){
                                    eventBuffer.poll();
                                }
                                return event.event_id == eventId;
                            }
                        })
                        .map(new Function<Event, Object>() {
                            @Override
                            public Object apply(Event event) throws Exception {
                                Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                                return event.message;
                            }
                        })
                        .cast(eventType)
                        .subscribe(consumerNext, consumerError);
            }
        }
    }

    public <T>Observable<T> toObservableWithData(final Object eventId, Class<T> eventType)
    {
        return rxBusData.ofType(Event.class)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType);
    }

    public <T>Disposable toObservableWithData(final Object eventId, Class<T> eventType, final Consumer consumer /*onNext*/)
    {
        return rxBusData.ofType(Event.class)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribe(consumer);
    }

    public <T>Disposable toObservableWithData(final Object eventId, Class<T> eventType, final Consumer consumerNext /*onNext*/,
                                              final Consumer consumerError /*onError*/)
    {
        return rxBusData.ofType(Event.class)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribe(consumerNext, consumerError);
    }

    public <T>Observable<T> toObservableWithDataWithinTimeout(final Object eventId, Class<T> eventType, long timeinterval, TimeUnit unit)
    {
        return rxBusData.ofType(Event.class)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .timeout(timeinterval, unit)
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType);
    }

    public <T>Disposable toObservableWithDataWithinTimeout(final Object eventId, Class<T> eventType, final Consumer consumerNext /*onNext*/,
                                              final Consumer consumerError /*onError*/, long timeinterval, TimeUnit unit)
    {
        return rxBusData.ofType(Event.class)
                .subscribeOn(Schedulers.from(rxBusEs))
                .observeOn(Schedulers.from(rxBusEs))
                .timeout(timeinterval, unit)
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribe(consumerNext, consumerError);
    }

    public <T>Observable<T> toObservableWithNotice(final Object eventId, Class<T> eventType)
    {
        return rxBusNotice.ofType(Event.class)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType);
    }


    public <T>Disposable toObservableWithNotice(final Object eventId, Class<T> eventType, final Consumer consumer /*onNext*/)
    {
        return rxBusNotice.ofType(Event.class)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribe(consumer);
    }


    public <T>Disposable toObservableWithNotice(final Object eventId, Class<T> eventType, final Consumer consumerNext /*onNext*/,
                                                final Consumer consumerError /*onError*/)
    {
        return rxBusNotice.ofType(Event.class)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribe(consumerNext, consumerError);
    }

    public <T>Observable<T> toObservableWithNoticeWithinTimeout(final Object eventId, Class<T> eventType, long timeinterval, TimeUnit unit)
    {
        return rxBusNotice.ofType(Event.class)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .timeout(timeinterval, unit)
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType);
    }


    public <T>Disposable toObservableWithNoticeWithinTimeout(final Object eventId, Class<T> eventType, final Consumer consumerNext /*onNext*/,
                                                final Consumer consumerError /*onError*/, long timeinterval, TimeUnit unit)
    {
        return rxBusNotice.ofType(Event.class)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .timeout(timeinterval, unit)
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribe(consumerNext, consumerError);
    }

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    public <T> T toObservableWithBlocking(final Object eventId, Class<T> eventType, long timeinterval)
    {
        final BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        rxBusNotice.ofType(Event.class)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .timeout(timeinterval, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        Thread current = Thread.currentThread();
                        Log.d(TAG, "filter: thread name " + current.getName());
                        return event.event_id == eventId;
                    }
                })
                .map(new Function<Event, Object>() {
                    @Override
                    public Object apply(Event event) throws Exception {
                        Log.d(TAG, "map: thread name " + Thread.currentThread().getName());
                        return event.message;
                    }
                })
                .cast(eventType)
                .subscribeWith(new Observer<T>(){
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(T t) {
                        queue.offer(t);
                    }

                    @Override
                    public void onError(Throwable e) {
                        queue.offer("error");
                        Log.e(TAG, "queue offer error");
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        try {
            final T ret = (T) queue.take();
            compositeDisposable.dispose();
            if ("error".equals(ret))
                return null;
            return ret;
        } catch (Exception e)
        {
            Log.e(TAG, "queue exp");
        }

        return null;
    }


    private void postEvent(Event event)
    {
        rxBusUI.onNext(event);
    }



}
