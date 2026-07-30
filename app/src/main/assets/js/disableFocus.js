// Override document.visibilityState to always report "visible"
Object.defineProperty(document, 'hidden', {
    get: function() { return false; },
    configurable: true
});

Object.defineProperty(document, 'visibilityState', {
    get: function() { return 'visible'; },
    configurable: true
});

// Override hasFocus to always return true
Document.prototype.hasFocus = function() { return true; };

// Prevent visibilitychange events with "hidden" from propagating
document.addEventListener('visibilitychange', function(e) {
    e.stopImmediatePropagation();
}, true);

// Override document.onvisibilitychange setter
Object.defineProperty(document, 'onvisibilitychange', {
    set: function() {},
    get: function() { return null; },
    configurable: true
});
