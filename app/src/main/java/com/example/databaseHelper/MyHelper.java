package com.example.databaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyHelper extends SQLiteOpenHelper {


    public static String CREATE_TABLE = "create table "+ Database.TABLE_NAME +"(" +
            Database.ID + " Integer primary key autoincrement, " +
            Database.SURI + " varchar(30), " +
            Database.PURI + " varchar(30), " +
            Database.TIME + " timestamp not null default (datetime('now','localtime')), " +
            Database.TEXT + " varchar(256))";    // 用于创建表的SQL语句

    private static final String TAG = "UseDatabase";

    private Context myContext = null;


    public MyHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
    }

    public MyHelper(Context context)
    {
        super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
        myContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "创建数据库");
        Toast.makeText(myContext, "创建数据库", Toast.LENGTH_SHORT).show();
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + Database.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }



}
