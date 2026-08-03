(function() {
    'use strict';

    if (window.__detectIdleRunning) return;
    window.__detectIdleRunning = true;

    var match = "Game ending in";

    function killPopups() {
        var selectors = ['[class*="modal"]', '[class*="overlay"]', '[class*="popup"]',
            '[class*="dialog"]', '[class*="backdrop"]', '[class*="scrim"]'];
        selectors.forEach(function(s) {
            document.querySelectorAll(s).forEach(function(el) {
                el.style.pointerEvents = "none";
            });
        });
    }

    function clickButton(btn) {
        killPopups();
        btn.style.position = "relative";
        btn.style.zIndex = "2147483647";
        btn.style.pointerEvents = "auto";
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

            if (startBtn && stream) {
                clickButton(startBtn);
                AndroidBridge.log("[detectIdle] StartButton pressed");
            } else if (continueBtn && stream) {
                clickButton(continueBtn);
                AndroidBridge.log("[detectIdle] ContinueButton pressed");
            } else if (title.indexOf(match) !== -1 && stream) {
                AndroidBridge.keyPress();
                setTimeout(AndroidBridge.keyPress(), 30*1000);
                AndroidBridge.log("[detectIdle] Key pressed");
            }
        } catch (e) {
            AndroidBridge.log("[detectIdle] Error: " + e);
        }
    }

    setInterval(checkIdle, 10 * 1000);
}());
