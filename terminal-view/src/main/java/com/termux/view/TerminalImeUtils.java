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

    static int getTerminalDeleteCount(int requestedLength, int bufferedLength,
                                      boolean swipeTypingEnabled) {
        if (!swipeTypingEnabled) return requestedLength;
        return Math.max(0, requestedLength - Math.max(0, bufferedLength));
    }

    static int getBufferedCountBeforeDelete(CharSequence text, int selectionStart,
                                            int selectionEnd, int composingStart,
                                            int composingEnd, boolean codePoints) {
        int boundary = getDeleteBoundaries(text, selectionStart, selectionEnd,
            composingStart, composingEnd)[0];
        return getCount(text, 0, boundary, codePoints);
    }

    static int getBufferedCountAfterDelete(CharSequence text, int selectionStart,
                                           int selectionEnd, int composingStart,
                                           int composingEnd, boolean codePoints) {
        int boundary = getDeleteBoundaries(text, selectionStart, selectionEnd,
            composingStart, composingEnd)[1];
        return getCount(text, boundary, text == null ? 0 : text.length(), codePoints);
    }

    private static int[] getDeleteBoundaries(CharSequence text, int selectionStart,
                                             int selectionEnd, int composingStart,
                                             int composingEnd) {
        int textLength = text == null ? 0 : text.length();
        if (selectionStart < 0 || selectionEnd < 0) return new int[]{0, textLength};

        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        if (composingStart >= 0 && composingEnd >= 0) {
            start = Math.min(start, Math.min(composingStart, composingEnd));
            end = Math.max(end, Math.max(composingStart, composingEnd));
        }

        start = Math.max(0, Math.min(start, textLength));
        end = Math.max(start, Math.min(end, textLength));
        return new int[]{start, end};
    }

    private static int getCount(CharSequence text, int start, int end, boolean codePoints) {
        if (text == null || end <= start) return 0;
        return codePoints ? Character.codePointCount(text, start, end) : end - start;
    }
}
