package com.termux.view;

import android.os.Build;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.junit.Assert;
import org.junit.Test;

public class TerminalImeUtilsTest {

    @Test
    public void swipeTypingUsesAutocorrectableMultilineText() {
        int inputType = TerminalImeUtils.getInputType(true, true, false);

        Assert.assertEquals(InputType.TYPE_CLASS_TEXT, inputType & InputType.TYPE_MASK_CLASS);
        Assert.assertTrue((inputType & InputType.TYPE_TEXT_FLAG_AUTO_CORRECT) != 0);
        Assert.assertTrue((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0);
        Assert.assertEquals(0, inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    @Test
    public void rawTypingPreservesExistingInputModes() {
        Assert.assertEquals(InputType.TYPE_NULL,
            TerminalImeUtils.getInputType(true, false, false));
        Assert.assertEquals(
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            TerminalImeUtils.getInputType(true, false, true));
        Assert.assertEquals(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL,
            TerminalImeUtils.getInputType(false, false, true));
    }

    @Test
    public void swipeTypingRequestsNoPersonalizedLearningWhenSupported() {
        int options = TerminalImeUtils.getImeOptions(true, Build.VERSION_CODES.O);

        Assert.assertTrue((options & EditorInfo.IME_FLAG_NO_FULLSCREEN) != 0);
        Assert.assertTrue((options & EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0);
        Assert.assertEquals(EditorInfo.IME_FLAG_NO_FULLSCREEN,
            TerminalImeUtils.getImeOptions(true, Build.VERSION_CODES.O - 1));
    }

    @Test
    public void compositionDeletesBufferedTextBeforeTerminalText() {
        Assert.assertEquals(0, TerminalImeUtils.getTerminalDeleteCount(1, 5, true));
        Assert.assertEquals(2, TerminalImeUtils.getTerminalDeleteCount(4, 2, true));
        Assert.assertEquals(4, TerminalImeUtils.getTerminalDeleteCount(4, 2, false));
    }

    @Test
    public void composingSpanIsExcludedFromSurroundingDeleteCounts() {
        Assert.assertEquals(0, TerminalImeUtils.getBufferedCountBeforeDelete(
            "hello", 5, 5, 0, 5, false));
        Assert.assertEquals(0, TerminalImeUtils.getBufferedCountAfterDelete(
            "hello", 5, 5, 0, 5, false));
        Assert.assertEquals(1, TerminalImeUtils.getBufferedCountBeforeDelete(
            "ahellob", 6, 6, 1, 6, false));
        Assert.assertEquals(1, TerminalImeUtils.getBufferedCountAfterDelete(
            "ahellob", 6, 6, 1, 6, false));
    }

    @Test
    public void bufferedDeleteCountsSupportCodePointsAndReversedSpans() {
        String text = "a\uD83D\uDE00b";

        Assert.assertEquals(2, TerminalImeUtils.getBufferedCountBeforeDelete(
            text, 3, 3, -1, -1, true));
        Assert.assertEquals(1, TerminalImeUtils.getBufferedCountAfterDelete(
            text, 3, 3, -1, -1, true));
        Assert.assertEquals(1, TerminalImeUtils.getBufferedCountBeforeDelete(
            text, 3, 3, 3, 1, true));
        Assert.assertEquals(1, TerminalImeUtils.getBufferedCountAfterDelete(
            text, 3, 3, 3, 1, true));
    }
}
