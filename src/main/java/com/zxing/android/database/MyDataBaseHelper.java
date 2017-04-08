import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 *@author: tiejiang
 * data: 2017048
 */

public class MyDataBaseHelper extends SQLiteOpenHelper{

    public static final String CLASS_10 = "creat table class_10 ("
            + "id integer primary key autoincrement, "
            + "start_year integer, "
            + "college integer, "
            + "class integer, "
            + "student_id integer, "
            + "student_name text) ";

    public static final String CLASS_11 = "creat table class_11 ("
            + "id integer primary key autoincrement, "
            + "start_year integer, "
            + "college integer, "
            + "class integer, "
            + "student_id integer, "
            + "student_name text) ";

    public MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CLASS_10);
        sqLiteDatabase.execSQL(CLASS_11);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}