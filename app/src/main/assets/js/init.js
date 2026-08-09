// Auto-injected on every page load
// Runs after bridge.js

(function() {
    'use strict';

    function init() {
        // Example: log page load
        if (window.AndroidBridge) {
            window.AndroidBridge.log('Page loaded: ' + window.location.href);
        }

        // Example: listen for bridge ready
        document.addEventListener('androidBridgeReady', function() {
            console.log('[Android] Bridge ready');
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);

    } else {
        init();
    }
})();
