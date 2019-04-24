package com.example.rxcodebus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class TestUseDisposableJ {

    private CompositeDisposable compositeDisposable;

    public TestUseDisposableJ()
    {
        compositeDisposable = new CompositeDisposable();
    }

    public void startObserable()
    {
        compositeDisposable.add(RxBus.getDefault().toObservableWithData("dis", String.class, new Consumer() {
            @Override
            public void accept(Object o) throws Exception {

            }
        }));

        compositeDisposable.add(RxBus.getDefault().toObservableWithData("dis", String.class, new Consumer() {
            @Override
            public void accept(Object o) throws Exception {

            }
        }));
    }

    public void onDestroy()
    {
        if (compositeDisposable != null)
            compositeDisposable.dispose();
    }
}
