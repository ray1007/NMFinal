NMFinal
=======
12/21

內容：
*完成用 Android 判斷以下手勢：單點、雙點、移動、長按觸發拖移、拖移、雙指滾動
*完成用 Native Android 產生drag shadow。
*新增class GestureCallbacks，用來管理CordovaWebView的event listener們，提供一個一致的管理介面，避免需要在一堆程式裡尋找哪邊是負責某個手勢的callback function。

筆記：
更改部分assets/www中，test.js裡的內容
```
document.addEventListener('touchmove', function(e) {
        if(e.touches.length != 2)
            e.preventDefault();
    });
```
這段程式碼讓CordovaWebView只能用雙指做滾動手勢。

