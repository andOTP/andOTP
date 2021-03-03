package org.shadowice.flocke.andotp.Utilities;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_NUMPAD_ENTER;

public class EditorActionHelper {

    private EditorActionHelper() { /* not allowed */ }

    public static boolean isActionDoneOrKeyboardEnter(int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == ACTION_DOWN && (event.getKeyCode() == KEYCODE_ENTER || event.getKeyCode() == KEYCODE_NUMPAD_ENTER);
    }

    public static boolean isActionUpKeyboardEnter(KeyEvent event) {
        return event.getAction() == ACTION_UP && (event.getKeyCode() == KEYCODE_ENTER || event.getKeyCode() == KEYCODE_NUMPAD_ENTER);
    }
}
