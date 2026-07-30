(function() {
    'use strict';

    if (window.__detectIdleRunning) return;
    window.__detectIdleRunning = true;

    var match = "Game ending in";

    function checkIdle() {
        try {
            var title = document.title;
            var stream = document.querySelector("video#remote-video");
            //var startButton = document.querySelector('button[aria-label="Start"]'); //inital "Lets go" button
            //var continueButton = document.querySelector('button[color="accent"]'); //"back to game" button
            //if (startButton && stream) { //to make sure that its ingame, might detect same button in other area
            //    var startButtonCords = startButton.getBoundingClientRect();
            //    AndroidBridge.click(startButtonCords.left + startButtonCords.width /2 , startButtonCords.top + startButtonCords.height / 2);
            //    AndroidBridge.log("[detectIdle]" + "StartButton pressed");

            //}
            //if (continueButton && stream) {
            //    var continueButtonCords = continueButton.getBoundingClientRect();
            //    AndroidBridge.click(continueButtonCords.left + continueButtonCords.width /2 , continueButtonCords.top + continueButtonCords.height / 2);
            //    AndroidBridge.log("[detectIdle]" + "ContinueButton pressed");

            //}
            if (title.indexOf(match) !== -1 && stream) {
                AndroidBridge.keyPress();
                setTimeout(AndroidBridge.keyPress(), 10*1000); //send another key after 10 sec
                AndroidBridge.log("[detectIdle]" + "Key pressed");
            }
        } catch (e) {
            AndroidBridge.log("[detectIdle]" + "Error: " + e);
        }
    }

    setInterval(checkIdle, 10*1000); //10 secs

    //setInterval(checkIdle, 1*60*1000); //1 min
    //setInterval(checkIdle, 5*60*1000); //5 mins
}());
