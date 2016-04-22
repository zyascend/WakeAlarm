package com.zyascend.MyAlarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zyascend.MyAlarm.Model.AlarmModel;
import com.zyascend.MyAlarm.data.MyAlarmDataBase;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * Created by Administrator on 2016/3/17.
 */
public class AlarmService extends Service{

    private static final int ONE_DAY_TIME = 1000*60*60*24;
    private static final int CONFIG_ONE = 0;
    private static final int CONFIG_TWO = 1;
    private static AlarmManager alarmManager;

    private  MyBinder myBinder = new MyBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate() {
        Log.d("Service","启动服务");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Intent service = new Intent(this,AlarmService.class);
        this.startService(service);
        Log.d("Service","重启服务");
        super.onDestroy();
    }

    public static class MyBinder extends Binder {
        private String[] mTimeSplit;
        private int mHour;
        private int mMinute;


        public void setSingleAlarm(
                Context context, String time, int id) {

            mTimeSplit = time.split(":");
            mHour = Integer.parseInt(mTimeSplit[0]);
            mMinute = Integer.parseInt(mTimeSplit[1]);

            Calendar c = Calendar.getInstance();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH), mHour, mMinute, 0);

            long mTimeInfo = c.getTimeInMillis();
            long actualTime = mTimeInfo > System.currentTimeMillis()
                    ? mTimeInfo : mTimeInfo + ONE_DAY_TIME;

            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int flagDayOfWeek = 0;
            alarmManager.set(AlarmManager.RTC_WAKEUP, actualTime, getIntent(CONFIG_ONE, context, id, flagDayOfWeek));

            Log.d("Service", "单次闹钟设置完成");
        }

        public void setEverydayAlarm(Context context, String time, int id) {
            mTimeSplit = time.split(":");
            mHour = Integer.parseInt(mTimeSplit[0]);
            mMinute = Integer.parseInt(mTimeSplit[1]);

            Calendar c = Calendar.getInstance();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH), mHour, mMinute, 0);

            long mTimeInfo = c.getTimeInMillis();
            long actualTime = mTimeInfo > System.currentTimeMillis()
                    ? mTimeInfo : mTimeInfo + ONE_DAY_TIME;


            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int flagDayOfWeek = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, actualTime,
                        getIntent(CONFIG_ONE, context, id, flagDayOfWeek));
                Log.d("Service", "每日闹钟，调用了setExact()");

            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, actualTime,
                        getIntent(CONFIG_ONE, context, id, flagDayOfWeek));
                Log.d("Service", "每日闹钟，调用了set()");
            }

                Log.d("Service","每日闹钟设置完成");

        }

        public void setDiyAlarm(Context context, String repeat, String time,
                                int id, String repeatCode) {


            List<Integer> dayOfWeekList = loadDayOfWeek(repeat, repeatCode);

            mTimeSplit = time.split(":");
            mHour = Integer.parseInt(mTimeSplit[0]);
            mMinute = Integer.parseInt(mTimeSplit[1]);

            Calendar c = Calendar.getInstance();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH), mHour, mMinute, 0);

            long mTimeInfo = c.getTimeInMillis();
            long actualTime = mTimeInfo > System.currentTimeMillis()
                    ? mTimeInfo : mTimeInfo + ONE_DAY_TIME;


            int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);    //今天是星期几
            Log.d("Service", "今天是" + currentDayOfWeek);
            for (int j = 0; j < dayOfWeekList.size(); j++) {
                int flagDayOfWeek = dayOfWeekList.get(j);

                Log.d("Service", "第"+(j+1)+"次循环，这次的目标是： "+flagDayOfWeek);

                if (currentDayOfWeek == flagDayOfWeek) {

                    setAlarm(actualTime, context, id, flagDayOfWeek);

                    Log.d("Service","已设置，目标与今天相同,是" + flagDayOfWeek);

                } else if (currentDayOfWeek < flagDayOfWeek) {
                    int gapDay = flagDayOfWeek - currentDayOfWeek;
                    long realTime = actualTime + gapDay * ONE_DAY_TIME;

                    setAlarm(realTime, context, id, flagDayOfWeek);

                    Log.d("Service", "已设置，目标小于今天,是" + flagDayOfWeek);


                } else if (currentDayOfWeek > flagDayOfWeek) {
                    int gapDay = 7 - currentDayOfWeek + flagDayOfWeek;
                    long realTime = gapDay * ONE_DAY_TIME + actualTime;

                    setAlarm(realTime, context, id, flagDayOfWeek);

                    Log.d("Service", "已设置，目标大于今天,是" + flagDayOfWeek);
                }
            }
            dayOfWeekList.clear();

            Log.d("Service","自定义闹钟设置完成！");
        }

        @NonNull
        private List<Integer> loadDayOfWeek(String repeat, String repeatCode) {
            List<Integer> dayOfWeekList = new ArrayList<>();
            if (repeat.equals("周一至周五")) {
                for (int i = 2; i < 7; i++) {
                    dayOfWeekList.add(i);
                }

            } else if (repeat.equals("周六周日")) {
                dayOfWeekList.add(1);
                dayOfWeekList.add(7);

            } else {
                if (repeatCode!=null){
                    String[] splitCode = repeatCode.split(",");
                    for (int i = 0; i < splitCode.length; i++) {
                        dayOfWeekList.add(Integer.parseInt(splitCode[i]));
                        Log.d("Service", "获取到自定义dayOfWeek");
                    }

                }

            }
            return dayOfWeekList;
        }

        private void setAlarm(long realTime, Context context, int id, int flagDayOfWeek) {

            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, realTime,
                        getIntent(CONFIG_TWO, context, id, flagDayOfWeek));

            } else {

                alarmManager.set(AlarmManager.RTC_WAKEUP, realTime,
                        getIntent(CONFIG_TWO, context, id + flagDayOfWeek, flagDayOfWeek));
            }

        }

        public void cancelAlarm(AlarmModel alarm,int id,Context context){

            if (alarm==null){
                Log.d("alarm null","null");
            }

            Log.d("id ", String.valueOf(id));

            String repeat = alarm.getRepeatType();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            if (repeat.equals("每天")||repeat.equals("只响一次")){
                PendingIntent pi = PendingIntent.getBroadcast(context, id,
                        new Intent(context, MyAlarmReceiver.class), 0);

                alarmManager.cancel(pi);
            }else {
                List<Integer> flagDayOfWeek = loadDayOfWeek(repeat,alarm.getRepeatCode());

                for (int i = 0;i<flagDayOfWeek.size();i++){
                    PendingIntent pi = getIntent(CONFIG_TWO,context,id,flagDayOfWeek.get(i));
                    alarmManager.cancel(pi);
                    Log.d("Service","取消id是"+id+" 星期"+flagDayOfWeek+"的闹钟");
                }
            }

        }
        private PendingIntent getIntent(int config, Context context, int id, int flagDayOfWeek) {

            Intent intent = new Intent(context, MyAlarmReceiver.class);
            intent.putExtra(MyAlarmReceiver.ID_FLAG, Integer.toString(id));

            switch (config) {
                case CONFIG_ONE:

                    return PendingIntent.getBroadcast(context,
                            id+flagDayOfWeek, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                case CONFIG_TWO:
                    return PendingIntent.getBroadcast(context,
                            id+flagDayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            }
            return null;
        }
    }



    public static class RebootReceiver extends BroadcastReceiver {

        private String mTime,mRepeat,mActive,mRepeatCode;
        private int mID;
        private AlarmService.MyBinder binder = new MyBinder();
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){

                Log.d("RebootReceiver","接收到开机广播");

                MyAlarmDataBase db = new MyAlarmDataBase(context);
                List<AlarmModel> alarm = db.getAllAlarms();

                for (AlarmModel am : alarm) {
                    mTime = am.getTime();
                    mRepeat = am.getRepeatType();
                    mID = am.getID();
                    mActive = am.getActive();
                    if (mRepeat.equals("自定义")){
                        mRepeatCode = am.getRepeatCode();
                    }

                    if (mActive.equals("true")){
                        switch (mRepeat) {
                            case "只响一次":
                                binder.setSingleAlarm(context, mTime, mID);
                                break;
                            case "每天":
                                binder.setEverydayAlarm(context, mTime, mID);
                                break;
                            default:
                                binder.setDiyAlarm(context, mRepeat, mTime, mID,mRepeatCode);
                        }

                        Log.v("RebootReceiver","完成重启后闹钟设置");

                    }
                }
            }
        }


    }
}
