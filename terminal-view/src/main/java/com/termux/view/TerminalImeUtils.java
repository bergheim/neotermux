package com.termux.view;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

final class TerminalImeUtils {

    private TerminalImeUtils() {
    }

    static final class ExtractedTextSnapshot {
        final String text;
        final int startOffset;
        final int partialStartOffset;
        final int partialEndOffset;
        final int selectionStart;
        final int selectionEnd;

        ExtractedTextSnapshot(String text, int selectionStart, int selectionEnd) {
            this.text = text;
            this.startOffset = 0;
            this.partialStartOffset = -1;
            this.partialEndOffset = -1;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }
    }

    static final class ImeStateUpdateTracker {
        private int batchEditDepth;
        private boolean updatePending;

        void beginBatchEdit() {
            batchEditDepth++;
        }

        boolean isInBatchEdit() {
            return batchEditDepth > 0;
        }

        boolean endBatchEdit() {
            if (batchEditDepth == 0) return false;
            batchEditDepth--;
            if (batchEditDepth > 0 || !updatePending) return false;
            updatePending = false;
            return true;
        }

        boolean requestUpdate() {
            if (batchEditDepth == 0) return true;
            updatePending = true;
            return false;
        }

        void reset() {
            batchEditDepth = 0;
            updatePending = false;
        }
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

    static int getImeOptions() {
        return EditorInfo.IME_FLAG_NO_FULLSCREEN;
    }

    static ExtractedTextSnapshot getExtractedTextSnapshot(CharSequence text, int selectionStart,
                                                          int selectionEnd) {
        String snapshotText = text == null ? "" : text.toString();
        int fallbackSelection = snapshotText.length();
        return new ExtractedTextSnapshot(snapshotText,
            normalizeEditorIndex(selectionStart, snapshotText.length(), fallbackSelection),
            normalizeEditorIndex(selectionEnd, snapshotText.length(), fallbackSelection));
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

    private static int normalizeEditorIndex(int index, int textLength, int fallback) {
        if (index < 0) return fallback;
        return Math.min(index, textLength);
    }
}
