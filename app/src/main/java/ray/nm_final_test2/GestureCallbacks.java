package ray.nm_final_test2;

import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.apache.cordova.CordovaWebView;

/**
 * Created by Ray on 2014/12/21.
 */

/*
* This class handles callback functions of the main CordovaWebView
* in MainActivity.
*
*/

public class GestureCallbacks {
    private static String _logTag = "GestureCallback";
    private MainActivity ma;
    private long longPressThreshold;
    private long doubleTapThreshold;
    private CordovaWebView cwv;

    GestureCallbacks(MainActivity m, long l, long d){
        ma = m;
        longPressThreshold = l;
        doubleTapThreshold = d;
        cwv = ma.cwv;
    }

    public long getLongPressThreshold(){ return longPressThreshold; }
    public long getDoubleTapThreshold(){ return doubleTapThreshold; }

    public void onSingleTap(){
        Log.v(_logTag, "single tap at "+ma.touch_x+", "+ma.touch_y+".");
        ma.status.setText("single tap at "+ma.touch_x+", "+ma.touch_y+".");
        cwv.loadUrl("javascript:" +
                " Var.getSelectedHTMLCode(document.elementFromPoint(" + ma.touch_x + ", " + ma.touch_y + ").innerHTML); ");
    }

    public void onDoubleTap(){
        Log.v(_logTag, "double tap.");
        ma.status.setText("double tap at" + ma.touch_x + ", " + ma.touch_y + ".");
        cwv.loadUrl("javascript:" +
                " Var.showDialog(navigator ? navigator.userAgent.toLowerCase() : \"other\"); ");
    }

    public void onMoveUp(){
        ma.status.setText("move up.");
    }

    public void onMoveDown(){
        ma.status.setText("move down.");
    }
    public void onMoveLeft(){
        ma.status.setText("move left.");
    }
    public void onMoveRight(){
        ma.status.setText("move right.");
    }

    public void onLongPress(){
        if(ma.shadow == null){
            ma.status.setText("nothing selected. Drag bypassed.");
            return;
        }
        ma.status.setText("long press activates drag.");
        ma.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ma.shadow.layout(0, 0, ma.shadow.getWidth(), ma.shadow.getContentHeight());
                ma.mainll.removeView(ma.shadow);
                View.DragShadowBuilder myShadowBuilder = new View.DragShadowBuilder(ma.shadow) {
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
                ma.vibrator.vibrate(100);
                cwv.startDrag(null, myShadowBuilder, null, 0);
            }
        });
    }

    public void onLongPressMove(){
        ma.status.setText("LongPressMove");
    }

    public void onDragStart(){
        ma.status.setText("Drag starts");

    }

    public void onDragEnd(){
        ma.status.setText("Drag ends");
    }

    public void on2FingerScroll(){
        ma.status.setText("scrolling");
    }
}
