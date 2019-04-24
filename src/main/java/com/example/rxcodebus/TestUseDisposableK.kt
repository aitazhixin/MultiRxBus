package com.example.rxcodebus

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class TestUseDisposableK {
    private val compositeDisposable = CompositeDisposable()

    operator fun CompositeDisposable.plusAssign(disposable : Disposable?){
        if (disposable != null)
            add(disposable)
    }

    fun startObserable()
    {
        compositeDisposable += RxBusKt.default.toObservableWithData("disp", String::class.java, object : Consumer<String>{
            override fun accept(t: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        compositeDisposable += RxBusKt.default.toObservableWithData("disp", String::class.java, object : Consumer<String>{
            override fun accept(t: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun onDestroy(){
        compositeDisposable.dispose()
    }
}