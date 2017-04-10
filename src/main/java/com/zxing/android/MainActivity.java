package com.zxing.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zxing.android.database.MyDataBaseHelper;

/**
 * Created by tiejiang on 17-4-10.
 */

public class MainActivity extends Activity {

    private Button mLoginButton;
    private MyDataBaseHelper mDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new MyDataBaseHelper(this, "attendance.db", null, 1);
        mDbHelper.getWritableDatabase();
        mLoginButton = (Button)findViewById(R.id.login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mLoginIntent = new Intent();
                mLoginIntent.setClass(MainActivity.this, CaptureActivity.class);
                startActivity(mLoginIntent);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
