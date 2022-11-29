package psycho.euphoria.tapselectword;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("This is an example of how to select a word on a tap in Android's TextView.");
        setContentView(textView);
    }
}