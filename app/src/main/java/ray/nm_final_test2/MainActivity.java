package ray.nm_final_test2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewConfiguration;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.Config;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.api.CordovaPlugin;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jcabi.github.*;
import java.util.*;
import java.util.concurrent.*;


public class MainActivity extends FragmentActivity
                          implements CordovaInterface,
                                     DeployLoginDialog.OnDeployLoginFinishedListener {

    private static final String _logTag = "MainActivity";
    private int count = 0;

    protected boolean touch_still = false,
                    touch_longPress = false,
                    touch_up = false,
                    touch_quickDown = false,
                    touch_doubleTap = false,
                    touch_drag = false,
                    touch_multi = false;

    protected Runnable touch_longPressChecker, touch_doubleTapChecker;
    protected float touch_x, touch_y;

    protected static CordovaWebView cwv = null;
    protected static LinearLayout mainll;
    protected static TextView status;
    protected String selectedHTML;
    protected CordovaWebView shadow;
    protected Vibrator vibrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(_logTag, "onCreate(): starts.");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_activity);

        cwv = (CordovaWebView) findViewById(R.id.main_webview);
        Config.init(this);
        cwv.loadUrl(Config.getStartUrl());
        cwv.addJavascriptInterface(new WebInterface(this, this), "Var");

        Log.v(_logTag, "onCreate(): find views & preparation.");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        status = (TextView) findViewById(R.id.status_bar);
        mainll = (LinearLayout) findViewById(R.id.mainLL);

        setCordovaWebViewGestures(cwv, new GestureCallbacks(this, 600, 150));

        Button deploy_btn = (Button) findViewById(R.id.deploy_btn);
        deploy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeployLoginDialog().show(getSupportFragmentManager(), "deploy_login");
            }
        });
    }

    /*
    * The following are the function must be declared since MainActivity
    * implements CordovaInterface.
    */
    // Plugin to call when activity result is received
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;

    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Object onMessage(String id, Object data) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        if (cwv.pluginManager != null) {
            cwv.pluginManager.onDestroy();
        }
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    /*
    * The following are the member functions of MainActivity.
    */

    public void onLoginFinished(Github github, Vector<CharSequence> repoNames){
        String msg="";
        for(CharSequence c : repoNames ){
            msg += c + "\n";
        }
        new AlertDialog.Builder(this)
                .setTitle("html code")
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing !
                    }
                })
                .show();
        //new DeployChoiceDialog().show(getSupportFragmentManager(), "deploy_choice");
    }

    /*
    * The following function is called in onCreate() to set up gestures of CordovaWebView.
    */
    private void setCordovaWebViewGestures(CordovaWebView cordovaWV, final GestureCallbacks callbacks){
        /*
        * This function is separated from "onCreate()" to make codes
        * clean. Also with the aid of class GestureCallbacks to manage
        * callback functions in a better way.
        *
        * In this function, gestures such as "single tap", "double tap",
        * "move", "long press", "drag" are handled. You just need to set
        * up the desired corresponding functions to execute in GestureCallbacks
        * class, and the following codes would set them properly.
        *
        * @params CordovaWebView cwv : The embedded CordovaWebView of the activity.
        * @params
        * @return void               : No return needed.
        *
        */

        /*
        * The following Runnable is for checking if a new press is a long press
        * by using "postDelayed()" function of class View.
        *
        */
        touch_longPressChecker = new Runnable() {
            @Override
            public void run() {
                if(touch_still && !touch_up && !touch_multi) {
                    touch_longPress = true;
                    callbacks.onLongPress();
                }
            }
        };

        /*
        * The following Runnable is for checking if a new press is a double tap
        * by using "postDelayed()" function of class View.
        *
        * "single tap" callback functions are executed here.
        *
        */
        touch_doubleTapChecker = new Runnable() {
            @Override
            public void run() {
                touch_quickDown = false;
                if(!touch_doubleTap && touch_still){
                    callbacks.onSingleTap();
                }
            }
        };

        /*
        * The following codes are for setting touch listeners. Conditions of
        * gestures are set here.
        *
        * "double tap", "move", "long press" callback functions are executed here.
        *
        */
        cordovaWV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        touch_multi = true;
                        break;
                    case MotionEvent.ACTION_DOWN:
                        if (touch_quickDown) {
                            touch_doubleTap = true;
                            callbacks.onDoubleTap();
                        } else {
                            view.postDelayed(touch_longPressChecker, callbacks.getLongPressThreshold());
                            touch_longPress = false;
                            touch_still = true;
                            touch_doubleTap = false;
                            touch_drag = false;
                            touch_up = false;
                        }
                        touch_x = x;
                        touch_y = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int slop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
                        if(touch_multi){
                            callbacks.on2FingerScroll();
                            break;
                        }
                        if ((Math.abs(touch_x - x) > slop || Math.abs(touch_y - y) > slop) && touch_still) {
                            touch_still = false;
                        }
                        if (!touch_doubleTap && !touch_longPress) {
                            float dx = touch_x - x;
                            float dy = touch_y - y;
                            if (Math.abs(dx) > Math.abs(dy)) {
                                if (dx > 0) {
                                    callbacks.onMoveLeft();
                                } else {
                                    callbacks.onMoveRight();
                                }
                            } else {
                                if (dy > 0) {
                                    callbacks.onMoveUp();
                                } else {
                                    callbacks.onMoveDown();
                                }
                            }
                        }
                        touch_x = x;
                        touch_y = y;
                        if (touch_longPress)
                            if (!touch_drag) { touch_drag = true; }

                        break;
                    case MotionEvent.ACTION_UP:
                        if (touch_longPress) {
                            touch_longPress = false;
                            break;
                        }
                        touch_multi = false;
                        touch_up = true;
                        touch_quickDown = true;
                        touch_x = x;
                        touch_y = y;
                        view.postDelayed(touch_doubleTapChecker, callbacks.getDoubleTapThreshold());
                        break;
                }
                return false;
            }
        });

        /*
        * The following codes are for setting drag listeners.
        *
        * "drag start", "drag end" callback functions are executed here.
        */
        cordovaWV.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                final int action = dragEvent.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        callbacks.onDragStart();
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;

                    case DragEvent.ACTION_DROP:
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        callbacks.onDragEnd();
                        return true;
                }
                return false;
            }
        });
    }

    public void setSelectedHTML(String s){
        selectedHTML = s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shadow = new CordovaWebView(MainActivity.this);
                shadow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                mainll.addView(shadow);
                shadow.loadData(selectedHTML, "text/html", "utf-8");
            }
        });
    }
}
