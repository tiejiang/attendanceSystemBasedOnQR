package com.zxing.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tiejiang on 17-4-12.
 */

public class DatabaseCreate {

    private Context mContext;
    private MyDataBaseHelper mDbHelper;
    public static String dbName = "attendance.db";//数据库的名字
    public static String DATABASE_PATH = "/data/data/com.zxing.android/databases/";//数据库在手机里的路径


    public DatabaseCreate(Context context){
        this.mContext = context;
    }
    public void createDb(){
        // 检查 SQLite 数据库文件是否存在
        if ((new File(DATABASE_PATH + dbName)).exists() == false) {
            // 如 SQLite 数据库文件不存在，再检查一下 database 目录是否存在
            File f = new File(DATABASE_PATH);
            // 如 database 目录不存在，新建该目录
            if (!f.exists()) {
                f.mkdir();
            }

            try {
                // 得到 assets 目录下我们实现准备好的 SQLite 数据库作为输入流
                InputStream is = mContext.getAssets().open(dbName);
                // 输出流
                OutputStream os = new FileOutputStream(DATABASE_PATH + dbName);

                // 文件写入
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }

                // 关闭文件流
                os.flush();
                os.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        // 测试 /data/data/com.zxing.android/databases/ 下的数据库是否能正常工作
//        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH + dbName, null);
//        Cursor cursor = database.rawQuery("select * from test", null);
//        if (cursor.getCount() > 0) {
//            cursor.moveToFirst();
//            try {
//                // 解决中文乱码问题
//                byte test[] = cursor.getBlob(0);
//                String strtest = new String(test, "utf-8").trim();
//
//                // 看输出的信息是否正确
//                System.out.println(strtest);
//            } catch (UnsupportedEncodingException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        cursor.close();

//        mDbHelper = new MyDataBaseHelper(mContext, "attendance.db", null, 1);
//        mDbHelper.getWritableDatabase();
//        return mDbHelper;
    }



    /**
     * 判断数据库是否存在
     * @return false or true
     */
    public boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try{
            String databaseFilename = DATABASE_PATH+dbName;
            checkDB =SQLiteDatabase.openDatabase(databaseFilename, null,
                    SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){

        }
        if(checkDB!=null){
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }
    /**
     * 复制数据库到手机指定文件夹下
     * @throws IOException
     */
    public void copyDataBase() throws IOException{
        String databaseFilenames =DATABASE_PATH+dbName;
        File dir = new File(DATABASE_PATH);
        if(!dir.exists())//判断文件夹是否存在，不存在就新建一个
            dir.mkdir();
        FileOutputStream os = null;
        try{
            os = new FileOutputStream(databaseFilenames);//得到数据库文件的写入流
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
//        InputStream is = mContext.getResources().openRawResource(R.raw.test);//得到数据库文件的数据流
        InputStream is = mContext.getAssets().open(dbName);
        byte[] buffer = new byte[8192];
        int count = 0;
        try{

            while((count=is.read(buffer))>0){
                os.write(buffer, 0, count);
                os.flush();
            }
        }catch(IOException e){

        }
        try{
            is.close();
            os.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }





}
