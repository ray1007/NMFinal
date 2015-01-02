package ray.nm_final_test2;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.Config;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;
import com.jcabi.github.*;
import java.util.*;
import java.util.concurrent.*;


public class MainActivity extends FragmentActivity
                          implements CordovaInterface,
                                     DeployLoginDialog.OnDeployLoginFinishedListener {

    private static final String _logTag = "MainActivity";

    protected CordovaWebView cwv = null;
    protected FrameLayout mainll;
    protected String selectedHTML;
    protected CordovaWebView shadow;
    protected VideoView splashVid;
    protected RelativeLayout splashView;
    protected Vibrator vibrator;
    private boolean splash = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(_logTag, "onCreate(): starts.");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_activity);

        Log.v(_logTag, "onCreate(): find views & preparation.");
        splashVid = (VideoView) findViewById(R.id.splash_vid);
        splashView = (RelativeLayout) findViewById(R.id.splash_view);

        splashVid.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(splash) {
                    splashVid.seekTo(0 * 1000);
                    splashVid.start();
                } else {
                    splashView.setVisibility(View.GONE);
                }
            }
        });
        splashVid.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash));
        splashVid.start();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mainll = (FrameLayout) findViewById(R.id.mainLL);
        LayoutTransition splashTrans = new LayoutTransition();
        splashTrans.enableTransitionType(LayoutTransition.DISAPPEARING);
        mainll.setLayoutTransition(splashTrans);

        cwv = (CordovaWebView) findViewById(R.id.main_webview);
        Config.init(this);
        cwv.loadUrl(Config.getStartUrl());
        cwv.addJavascriptInterface(this, "Android");
        setCordovaWebViewGestures(cwv);
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
    private void setCordovaWebViewGestures(final CordovaWebView cordovaWV){
        /*
        * @params CordovaWebView cwv : The embedded CordovaWebView of the activity.
        * @return void               : No return needed.
        *
        * The following codes are for setting drag listeners.
        * "drag start", "drag end" callback functions are executed here.
        */
        cordovaWV.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                final int action = dragEvent.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
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
                        // tell javascript.
                        cordovaWV.loadUrl("javascript:" +
                                "");
                        return true;
                }
                return false;
            }
        });
    }


    /*
    * JavascriptInterface functions
    */

    @JavascriptInterface
    public void showToast(String msg){
        Log.v("WebInterface", "fired");
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void showDialog(String msg){
        Log.v("JSinterface", msg);
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
    }

    @JavascriptInterface
    public void hideSplashView(){
        splash = false;
    }

    @JavascriptInterface
    public void setSelectedHTML(String s){
        selectedHTML = s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shadow = new CordovaWebView(MainActivity.this);
                shadow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                mainll.addView(shadow);

                String prefix = "<link rel=\"stylesheet\" href=\"android_asset/css/bootstrap.min.css\">\n"+
                                "<script src=\"android_asset/js/jquery-1.11.1.min.js\"></script>\n" +
                                "<script src=\"android_asset/js/bootstrap.min.js\"></script>\n";
                selectedHTML = prefix + selectedHTML;
                shadow.loadData(selectedHTML, "text/html", "utf-8");
            }
        });
    }

    @JavascriptInterface
    public void startDrag(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(shadow == null){
                    vibrator.vibrate(100);
                    return;
                }
                shadow.layout(0, 0, shadow.getWidth(), shadow.getContentHeight());
                mainll.removeView(shadow);
                View.DragShadowBuilder myShadowBuilder = new View.DragShadowBuilder(shadow) {
                    @Override
                    public void onProvideShadowMetrics(Point size, Point touch) {
                        int width, height;

                        width = getView().getWidth() / 2;
                        height = getView().getHeight();
                        size.set(width, height);

                        // Sets the touch point's position
                        touch.set(width / 4, height / 4);
                    }
                };

                // Drag starts.
                vibrator.vibrate(100);
                cwv.startDrag(null, myShadowBuilder, null, 0);
            }
        });
    }
}
