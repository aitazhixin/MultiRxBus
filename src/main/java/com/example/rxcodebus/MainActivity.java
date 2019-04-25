package com.example.rxcodebus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements RxSubscription {
    private final String TAG = "RxBUS";

    private Disposable disposable;
    private Observable observable;

    private EditText editer;
    private EditText editText;
    private TextView texter;
    private TextView texter2;

//    static {
//        System.setProperty("rx2.computation-threads", "2");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editer = findViewById(R.id.editer);
        texter = findViewById(R.id.texter);
        editText = findViewById(R.id.editer2);
        texter2 = findViewById(R.id.texter2);
        String str100 = "128";
        String str1002 = "128";
        String str1003 = "128";
        Integer in100 = Integer.parseInt(str100);
        Integer in1002 = Integer.parseInt(str1002);
        Integer in1003 = Integer.parseInt(str1003);
        Integer in1004 = 129;
        RxBus.getDefault().postUI((byte)0x02, "test");

        editer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RxBus.getDefault().post((byte)0x01, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RxBus.getDefault().broadNotice((byte)0x02, s.toString());
//                observable.subscribeWith(new Observer() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Object o) {
//                        Log.d(TAG, "subscribe test");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        ObservableOnSubscribe observableOnSubscribe = new ObservableOnSubscribe() {
//            @Override
//            public void subscribe(ObservableEmitter emitter) throws Exception {
//                emitter.onNext("test");
//                Log.d(TAG, "observableOnSubscribe subscribe called");
//            }
//        };

//        observable = Observable.create(observableOnSubscribe);
//
//        observable.subscribeWith(new Observer() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(Object o) {
//                Log.d(TAG, "loc test: " + (String)o);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
//
//
//        ObservableEmitter emitter = new ObservableEmitter() {
//            @Override
//            public void setDisposable(Disposable d) {
//
//            }
//
//            @Override
//            public void setCancellable(Cancellable c) {
//
//            }
//
//            @Override
//            public boolean isDisposed() {
//                return false;
//            }
//
//            @Override
//            public ObservableEmitter serialize() {
//                return null;
//            }
//
//            @Override
//            public boolean tryOnError(Throwable t) {
//                return false;
//            }
//
//            @Override
//            public void onNext(Object value) {
//                Log.d(TAG, "ObservableEmitter onNext: send " + (String)value);
//            }
//
//            @Override
//            public void onError(Throwable error) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        };
//
//        try {
//            observableOnSubscribe.subscribe(emitter);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            Log.e(TAG, "observableOnSubscribe.subscribe(emitter) exception");
//        }

        startSubscribe((byte)0x02, String.class);
//        startSubscribe((byte)0x02, String.class);

    }

    @Override
    protected void onDestroy(){
        onDispose();
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        onDispose();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startSubscribe(final Object event_id, final Class event_type)
    {
        observable = RxBus.getDefault().toObservableWithNotice(event_id, event_type);
        disposable = observable.subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.d(TAG, "Consumer test");
                    }
                });

        observable.subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                Log.d(TAG, "Consumer test new ");
            }
        });

        observable.subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                Log.d(TAG, "Consumer test renew");
            }
        });

        disposable.dispose();

//        RxBus.getDefault().toObservableWithNotice(event_id, event_type, new Consumer() {
//            @Override
//            public void accept(Object o) throws Exception {
//                Log.d(TAG, "Consumer Interface");
//            }
//        });
        RxBus.getDefault().toObservableWithNoticeWithinTimeout(event_id, event_type, 3500, TimeUnit.MILLISECONDS)
                .subscribeWith(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "with timeout: subscribe");
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.d(TAG, "with timeout: next");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "with timeout: error");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "with timeout: complete");
                    }
                });

        RxBus.getDefault().toObservableWithNoticeWithinTimeout(event_id, event_type, new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.d(TAG, "accept next in timeout");
                    }
                },
                new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Log.e(TAG, "accept error timeout");
                    }
                },
                3000,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDispose() {
//        disposable.dispose();
    }

    @Override
    public void parse(Object event_id, Object msg) {
        Thread current = Thread.currentThread();
        Log.d(TAG, "thread id " + current.getId() + " name " + current.getName() + " priority " + current.getPriority());
        if ((byte)event_id == (byte)0x01) {
            texter.setText((String) msg);
        }
        else if ((byte)event_id == (byte)0x02)
        {
            texter2.setText((String)msg);
        }
    }
}
