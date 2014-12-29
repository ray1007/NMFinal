// Declare callbackResponse object.
var myResponses = {
    statusBar : null,
    longPressThreshold : 600,
    doubleTapThreshold : 150,
    gestureCountThreshold : 10,
    onSingleTap : function(x, y){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "single tap at \("+x+", "+y+").";
        console.log("single tap.");
    },
    onDoubleTap : function(x, y){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "double tap at \("+x+", "+y+").";
        console.log("double tap.");
    },
    onLongPressStart : function(){
        if(this.statusBar != null)
                this.statusBar.innerHTML = "drag starts.";
        console.log("long press starts.");
        Android.startDrag();
    },
    onLongPressMove : function(x, y){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "moving after long press pageXY at \("+x+", "+y+").";
        console.log("moving after long press pageXY at \("+x+", "+y+").");
    },
    on2FingerMoveUp : function(){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "2FingerMoveUp.";
        console.log("2FingerMoveUp.");
    },
    on2FingerMoveDown : function(){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "2FingerMoveDown.";
        console.log("2FingerMoveDown.");
    },
    on2FingerMoveLeft : function(){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "2FingerMoveLeft.";
        console.log("2FingerMoveLeft.");
    },
    on2FingerMoveRight : function(){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "2FingerMoveRight.";
        console.log("2FingerMoveRight.");
    },
    onPinchIn : function(){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "pinch in.";
        console.log("pinch in.");
    },
    onPinchOut : function(){
        if(this.statusBar != null)
            this.statusBar.innerHTML = "pinch out.";
        console.log("pinch out.");
    }
};

jQuery(document).ready(function($){
    setListeners(myResponses);
    Android.hideSplashView();
});

// Implement gestureListeners
function setListeners(responses) {
    console.log("starts");
    var myListener = document.getElementById("myGestureListener");
    var myStatusBar  = document.getElementById("gestureStatus");
    
    var touch_longPress     = false;
    var touch_doubleTap     = false;
    var touch_still         = false;
    var touch_2Finger       = false;
    var touch_2FingerMoved  = false;
    var touch_quickDown     = false;
    var touch_up            = false;
    var touch_x, touch_y, touch_x2, touch_y2;
    var touch_2fingerUpCount = 0,
        touch_2fingerDownCount = 0, 
        touch_2fingerLefttCount = 0, 
        touch_2fingerRightCount = 0,
        touch_pinchInCount = 0,
        touch_pinchOutCount = 0;
    
    responses.statusBar = myStatusBar;
    
    var onTouchStart = function(event){
        if(touch_longPress)
            return;
        if(event.touches.length > 2)
            return;
        
        if(touch_quickDown){
            touch_doubleTap = true;
            responses.onDoubleTap(event.touches.item(0).pageX, event.touches.item(0).pageY);
        } else {
            // modify gesture flags.
            touch_2Finger = false;
            if(event.touches.length == 2){
                touch_2Finger = true;
                touch_x2 = event.touches.item(1).pageX;
                touch_y2 = event.touches.item(1).pageY;
            }
            touch_quickDown     = false;
            touch_still         = true;
            touch_doubleTap     = false;
            touch_up            = false;
            touch_2FingerMoved  = false;
        
            setTimeout(longPressChecker , responses.longPressThreshold);
        }
        // update new position.
        touch_x = event.touches.item(0).pageX;
        touch_y = event.touches.item(0).pageY;
    };
    
    var onTouchMove = function(event){
        event.preventDefault();
        if(event.touches.length > 2)
            return;
        touch_still = false;
        var touch = event.touches.item(0);
        
        if(touch_longPress){
            responses.onLongPressMove(event.touches.item(0).pageX, event.touches.item(0).pageY);
        } else {
            // handles "2 finger scroll"
            if(touch_2Finger && !touch_2FingerMoved){
                var secondTouch = event.touches.item(1);
                if(secondTouch == null)
                    return;

                var delta_x1 = touch.pageX - touch_x;
                var delta_y1 = touch.pageY - touch_y
                var delta_x2 = secondTouch.pageX - touch_x2;
                var delta_y2 = secondTouch.pageY - touch_y2;
                //console.log("2 finger: (dx1, dy1, dx2, dy2) = \("+delta_x1+", "+delta_y1+", "+delta_x2+", "+delta_y2+"\).");

                if(delta_x1*delta_x2 > 0 || delta_y1*delta_y2 > 0 ) {
                    if( Math.abs(delta_x1) > Math.abs(delta_y1) ){
                        if( (delta_x1) > 0){
                            touch_2fingerRightCount++;
                            touch_2fingerUpCount = touch_2fingerDownCount = touch_2fingerLeftCount = 0;
                            if(touch_2fingerRightCount == responses.gestureCountThreshold){
                                responses.on2FingerMoveRight();
                                touch_2FingerMoved = true;
                            }
                        } else {
                            touch_2fingerLeftCount++;
                            touch_2fingerUpCount = touch_2fingerDownCount = touch_2fingerRightCount = 0;
                            if(touch_2fingerLeftCount == responses.gestureCountThreshold){
                                responses.on2FingerMoveLeft();
                                touch_2FingerMoved = true;
                            }
                        }
                    } else {
                        if( (delta_y1) > 0){
                            touch_2fingerDownCount++;
                            touch_2fingerUpCount = touch_2fingerLeftCount = touch_2fingerRightCount = 0;
                            if(touch_2fingerDownCount == responses.gestureCountThreshold){
                                responses.on2FingerMoveDown();
                                touch_2FingerMoved = true;
                            }
                        } else {
                            touch_2fingerUpCount++;
                            touch_2fingerDownCount = touch_2fingerLeftCount = touch_2fingerRightCount = 0;
                            if(touch_2fingerUpCount == responses.gestureCountThreshold){
                                responses.on2FingerMoveUp();
                                touch_2FingerMoved = true;
                            }
                        }
                    }
                } else {
                    var prevXDist = Math.abs(touch_x - touch_x2);
                    var XDist = Math.abs(touch.pageX - secondTouch.pageX);
                    if(Math.abs(prevXDist - XDist) > 0){
                        if(prevXDist > XDist){
                            touch_pinchOutCount = 0;
                            touch_pinchInCount++;
                            if(touch_pinchInCount >= responses.gestureCountThreshold){
                                responses.onPinchIn(); touch_pinchInCount = 0;
                            }
                        } else {
                            touch_pinchInCount = 0;
                            touch_pinchOutCount++;
                            if(touch_pinchOutCount >= responses.gestureCountThreshold){
                                responses.onPinchOut(); touch_pinchOutCount = 0;
                            }
                        }
                    }

                }
                //touch_2FingerMoved = true;
            }
            // update new position.
            touch_x = event.touches.item(0).pageX;
            touch_y = event.touches.item(0).pageY;
        }
    };
    
    var onTouchEnd = function(event){
        touch_up = true;
        gestureCount = 0;
        if(event.touches.length == 0)
            touch_longPress = false;
        if(!touch_2Finger && !touch_longPress){
            touch_quickDown = true;
            setTimeout(doubleTapChecker, responses.doubleTapThreshold);
        }
    };
    
    var longPressChecker = function(event){
        if(touch_still && !touch_up && !touch_2Finger){
            touch_longPress = true ;
            responses.onLongPressStart();
        }
    };
    
    var doubleTapChecker = function(event){
        touch_quickDown = false ;
        if(!touch_doubleTap && touch_still){
            responses.onSingleTap(touch_x, touch_y);
        }
    };
    
    myListener.addEventListener('touchstart', onTouchStart, false);
    myListener.addEventListener('touchmove',  onTouchMove,  false);
    myListener.addEventListener('touchend',   onTouchEnd,   false);
}