package com.example.rxcodebus;


public interface RxSubscription {

    public void startSubscribe(Object eventId, Class eventClass);
    public void onDispose();
    public void parse(Object eventId, Object msg);

}