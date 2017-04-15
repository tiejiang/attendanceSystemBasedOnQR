package com.zxing.android.sms;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by tiejiang on 17-4-15.
 */

public class SMS {

    private Context mContext;

    public SMS(Context context){
        this.mContext = context;
    }
    public void send(String number, String message){
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> texts = manager.divideMessage(message);
        for (String text : texts) {
            manager.sendTextMessage(number, null, text, null, null);
        }
        Toast.makeText(mContext, "send success", Toast.LENGTH_LONG).show();
    }
}
