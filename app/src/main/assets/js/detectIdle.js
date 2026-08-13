(function() {
    'use strict';

    if (window.__detectIdleRunning) return;
    window.__detectIdleRunning = true;

    var match = "Game ending in";

    function killPopups() {
    //    var selectors = ['[class*="modal"]', '[class*="overlay"]', '[class*="popup"]',
    //        '[class*="dialog"]', '[class*="backdrop"]', '[class*="scrim"]'];
    //    selectors.forEach(function(s) {
    //        document.querySelectorAll(s).forEach(function(el) {
    //            el.style.pointerEvents = "none";
    //        });
    //    });
        let overlay = document.querySelector("mat-snack-bar-container");
        if (overlay) {
            overlay.hidden = true;
        }
        else {
            console.log("[detectIdle] cant find overlay");
        }
    }
    function clickButton(btn) {
        //killPopups();
        //var outer = btn.parentElement;
        //outer.style.position = "relative";
        //outer.style.zIndex = "2147483647";
        //outer.style.pointerEvents = "auto";
        var rect = btn.getBoundingClientRect();
        var r = window.devicePixelRatio || 1;
        AndroidBridge.click((rect.left + rect.width / 2) * r, (rect.top + rect.height / 2) * r);
    }

    function checkIdle() {
        try {
            var stream = document.querySelector("video#remote-video");
            var title = document.title;
            var startBtn = document.querySelector('button[aria-label="Start"]');
            var continueBtn = Array.from(document.querySelectorAll('button'))
                .find(function(b) { return b.innerText == 'BACK TO GAME'; });

            //if (startBtn && stream) {
            //    clickButton(startBtn);
            //    console.log("[detectIdle] StartButton pressed");
            } if (continueBtn && stream) {
                clickButton(continueBtn);
                console.log("[detectIdle] ContinueButton pressed");
            //} else if (title.indexOf(match) !== -1 && stream) {
            } else if (stream) {
                AndroidBridge.keyPress();
                console.log("[detectIdle] Keep in session button pressed");
            }
        } catch (e) {
            console.log("[detectIdle] Error: " + e);
        }
    }

    setInterval(checkIdle, 1 * 60 * 1000); //1 min
    //setInterval(checkIdle, 500);
}());
