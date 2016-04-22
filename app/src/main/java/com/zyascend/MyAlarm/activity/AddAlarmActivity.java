package com.zyascend.MyAlarm.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zyascend.MyAlarm.Model.AlarmModel;
import com.zyascend.MyAlarm.AlarmService;
import com.zyascend.MyAlarm.data.MyAlarmDataBase;
import com.zyascend.MyAlarm.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 *
 * Created by zyascend.
 */
public class AddAlarmActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener {

    private static final String KEY_RING = "key_ring";
    private Toolbar mToolbar;
    private EditText mTitleText;
    private TextView mTimeText, mRepeatText,mWakeText,mRingText;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private Calendar mCalendar;
    private int  mHour, mMinute,ID;
    private String mTitle;
    private String mTime;
    private String mRepeatType,mRepeatCode;
    private String mActive,mWake,mRing;
    private AlarmService.MyBinder binder;
    private ServiceConnection connection = null ;
    private MediaPlayer player ;
    private MyAlarmDataBase db ;



    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_REPEAT = "repeat_key";
    private static final String KEY_WAKE= "wake_key";
    private static final String KEY_ACTIVE = "active_key";
    private String finalDefine;
    private List<Integer> repeatCode = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        //初始化View
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleText = (EditText) findViewById(R.id.alarm_title);
        mTimeText = (TextView) findViewById(R.id.set_time);
        mRepeatText = (TextView) findViewById(R.id.set_repeat);
        mWakeText = (TextView) findViewById(R.id.set_wake);
        mRingText = (TextView) findViewById(R.id.set_ring);

        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);

        //配置ToolBar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.title_activity_add_alarm);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //设置默认值
        mCalendar = Calendar.getInstance();
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);
        mTime = mHour + ":" + mMinute;

        mActive = "true";
        mRepeatType = "每天";
        mRepeatCode = String.valueOf(new Random().nextInt(100)+1);
        mWake = "常规";
        mRing = "响铃";

        db = new MyAlarmDataBase(this);
        ID = db.addAlarm(new AlarmModel(mTitle,mTime,mWake,mRepeatType,mRepeatCode,mRing,mActive));


        mTitleText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTitle = s.toString().trim();
                mTitleText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTimeText .setText(mTime);
        mWakeText.setText(mWake);
        mRingText.setText(mRing);
        mRepeatText.setText(mRepeatType);

        // 得到上次设置状态
        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            mTitleText.setText(savedTitle);
            mTitle = savedTitle;

            String savedTime = savedInstanceState.getString(KEY_TIME);
            mTimeText.setText(savedTime);
            mTime = savedTime;

            String savedRepeat= savedInstanceState.getString(KEY_REPEAT);
            mRepeatText.setText(savedRepeat);
            mRepeatType = savedRepeat;

            String savedRing = savedInstanceState.getString(KEY_RING);
            mRingText.setText(savedRing);
            mRing = savedRing;

            String savedWake = savedInstanceState.getString(KEY_WAKE);
            mWakeText.setText(savedWake);
            mWake = savedWake;

            mActive = savedInstanceState.getString(KEY_ACTIVE);
        }

        // 设置可用按钮
        if (mActive.equals("false")) {
            mFAB1.setVisibility(View.VISIBLE);
            mFAB2.setVisibility(View.GONE);

        } else if (mActive.equals("true")) {
            mFAB1.setVisibility(View.GONE);
            mFAB2.setVisibility(View.VISIBLE);
        }

    }

    //保存当前设置状态
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, mTitleText.getText());
        outState.putCharSequence(KEY_TIME, mTimeText.getText());
        outState.putCharSequence(KEY_REPEAT, mRepeatText.getText());
        outState.putCharSequence(KEY_RING, mRingText.getText());
        outState.putCharSequence(KEY_WAKE, mWakeText.getText());
        outState.putCharSequence(KEY_ACTIVE, mActive);

    }

    public void selectFab1(View v){
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mActive = "true";
    }

    public void selectFab2(View v){
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mActive = "false";
    }


    public void selectTime(View v){
        Calendar now = Calendar.getInstance();
        TimePickerDialog timeDialog = TimePickerDialog.newInstance(
                this, now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),false);
        timeDialog.setThemeDark(false);
        timeDialog.show(getFragmentManager(), "选择时间");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        if (minute < 10) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        mTimeText.setText(mTime);
    }

    public void selectRepeat(View v){

        final String[] items = {"只响一次","每天","周一至周五","周六周日","自定义"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_view_day_grey600_24dp);
        builder.setTitle("选择重复");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String repeatType = items[which];
                if (which == 4) {
                    showDefineDialog(dialog);
                } else {
                    mRepeatType = repeatType;
                    mRepeatText.setText(mRepeatType);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectWake(View v){
        final String[] items = {"常规","回答问题"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("停闹方式");
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mWake = items[which];
                mWakeText.setText(mWake);

                if (which == 1) {
                    Intent i = new Intent(AddAlarmActivity.this, QuestionActivity.class);
                    i.putExtra(EditAlarmActivity.ALARM_ID, Integer.toString(ID));
                    startActivity(i);
                }

                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectRing(View v){
        final String[] options = new String[]{"震动","响铃","震动并响铃"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择方式");
        builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRing = options[which];
                mRingText.setText(mRing);
                if (which == 1 || which == 2) {
                    showRingDialog(dialog);
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRingDialog(DialogInterface lastDialog) {

        lastDialog.dismiss();

        String[] ringList = new String[]{"Morning","卡农","空灵","天籁森林","唯美","温暖早晨"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(ringList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences("ringCode", MODE_PRIVATE).edit();
                editor.putInt("key_ring", which + 1);
                editor.apply();
                playRing(which + 1);
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences("ringCode", MODE_PRIVATE).edit();
                editor.putInt("key_ring", which + 1);
                editor.apply();
                if (player.isPlaying()){
                    player.stop();
                    player.release();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playRing(int i) {
        switch (i){
            case 1:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring01);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring01);

                }
                break;
            case 2:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring02);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring02);

                }
                break;
            case 3:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring03);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring03);

                }
                break;
            case 4:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring04);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring04);

                }
                break;
            case 5:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring05);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring05);

                }
                break;
            case 6:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring06);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring06);

                }
                break;

        }
        player.setLooping(true);
        player.start();


    }

    private void showDefineDialog(DialogInterface lastDialog) {

        lastDialog.dismiss();

        final String[] myDefine = {"周一","周二","周三","周四","周五","周六","周日"};
        final List<String> choosedDefine = new ArrayList<>();
        finalDefine="";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自定义");
        builder.setMultiChoiceItems(myDefine, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if (isChecked){
                    choosedDefine.add(myDefine[which]);
                    repeatCode.add(which+2);

                    StringBuilder sb = new StringBuilder();
                    if (repeatCode != null && repeatCode.size() > 0) {
                        for (int i = 0; i < repeatCode.size(); i++) {
                            if (i < repeatCode.size() - 1) {
                                sb.append(repeatCode.get(i) + ",");
                            } else {
                                sb.append(repeatCode.get(i));
                            }
                        }
                    }
                    mRepeatCode = sb.toString();
                }
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0;i<choosedDefine.size();i++){
                    finalDefine = finalDefine+" "+choosedDefine.get(i);
                    mRepeatType=finalDefine;
                    mRepeatText.setText(mRepeatType);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.save_Alarm:
                mTitleText.setText(mTitle);
                if (mTitleText.getText().toString().length() == 0)
                    mTitleText.setError("闹钟名不能为空");
                else {
                    saveAlarm();
                    finish();
                }
                return true;


            case R.id.discard_alarm:
                Toast.makeText(getApplicationContext(), "取消设置",
                        Toast.LENGTH_SHORT).show();

                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveAlarm() {

        AlarmModel model = db.getAlarm(ID);
        model.setTitle(mTitle);
        model.setTime(mTime);
        model.setRepeatType(mRepeatType);
        model.setRepeatCode(mRepeatCode);
        model.setWakeType(mWake);
        model.setActive(mActive);
        model.setRing(mRing);
        db.updateAlarm(model);

        if (mActive.equals("true")) {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binder = (AlarmService.MyBinder) service;
                    switch (mRepeatType) {
                        case "只响一次":
                            binder.setSingleAlarm(getApplicationContext(), mTime,ID);
                            break;
                        case "每天":
                            binder.setEverydayAlarm(getApplicationContext(), mTime,ID);
                            break;
                        default:
                            binder.setDiyAlarm(getApplicationContext(), mRepeatType, mTime,ID, mRepeatCode);
                            repeatCode.clear();
                            break;
                    }

                    Log.d("AddActivity","绑定服务");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }

        Intent intent = new Intent(this,AlarmService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);

        Intent intent2 = new Intent();
        intent2.setClass(AddAlarmActivity.this, MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
    }

    @Override
    public void onBackPressed() {
        AlarmModel am = db.getAlarm(ID);
        db.deleteAlarm(am);
        db.deleteQuestion(am);
        Log.d("AddActivity", "用户取消创建Alarm");

        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        if (connection!=null) {
            unbindService(connection);
        }
        if (player!=null){
            player.release();
        }
        super.onDestroy();
    }
}
