package com.ndeso17.nimesnolimit;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("NimeNoLimit\n\nUnlocks premium features, removes ads, and " +
                "disables limits inside the target app.\n\nEnable this module in " +
                "LSPosed, select the target app in its scope, then restart the app.");
        tv.setPadding(48, 48, 48, 48);
        tv.setGravity(Gravity.CENTER);
        setContentView(tv);
    }
}
