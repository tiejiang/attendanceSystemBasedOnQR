package com.zxing.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zxing.android.database.DatabaseCreate;

/**
 * Created by tiejiang on 17-4-10.
 */

public class MainActivity extends Activity {

    private Button mLoginButton;
    private EditText collegeID, classID;
    private String stuCollege = "";
    private String stuClass = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //check and copy database to the dir
        new DatabaseCreate(this).createDb();

        collegeID = (EditText)findViewById(R.id.college_id);
        classID = (EditText)findViewById(R.id.class_id);
        mLoginButton = (Button)findViewById(R.id.login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stuCollege = collegeID.getText().toString();
                stuClass = classID.getText().toString();
                Log.d("TIEJIANG", "stuCollege= " + stuCollege + ", stuClass= " + stuClass);
                if (!stuCollege.equals("") && !stuClass.equals("")){
                    Intent mLoginIntent = new Intent();
                    mLoginIntent.putExtra("college", stuCollege);
                    mLoginIntent.putExtra("class", stuClass);
                    mLoginIntent.setClass(MainActivity.this, CaptureActivity.class);
                    startActivity(mLoginIntent);
                }else {
                    Toast.makeText(MainActivity.this, "请先输入学院和班级", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
