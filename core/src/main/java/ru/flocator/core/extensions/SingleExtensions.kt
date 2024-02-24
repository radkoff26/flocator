package ru.flocator.core.extensions

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun <T : Any> Single<T>.withCacheRequest(cacheSingle: Single<T>): Observable<T> {
    val compositeDisposable = CompositeDisposable()
    return Observable.create { emitter ->
        compositeDisposable.addAll(
            subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        emitter.onNext(it)
                        emitter.onComplete()
                    },
                    {
                        emitter.onError(it)
                        emitter.onComplete()
                    }
                ),
            cacheSingle.subscribeOn(Schedulers.io())
                .doOnSuccess {
                    emitter.onNext(it)
                }
                .subscribe()
        )
    }.doOnComplete {
        compositeDisposable.dispose()
    }
}