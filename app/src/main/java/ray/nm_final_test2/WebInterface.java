package ray.nm_final_test2;

/**
 * Created by Ray on 2014/12/4.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import android.webkit.JavascriptInterface;

public class WebInterface {
    Context _context;
    public WebInterface(Context c){ _context = c; Log.v("WebInterface", "constructed");}

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

}
