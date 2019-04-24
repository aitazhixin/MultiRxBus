package com.example.rxcodebus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.OutputStream;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements RxSubscription {

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
                RxBus.getDefault().postUI((byte)0x02, s.toString());
                observable.subscribeWith(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {
                        Log.d("RxBUS", "subscribe test");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ObservableOnSubscribe observableOnSubscribe = new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter emitter) throws Exception {
                emitter.onNext("test");
                Log.d("RxBUS", "observableOnSubscribe subscribe called");
            }
        };

        Observable loc_observable = Observable.create(observableOnSubscribe);
//
//        loc_observable.subscribeWith(new Observer() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(Object o) {
//                Log.d("RxBUS", "loc test: " + (String)o);
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
//        Log.d("RxBUS", "subscribeWith");


        ObservableEmitter emitter = new ObservableEmitter() {
            @Override
            public void setDisposable(Disposable d) {

            }

            @Override
            public void setCancellable(Cancellable c) {

            }

            @Override
            public boolean isDisposed() {
                return false;
            }

            @Override
            public ObservableEmitter serialize() {
                return null;
            }

            @Override
            public boolean tryOnError(Throwable t) {
                return false;
            }

            @Override
            public void onNext(Object value) {
                Log.d("RxBUS", "ObservableEmitter onNext: send " + (String)value);
            }

            @Override
            public void onError(Throwable error) {

            }

            @Override
            public void onComplete() {

            }
        };

        try {
            observableOnSubscribe.subscribe(emitter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("RxBUS", "observableOnSubscribe.subscribe(emitter) exception");
        }


        Thread current = Thread.currentThread();
        Log.d("RxBUS", "thread id " + current.getId() + " name " + current.getName() + " priority " + current.getPriority() + " system core num " + Runtime.getRuntime().availableProcessors());

        startSubscribe((byte)0x02, String.class);

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

    private int obs_idx = 0;

    @Override
    public void startSubscribe(final Object event_id, final Class event_type)
    {
        RxBus.getDefault().toObservableWithUI(event_id, event_type)
                .subscribeWith(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {
//                        disposable = d;
                    }

                    @Override
                    public void onNext(Object o) {
                        Log.d("RxBUS", " system time " + System.currentTimeMillis() + " thread name " + Thread.currentThread().getName());
                        int id = 0;
                        while(id++ < 10)
                        {
                            try {
                                Thread.sleep(5);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        Thread current = Thread.currentThread();
                        Log.d("RxBUS", " thread id " + current.getId() + " name " + current.getName() + " priority " + current.getPriority());
//                        Log.d("RxBUS", "1 system time " + System.currentTimeMillis());
//                        parse(event_id, o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
//                        Log.d("RxBUS", "onError: " + e.getMessage());
//                        if (disposable.isDisposed())
//                            startSubscribe(event_id, event_type);
                    }

                    @Override
                    public void onComplete() {
//                        if (disposable.isDisposed())
//                            startSubscribe(event_id, event_type);
                    }
                });
    }

    @Override
    public void onDispose() {
//        disposable.dispose();
    }

    @Override
    public void parse(Object event_id, Object msg) {
        Thread current = Thread.currentThread();
        Log.d("RxBUS", "thread id " + current.getId() + " name " + current.getName() + " priority " + current.getPriority());
        if ((byte)event_id == (byte)0x01) {
            texter.setText((String) msg);
        }
        else if ((byte)event_id == (byte)0x02)
        {
            texter2.setText((String)msg);
        }
    }
}
