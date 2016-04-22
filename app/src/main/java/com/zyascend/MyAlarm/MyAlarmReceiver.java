package com.zyascend.MyAlarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.zyascend.MyAlarm.Model.AlarmModel;
import com.zyascend.MyAlarm.activity.PlayAlarmActivity;
import com.zyascend.MyAlarm.data.MyAlarmDataBase;

import java.util.Calendar;


/**
 *
 * Created by Administrator on 2016/3/11.
 */
public class MyAlarmReceiver extends BroadcastReceiver {

    public static final String ID_FLAG = "flag";
    private static final int ONE_DAY_TIME = 1000*60*60*24;
    private static final int ONE_WEEK_TIME = 1000*60*60*24*7;
    private String mRepeatType;
    private AlarmManager manager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context,PlayAlarmActivity.class);
        i.putExtra(PlayAlarmActivity.ALARM_ID, intent.getStringExtra(ID_FLAG));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

        int alarmId = Integer.parseInt(intent.getStringExtra(ID_FLAG));
        MyAlarmDataBase db = new MyAlarmDataBase(context);
        AlarmModel model = db.getAlarm(alarmId);
        mRepeatType = model.getRepeatType();

        switch (mRepeatType){
            case "每天":
                setRepeatAlarm(context, alarmId,ONE_DAY_TIME);
                break;
            default:
                setRepeatAlarm(context,alarmId,ONE_WEEK_TIME);
            break;
        }

        Log.d("AlarmReceiver","接收到闹钟广播");

    }

    private void setRepeatAlarm(Context mContext,int id,int gapTime) {

        Calendar c =Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), 0);
        long mTimeInfo = c.getTimeInMillis()+gapTime;
        int currentWeekOfDay = c.get(Calendar.DAY_OF_WEEK);

        Intent i = new Intent(mContext,MyAlarmReceiver.class);
        i.putExtra(MyAlarmReceiver.ID_FLAG, Integer.toString(id));
        PendingIntent pi = PendingIntent.getBroadcast(mContext,id+currentWeekOfDay,i,PendingIntent.FLAG_UPDATE_CURRENT);

        manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP,mTimeInfo,pi);
        }else {
            manager.set(AlarmManager.RTC_WAKEUP,mTimeInfo,pi);
        }

        Log.d("AlarmReceiver","已再次设置对应闹钟(重复)");

    }
}
