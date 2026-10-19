package local.kishore.quiettv;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/** No window inspection, UI events, or key filtering. Only a user-requested screen lock. */
public class SleepService extends AccessibilityService {
    private static SleepService connected;
    @Override protected void onServiceConnected() { connected=this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() { if(connected==this)connected=null;super.onDestroy(); }
    public static boolean sleep() {
        return connected!=null && connected.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
    }
}
