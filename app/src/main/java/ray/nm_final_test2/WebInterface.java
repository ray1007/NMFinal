package ray.nm_final_test2;

/**
 * Created by Ray on 2014/12/4.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import android.webkit.JavascriptInterface;

public class WebInterface {
    Context _context;
    MainActivity _activity;

    public WebInterface(Context c, MainActivity a){
        _context = c;
        _activity = a;
        Log.v("WebInterface", "constructed");
    }

    @JavascriptInterface
    public void showToast(String msg){
        Log.v("WebInterface", "fired");
        Toast.makeText(_context, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void showDialog(String msg){
        Log.v("JSinterface", msg);
        new AlertDialog.Builder(_context)
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
    public void getSelectedHTMLCode(String htmlCode){
        _activity.setSelectedHTML(htmlCode);
    }
}
