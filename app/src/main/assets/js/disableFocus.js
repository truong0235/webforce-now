Object.defineProperty(document, 'hidden', { value: false, configurable: true });
Object.defineProperty(document, 'visibilityState', { value: 'visible', configurable: true });
document.hasFocus = () => true;

window.onblur = null;
window.onfocus = null;
document.onvisibilitychange = null;

const blockEvents = ['visibilitychange', 'blur', 'focus', 'focusout', 'fullscreenchange'];
const origAddEventListener = EventTarget.prototype.addEventListener;
EventTarget.prototype.addEventListener = function(type, listener, options) {
    if (!blockEvents.includes(type)) {
        return origAddEventListener.call(this, type, listener, options);
    }
};
