package ray.nm_final_test2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewConfiguration;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.jcabi.github.*;
import java.util.*;


public class MainActivity extends FragmentActivity
                            implements DeployLoginDialog.OnDeployLoginFinishedListener {

    private static final String _logTag = "MainActivity";
    private int count = 0;
    private boolean touch_still = false,
                    touch_longPress = false,
                    //touch_checkedDown = false,
                    touch_quickDown = false,
                    touch_doubleTap = false;
    private Runnable touch_longPressChecker, touch_doubleTapChecker;
    private float touch_x, touch_y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(_logTag, "onCreate(): starts.");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_activity);

        Log.v(_logTag, "onCreate(): find views & preparation.");
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        String htmlCode = getResources().getString(R.string.html_source_code);
        final TextView status = (TextView) findViewById(R.id.status_bar);

        final WebView mainWebView = (WebView) findViewById(R.id.main_webview);

        mainWebView.getSettings().setJavaScriptEnabled(true);
        mainWebView.setWebChromeClient(new WebChromeClient());
        mainWebView.addJavascriptInterface(new WebInterface(this), "Var");

        touch_longPressChecker = new Runnable() {
            @Override
            public void run() {
                if(touch_still) {
                    touch_longPress = true;
                }
                //touch_checkedDown = true;
            }
        };
        touch_doubleTapChecker = new Runnable() {
            @Override
            public void run() {
                touch_quickDown = false;
                if(!touch_doubleTap && touch_still){
                    //mainWebView.loadUrl("javascript:" +
                    //        " function myFunction(a, b) { return a * b; } ");
                    mainWebView.loadUrl("javascript:" +
                            " function myFunction(a, b) { " +
                            "      return document.elementFromPoint(a,b).innerHTML; " +
                            " } ");
                    mainWebView.loadUrl("javascript:" +
                            //" document.getElementsByTagName(\"p\")[0].innerHTML= myFunction(3,4); "+
                            //" Var.showDialog(document.documentElement.outerHTML);" +
                            " Var.showDialog(myFunction("+touch_x+","+touch_y+"));");
                    status.setText("single tap at "+touch_x+", "+touch_y+".");
                    Log.v(_logTag, "single tap at "+touch_x+", "+touch_y+".");
                }
            }
        };
        mainWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                switch(motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if(touch_quickDown){
                            // double tap.
                            touch_doubleTap = true;
                            status.setText("double tap at"+touch_x+", "+touch_y+".");
                            Log.v(_logTag, "double tap.");
                            mainWebView.loadUrl("javascript:" +
                                    " Var.showDialog(navigator ? navigator.userAgent.toLowerCase() : \"other\"); ");
                        } else {
                            view.postDelayed(touch_longPressChecker, 600);
                            touch_longPress = false;
                            touch_still = true;
                            touch_doubleTap = false;
                        }
                        touch_x = x;
                        touch_y = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int slop = ViewConfiguration.get(view.getContext()).getScaledTouchSlop();
                        if( (Math.abs(touch_x-x) > slop || Math.abs(touch_y-y) > slop) && touch_still){
                            touch_still = false;
                            Log.v(_logTag, "no longer still.");
                        }
                        if(!touch_doubleTap){
                            float dx = touch_x - x;
                            float dy = touch_y - y;
                            if(Math.abs(dx) > Math.abs(dy)){
                                if(dx > 0){ status.setText("move left."); Log.v(_logTag, "move left."); }
                                else { status.setText("move right."); Log.v(_logTag, "move right."); }
                            }
                            else {
                                if(dy > 0) { status.setText("move up."); Log.v(_logTag, "move up."); }
                                else { status.setText("move down."); Log.v(_logTag, "move down."); }
                            }
                        }
                        touch_x = x;
                        touch_y = y;
                        if(touch_longPress){
                            status.setText("move in long press at "+touch_x+", "+touch_y+".");
                            Log.v(_logTag, "move in long press.");
                            break;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(touch_longPress){
                            touch_longPress = false;
                            break;
                        }
                        touch_quickDown = true;
                        touch_x = x;
                        touch_y = y;
                        view.postDelayed(touch_doubleTapChecker, 150);
                        break;
                }
                return false;
            }
        });
        mainWebView.loadData(htmlCode, "text/html", "utf-8");

        Button deploy_btn = (Button) findViewById(R.id.deploy_btn);
        deploy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeployLoginDialog().show(getSupportFragmentManager(), "deploy_login");
            }
        });
    }

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
    private class DeployTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog _mProgressDialog = null;
        private String username, password;

        DeployTask(String u, String p){
            username = u;
            password = p;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _mProgressDialog = new ProgressDialog(MainActivity.this);
            _mProgressDialog.setTitle("Loading data from your GitHub repository");
            _mProgressDialog.setMessage("Loading...");
            _mProgressDialog.setIndeterminate(false);
            _mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            _mProgressDialog.dismiss();
        }
    }
    */
}
