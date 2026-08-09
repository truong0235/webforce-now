var isInQueue = false;

(function() {

    if (window.__inQueueRunning) return;
    window.__inQueueRunning = true;

    function inQueue(){

        var queueNumb = document.querySelector('span.font-sub1') ? document.querySelector('span.font-sub1').innerText : null;
        //var stream = document.querySelector("video#remote-video"); //stream exist right after in queue
        if (isInQueue == true && !queueNumb) {
            isInQueue = false;
            AndroidBridge.playSound();
        } else if (queueNumb) {
            isInQueue = true;
            //AndroidBridge.updateQueueNotification(queueNumb); // not exist yet
        }
    }

    setInterval(inQueue, 10*1000);



    //eruda script
    var script = document.createElement('script');
    script.src="https://cdn.jsdelivr.net/npm/eruda";
    document.body.append(script);
    script.onload = function () { eruda.init(); }


}());

