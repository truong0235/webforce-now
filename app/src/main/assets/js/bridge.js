// JS-side bridge for two-way Java <-> JS communication
// Exposed as window.AndroidBridge

(function() {
    'use strict';

    // Callback registry for Java -> JS responses
    var callbacks = {};
    var callbackId = 0;

    window.AndroidBridge = {
        // Called from Java via evaluateJavascript
        // Java calls: webView.evaluateJavascript("AndroidBridge.onResponse(id, data)", null)
        onResponse: function(id, data) {
            var cb = callbacks[id];
            if (cb) {
                cb(data);
                delete callbacks[id];
            }
        },

        // Register a callback, returns id for Java to use
        registerCallback: function(callback) {
            var id = ++callbackId;
            callbacks[id] = callback;
            return id;
        },

        // Utility: send message to Java side
        sendToJava: function(method, data) {
            // Java will define window.AndroidBridgeHandler.handleMessage(method, data)
            if (window.AndroidBridgeHandler && window.AndroidBridgeHandler.handleMessage) {
                window.AndroidBridgeHandler.handleMessage(method, data);
            }
        },

        // Utility: log from JS (appears in Android logcat)
        log: function(message) {
            this.sendToJava('log', message);
        }
    };

    // Dispatch custom event for page-load scripts to listen
    document.addEventListener('DOMContentLoaded', function() {
        document.dispatchEvent(new CustomEvent('androidBridgeReady'));
    });
})();
