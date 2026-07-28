package com.termux.view;

import android.os.Build;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

final class TerminalImeUtils {

    private TerminalImeUtils() {
    }

    static int getInputType(boolean terminalSelected, boolean swipeTypingEnabled,
                            boolean enforceCharBasedInput) {
        if (!terminalSelected) {
            return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
        }

        if (swipeTypingEnabled) {
            return InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_NORMAL
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        }

        if (enforceCharBasedInput) {
            return InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        }

        return InputType.TYPE_NULL;
    }

    static int getImeOptions(boolean swipeTypingEnabled, int sdkInt) {
        int options = EditorInfo.IME_FLAG_NO_FULLSCREEN;
        if (swipeTypingEnabled && sdkInt >= Build.VERSION_CODES.O) {
            options |= EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING;
        }
        return options;
    }

    static int getTerminalBackspaceCount(int requestedLength, int bufferedLength,
                                         boolean swipeTypingEnabled) {
        if (!swipeTypingEnabled) return requestedLength;
        return Math.max(0, requestedLength - Math.max(0, bufferedLength));
    }

    static int getCodePointCountBeforeCursor(CharSequence text, int cursor) {
        if (text == null || cursor <= 0) return 0;
        int boundedCursor = Math.min(cursor, text.length());
        return Character.codePointCount(text, 0, boundedCursor);
    }
}
