package com.zyascend.MyAlarm.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Alarm数据库
 * Created by Administrator on 2016/3/4.
 */
public class MyAlarmOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = " AlarmDatabase";
    private static final String TABLE_ALARM = " AlarmTable";
    private static final String TABLE_QUESTION = " AlarmQuestion";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TIME = "time";
    private static final String KEY_REPEAT_TYPE = "repeat_type";
    private static final String KEY_REPEAT_CODE = "repeat_code";
    private static final String KEY_WAKE_TYPE = "normal";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_QUESTION_ID = "que_id";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_ANSWER = "answer" ;
    private static final String KEY_ALARM_ID = "alarm_id";
    private static final String KEY_RING = "ring";

    String CREATE_ALARM_TABLE = "CREATE TABLE" + TABLE_ALARM +

            "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TITLE + " TEXT,"
            + KEY_TIME + " INTEGER,"
            + KEY_WAKE_TYPE + " TEXT,"
            + KEY_REPEAT_TYPE + " TEXT,"
            + KEY_REPEAT_CODE + " TEXT,"
            + KEY_RING + " TEXT,"
            + KEY_ACTIVE + " BOOLEAN" + ")";

    String CREATE_QUESTION_TABLE = "CREATE TABLE" + TABLE_QUESTION+
            "("
            + KEY_QUESTION_ID + " INTEGER PRIMARY KEY,"
            + KEY_QUESTION + " TEXT,"
            + KEY_ANSWER + " TEXT,"
            + KEY_ALARM_ID + " INTEGER"+")";

    public MyAlarmOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ALARM_TABLE);
        db.execSQL(CREATE_QUESTION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion >= newVersion){
            return;
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTION);
        onCreate(db);
    }
}
