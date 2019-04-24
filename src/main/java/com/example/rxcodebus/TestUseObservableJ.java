package com.example.rxcodebus;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class TestUseObservableJ {
    private Observable          observable;
    private CompositeDisposable compositeDisposable;
    private Disposable disposable;

    public TestUseObservableJ(){
        observable = RxBus.getDefault().toObservableWithUI("obs", String.class);
        compositeDisposable = new CompositeDisposable();
    }

    public void startObserve()
    {
        observable.subscribeWith(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
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

        compositeDisposable.add(observable.subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {

            }
        }));

        compositeDisposable.add(observable.subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {

            }
        }));

        compositeDisposable.add(RxBus.getDefault().toObservableWithData("obs", String.class).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {

            }
        }));
    }

    public void onDestroy()
    {
        if (compositeDisposable != null)
            compositeDisposable.dispose();

        if (disposable != null)
            disposable.dispose();
    }
}
