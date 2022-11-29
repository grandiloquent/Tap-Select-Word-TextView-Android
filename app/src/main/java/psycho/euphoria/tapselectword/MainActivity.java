package psycho.euphoria.tapselectword;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.BreakIterator;

public class MainActivity extends Activity {
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setPadding(30, 30, 30, 30);
        textView.setTextSize(16);
        textView.setText("This is an example of how to select a word on a tap in Android's TextView.");
        setContentView(textView);
        mTextView = textView;
        final WordIterator wordIterator = new WordIterator();
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        final float x = event.getX();
                        final float y = event.getY();
                        if (x < 132 || x > 1080 - 132) {//
                            return true;
                        }
                        // Remember finger down position, to be able to start selection from there
                        int t = mTextView.getOffsetForPosition(x, y);
                        long lastTouchOffsets = packRangeInLong(t, t);
                        final int minOffset = unpackRangeStartFromLong(lastTouchOffsets);
                        final int maxOffset = unpackRangeEndFromLong(lastTouchOffsets);
                        wordIterator.setCharSequence(mTextView.getText().toString(), minOffset, maxOffset);
                        int selectionStart, selectionEnd;
                        selectionStart = wordIterator.getBeginning(minOffset);
                        selectionEnd = wordIterator.getEnd(maxOffset);
                        if (selectionStart == BreakIterator.DONE || selectionEnd == BreakIterator.DONE ||
                                selectionStart == selectionEnd) {
                            // Possible when the word iterator does not properly handle the text's language
                            long range = getCharRange(minOffset);
                            selectionStart = unpackRangeStartFromLong(range);
                            selectionEnd = unpackRangeEndFromLong(range);
                        }
                        try {
                            String s = mTextView.getText().subSequence(selectionStart, selectionEnd).toString().trim();
                            if (s.length() > 0)
                                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //Log.e("B5aOx2", String.format("onTouch, %s",  mTextView.getText().subSequence(selectionStart,selectionEnd)));
                }
//                    mOffset = mTextView.getOffsetForPosition(motionEvent.getX(), motionEvent.getY());
//                    String s = findWordForRightHanded(mTextView.getText().toString(), mOffset);
//                    if (s.length() > 0) {
//                        Matcher matcher = mPattern.matcher(s);
//                        if (matcher.find()) {
//                            translate(matcher.group());
//                        }
//                    }
                return false;
            }
        });
    }

    public static long packRangeInLong(int start, int end) {
        return (((long) start) << 32) | end;
    }

    public static int unpackRangeEndFromLong(long range) {
        return (int) (range & 0x00000000FFFFFFFFL);
    }

    public static int unpackRangeStartFromLong(long range) {
        return (int) (range >>> 32);
    }

    private String findWordForRightHanded(String str, int offset) { // when you touch ' ', this method returns left word.
        if (str.length() == offset) {
            offset--; // without this code, you will get exception when touching end of the text
        }
        if (str.charAt(offset) == ' ') {
            offset--;
        }
        int startIndex = offset;
        int endIndex = offset;
        try {
            while (str.charAt(startIndex) != ' ' && str.charAt(startIndex) != '\n') {
                startIndex--;
            }
        } catch (StringIndexOutOfBoundsException e) {
            startIndex = 0;
        }
        try {
            while (str.charAt(endIndex) != ' ' && str.charAt(endIndex) != '\n') {
                endIndex++;
            }
        } catch (StringIndexOutOfBoundsException e) {
            endIndex = str.length();
        }
        // without this code, you will get 'here!' instead of 'here'
        // if you use only english, just check whether this is alphabet,
        // but 'I' use korean, so i use below algorithm to get clean word.
        char last = str.charAt(endIndex - 1);
        if (last == ',' || last == '.' ||
                last == '!' || last == '?' ||
                last == ':' || last == ';') {
            endIndex--;
        }
        return str.substring(startIndex, endIndex);
    }

    private long getCharRange(int offset) {
        final int textLength = mTextView.getText().length();
        if (offset + 1 < textLength) {
            final char currentChar = mTextView.getText().charAt(offset);
            final char nextChar = mTextView.getText().charAt(offset + 1);
            if (Character.isSurrogatePair(currentChar, nextChar)) {
                return packRangeInLong(offset, offset + 2);
            }
        }
        if (offset < textLength) {
            return packRangeInLong(offset, offset + 1);
        }
        if (offset - 2 >= 0) {
            final char previousChar = mTextView.getText().charAt(offset - 1);
            final char previousPreviousChar = mTextView.getText().charAt(offset - 2);
            if (Character.isSurrogatePair(previousPreviousChar, previousChar)) {
                return packRangeInLong(offset - 2, offset);
            }
        }
        if (offset - 1 >= 0) {
            return packRangeInLong(offset - 1, offset);
        }
        return packRangeInLong(offset, offset);
    }


}