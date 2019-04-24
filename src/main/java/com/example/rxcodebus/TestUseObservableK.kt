package com.example.rxcodebus

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class TestUseObservableK constructor() {
    private var observable: Observable<*>? = null
    private val composite = CompositeDisposable()
    private var disposable : Disposable? = null

    operator fun CompositeDisposable.plusAssign(disposable: Disposable?){
        if (disposable != null)
            add(disposable)
    }

    init {
        observable = RxBusKt.default.toObservableWithUI("obs", String::class.java)
    }

    fun startObserve(){
        observable?.subscribeWith<Observer<Any>>(object : Observer<Any> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onComplete() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onError(e: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onNext(t: Any) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        composite += observable?.subscribe(object : Consumer<Any> {
            override fun accept(t: Any?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        composite += observable?.subscribe(object : Consumer<Any> {
            override fun accept(t: Any?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        composite += RxBusKt.default.toObservableWithData("obs", String::class.java).subscribe(object : Consumer<Any> {
            override fun accept(t: Any?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun onDestroy(){
        composite.dispose()
        disposable?.dispose()
    }
}
