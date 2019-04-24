package com.example.rxcodebus;

import android.util.Log;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
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

    private void postEvent(Event event)
    {
        rxBusUI.onNext(event);
    }



}
