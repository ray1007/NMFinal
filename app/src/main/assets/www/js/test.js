
var tool=false;
var pos=false;
var scwidth;
var controlpro;
jQuery(document).ready(function($){
    scwidth=$("#innercontent").width();
    controlpro=scwidth*0.07;
    $('.logo').on( 'touchstart click', function ( startEvent ) {
    if(!pos)
    {
        $( "#controlpanel" ).css("left","0");
        $( "#controlpanel" ).transition({ opacity: 0.8},500,'snap');
        $( "#controlcontent" ).transition({ x: controlpro});
        pos=true;
    }
    else
    {
        $( "#controlcontent" ).transition({ x: -controlpro });
    pos=false;
    }
    });
    $ ("#controlpanel").on("touchstart click",function(startEvent)
    {
        $( "#controlcontent" ).transition({ x: -controlpro });
        $( "#controlpanel" ).transition({ opacity: 0},300,'snap',function(){
        $( "#controlpanel" ).css("left","-100%");
        });
        pos=false;
    });

    $ ("#innercontent").on("touchstart",function(startEvent)
    {
        // modified by Ray.
        document.getElementsByClassName("badge")[0].innerHTML = "Touched";
    });
     $ ("#innercontent").on("click",function(event)
    {
        // commented by Ray.
        //document.getElementsByClassName("badge")[0].innerHTML = "Touched";
        //alert("WTF");
    });

    /*
    * Added by Ray at 12/21
    *
    * The following codes makes scrolling listen only to 2-finger touch.
    */
    document.addEventListener('touchmove', function(e) {
        if(e.touches.length != 2)
            e.preventDefault();
    });

});
function test(){
    var rect = $("#header")[0].getBoundingClientRect();
    alert(rect.width);
    return true;
};
function OrientationChanged()
{
    scwidth=$("#innercontent").width();
    controlpro=scwidth*0.07;
};
window.addEventListener('orientationchange', OrientationChanged);
function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}