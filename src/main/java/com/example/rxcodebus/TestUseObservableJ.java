package com.example.rxcodebus;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class TestUseObservableJ {
    private Observable          observable;
    private CompositeDisposable compositeDisposable;

    public TestUseObservableJ(){
        observable = RxBus.getDefault().toObservableWithUI("obs", String.class);
    }

    public void startObserve()
    {
        observable.subscribeWith(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        observable.subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {

            }
        });
    }

    public void onDestory()
    {
        if (observable != null)
            RxBus.getDefault().disposableObservable(observable);
    }
}
