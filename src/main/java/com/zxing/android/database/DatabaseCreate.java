package com.zxing.android.database;

import android.content.Context;

/**
 * Created by tiejiang on 17-4-12.
 */

public class DatabaseCreate {

    private MyDataBaseHelper mDbHelper;

    public MyDataBaseHelper createDb(Context context){
        mDbHelper = new MyDataBaseHelper(context, "attendance.db", null, 1);
        mDbHelper.getWritableDatabase();

        return mDbHelper;
    }

    public void getDb(){


    }
}
