package com.zyascend.MyAlarm.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zyascend.MyAlarm.Model.AlarmModel;
import com.zyascend.MyAlarm.MyAlarmReceiver;
import com.zyascend.MyAlarm.R;
import com.zyascend.MyAlarm.SnoozeReceiver;
import com.zyascend.MyAlarm.activity.PlayAlarmActivity;
import com.zyascend.MyAlarm.data.MyAlarmDataBase;

import java.util.Calendar;

/**
 *
 * Created by Administrator on 2016/3/27.
 */
public class NormalFragment extends Fragment{

    private static final long FIVE_MINUTE_TIME = 1000 * 60 * 5;
    private static final int SNOOZE_ALARM_ID = 100;
    private Button btn;
    private ImageButton imageButton;
    private TextView alarmTittle,hourText,minuteText;
    private PlayAlarmActivity activity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_normal,container,false);

        btn = (Button) view.findViewById(R.id.btn_stopAlarm_normal);
        alarmTittle = (TextView) view.findViewById(R.id.textView3);
        imageButton = (ImageButton) view.findViewById(R.id.imageButton);
        hourText = (TextView) view.findViewById(R.id.time_hour_text);
        minuteText = (TextView) view.findViewById(R.id.time_minute_text);
        activity = (PlayAlarmActivity) getActivity();

        MyAlarmDataBase db = new MyAlarmDataBase(getActivity());
        AlarmModel alarm = db.getAlarm(activity.getmId());

        Log.d("id " , String.valueOf(activity.getmId()));
        alarmTittle.setText(alarm.getTitle() + "时间到");

        Calendar calendar = Calendar.getInstance();
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String  minute = null;

        int showHour = calendar.get(Calendar.HOUR_OF_DAY);
        int showMinute = calendar.get(Calendar.MINUTE);

        if (showMinute < 10){
             minute = "0"+String.valueOf(showMinute);
        }else if (showHour < 10){
             hour= "0"+String.valueOf(showHour);
        }else {
            minute = String.valueOf(showMinute);
            hour = String.valueOf(showHour);
        }

        hourText.setText(hour);
        minuteText.setText(minute);

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getActivity().finish();
                return false;
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snooze();
            }
        });

        System.out.println("已StartNormalFragment");

        return view;
    }

    private void snooze() {
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Calendar c =Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), 0);
        long mTimeInfo = c.getTimeInMillis()+FIVE_MINUTE_TIME;

        Intent intent = new Intent(getActivity(), SnoozeReceiver.class);
        intent.putExtra(SnoozeReceiver.ID_SNOOZE_FLAG, Integer.toString(activity.getmId()));
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(),SNOOZE_ALARM_ID,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        manager.set(AlarmManager.RTC_WAKEUP, mTimeInfo, pi);
        Log.d("NormalFragment","已设置贪睡");
        activity.finish();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
