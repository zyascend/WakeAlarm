package com.zyascend.MyAlarm.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.zyascend.MyAlarm.AlarmService;
import com.zyascend.MyAlarm.Model.AlarmModel;
import com.zyascend.MyAlarm.Utils.ActivityManager;
import com.zyascend.MyAlarm.Utils.DividerItemDecoration;
import com.zyascend.MyAlarm.data.MyAlarmDataBase;
import com.zyascend.MyAlarm.Utils.MyTimeSorter;
import com.zyascend.MyAlarm.R;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
/**
 *
 * Created by zyascend on 2016/3/4.
 */
public class MainActivity extends AppCompatActivity {

    private MyAlarmDataBase db;
    private Toolbar mToolBar;
    private FloatingActionButton mAddAlarmBtn;
    private RecyclerView mRecyclerView;
    private TextView mNoAlarmTextView;
    private MyReAdapter adapter;
    private LinkedHashMap<Integer, Integer> IDmap = new LinkedHashMap<>();
    private AlarmService.MyBinder binder;
    private ServiceConnection connection = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityManager.addActivity(this);

        db = new MyAlarmDataBase(getApplicationContext());

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mAddAlarmBtn = (FloatingActionButton) findViewById(R.id.add_reminder);
        mRecyclerView = (RecyclerView) findViewById(R.id.alarm_list);
        mNoAlarmTextView = (TextView) findViewById(R.id.no_alarm_text);

        List<AlarmModel> mAlarmList = db.getAllAlarms();

        if (mAlarmList.isEmpty()) {
            mNoAlarmTextView.setVisibility(View.VISIBLE);
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        adapter = new MyReAdapter();
        adapter.setItemCount();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));

        setSupportActionBar(mToolBar);
        mToolBar.setTitle(R.string.app_name);

        mAddAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddAlarmActivity.class);
                startActivity(intent);

            }
        });

    }

    protected int getDefaultItemCount() {

        return 100;
    }

    public void onResume() {
        super.onResume();
        List<AlarmModel> list = db.getAllAlarms();

        if (list.isEmpty()) {
            mNoAlarmTextView.setVisibility(View.VISIBLE);
        } else {
            mNoAlarmTextView.setVisibility(View.GONE);
        }
        adapter.setItemCount();

    }

    public void cancelAlarm(final Context context, final int id, final AlarmModel alarm){

        connection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (AlarmService.MyBinder)service;
                binder.cancelAlarm(alarm,id, context);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("MainActivity","解绑服务");
            }
        };
        Intent intent = new Intent(this,AlarmService.class);

        bindService(intent, connection, BIND_AUTO_CREATE);

        Log.d("MainActivity", "取消闹钟");

        unbindService(connection);

    }


    class MyReAdapter extends RecyclerView.Adapter<MyReAdapter.MyViewHolder> {

        private ArrayList<AlarmItem> mItems;

        public MyReAdapter() {
            mItems = new ArrayList<>();
        }

        public long getItemId(int position) {

            return position;
        }

        public void setItemCount() {
            mItems.clear();
            mItems.addAll(loadData());
            notifyDataSetChanged();
        }


        public void onDeleteItem() {
            mItems.clear();
            mItems.addAll(loadData());
        }

        public void removeItemSelected(int selected) {
            if (mItems.isEmpty()) {
                return;
            }
            mItems.remove(selected);
            notifyItemRemoved(selected);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.items_layout, parent, false);

            return new MyViewHolder(view);
        }


        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            AlarmItem item = mItems.get(position);
            holder.setAlarmTitle(item.mTitle);
            holder.setAlarmTime(item.mTime);
            holder.setRepeatType(item.mRepeatType);
            holder.setActiveImage(item.mActive);
            holder.getItemPosition(position);
            holder.mOverFlowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    View view = mRecyclerView.getChildAt(position);
                    int po = mRecyclerView.getChildAdapterPosition(view);
                    int id = IDmap.get(po);

                    AlarmModel alarm = db.getAlarm(id);
                    db.deleteAlarm(alarm);
                    db.deleteQuestion(alarm);

                    adapter.removeItemSelected(po);
                    adapter.onDeleteItem();
                    cancelAlarm(MainActivity.this, id, alarm);

                    Toast.makeText(MainActivity.this, "已删除闹钟", Toast.LENGTH_SHORT).show();


                    List<AlarmModel> alarmModels = db.getAllAlarms();
                    if (alarmModels.isEmpty()) {
                        mNoAlarmTextView.setVisibility(View.VISIBLE);
                    } else {
                        mNoAlarmTextView.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }

//        private void showPopupMenu(View v, final int position) {
//            PopupMenu menu = new PopupMenu(MainActivity.this, v);
//            menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
//            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//
//                    View view = mRecyclerView.getChildAt(position);
//                    int po = mRecyclerView.getChildAdapterPosition(view);
//                    int id = IDmap.get(po);
//                    switch (item.getItemId()) {
//                        case R.id.popup_delete:
//                            AlarmModel alarm = db.getAlarm(id);
//                            db.deleteAlarm(alarm);
//                            db.deleteQuestion(alarm);
//                            adapter.removeItemSelected(po);
//                            adapter.onDeleteItem(getDefaultItemCount());
//
//                            cancelAlarm(MainActivity.this,id,alarm);
//
//                            List<AlarmModel> alarmModels = db.getAllAlarms();
//                            if (alarmModels.isEmpty()) {
//                                mNoAlarmTextView.setVisibility(View.VISIBLE);
//                            } else {
//                                mNoAlarmTextView.setVisibility(View.INVISIBLE);
//                            }
//                            break;
//                        case R.id.popup_edit:
//                            selectAlarm(id);
//                            break;
//                        default:
//                            break;
//                    }
//                    return false;
//                }
//            });
//
//            menu.show();
//        }


        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener {

            private TextView mTitleText, mTimeText, mRepeatText;
            private ImageView mActiveImage, mThumbnailImage, mOverFlowImage;
            private ColorGenerator mColorGenerator = ColorGenerator.DEFAULT;
            private TextDrawable mDrawableBuilder;
            private int itemPosition;


            public MyViewHolder(final View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                itemView.setLongClickable(true);

                mTitleText = (TextView) itemView.findViewById(R.id.re_tittle);
                mTimeText = (TextView) itemView.findViewById(R.id.re_time);
                mRepeatText = (TextView) itemView.findViewById(R.id.re_repeatType);
                mActiveImage = (ImageView) itemView.findViewById(R.id.active_image);
                mThumbnailImage = (ImageView) itemView.findViewById(R.id.thumbnail_image);
                mOverFlowImage = (ImageView) itemView.findViewById(R.id.delete_image);

                mActiveImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int po = getItemPosition(itemPosition);

                        View view = mRecyclerView.getChildAt(po);
                        int id = mRecyclerView.getChildAdapterPosition(view);
                        int CheckedAlarmID = IDmap.get(id);
                        AlarmItem item = mItems.get(po);
                        AlarmModel alarmModel = db.getAlarm(CheckedAlarmID);

                        if (item.mActive.equals("true")) {

                            item.mActive = "false";
                            setActiveImage(item.mActive);
                            alarmModel.setActive("false");
                            setActiveImage(item.mActive);
                            db.updateAlarm(alarmModel);
                            cancelAlarm(MainActivity.this, CheckedAlarmID, alarmModel);
                            Toast.makeText(MainActivity.this, "已关闭闹钟", Toast.LENGTH_SHORT).show();

                        } else if (item.mActive.equals("false")) {

                            item.mActive = "true";
                            setActiveImage(item.mActive);
                            alarmModel.setActive("true");
                            db.updateAlarm(alarmModel);
                            restartAlarm(item.mTime, item.mRepeatType, CheckedAlarmID);
                            Toast.makeText(MainActivity.this, "已开启闹钟", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

            @Override
            public void onClick(View v) {
                int id = mRecyclerView.getChildAdapterPosition(v);
                int CheckedAlarmID = IDmap.get(id);
                selectAlarm(CheckedAlarmID);
            }



            public void setAlarmTitle(String title) {
                mTitleText.setText(title);
                String letter = "a";
                if (title != null && !title.isEmpty()) {
                    letter = title.substring(0, 1);
                }

                int color = mColorGenerator.getRandomColor();

                mDrawableBuilder = TextDrawable.builder().buildRound(letter, color);
                mThumbnailImage.setImageDrawable(mDrawableBuilder);
            }

            public void setAlarmTime(String time) {
                mTimeText.setText(time);
            }

            public void setRepeatType(String type) {
                mRepeatText.setText(type);

            }

            public void setActiveImage(String active) {
                if (active.equals("true")) {
                    mActiveImage.setImageResource(R.drawable.ic_alarm_on_grey_600_24dp);
                } else if (active.equals("false")) {
                    mActiveImage.setImageResource(R.drawable.ic_alarm_off_grey_600_24dp);
                }
            }

            public int getItemPosition(int position) {
                this.itemPosition = position;
                return position;
            }
        }


        class AlarmItem {
            public String mTitle;
            public String mTime;
            public String mRepeatType;
            public String mRepeatCode;
            public String mActive;

            public AlarmItem(String title, String time, String repeatNormal, String repeatDefine, String active) {
                this.mTitle = title;
                this.mTime = time;
                this.mRepeatType = repeatNormal;
                this.mRepeatCode = repeatDefine;
                this.mActive = active;

            }
        }

        public List<AlarmItem> loadData() {

            ArrayList<AlarmItem> items = new ArrayList<>();
            List<AlarmModel> am = db.getAllAlarms();
            List<String> Titles = new ArrayList<>();
            List<String> Repeat = new ArrayList<>();
            List<String> RepeatCode = new ArrayList<>();
            List<String> Actives = new ArrayList<>();
            List<String> Time = new ArrayList<>();
            List<Integer> IDList = new ArrayList<>();
            List<MyTimeSorter> TimeSortList = new ArrayList<>();

            for (AlarmModel a : am) {
                Titles.add(a.getTitle());
                Time.add(a.getTime());
                Repeat.add(a.getRepeatType());
                RepeatCode.add(a.getRepeatCode());
                Actives.add(a.getActive());
                IDList.add(a.getID());
            }

            int key = 0;
            for (int k = 0; k < Titles.size(); k++) {
                TimeSortList.add(new MyTimeSorter(key, Time.get(k)));
                key++;
            }

            Collections.sort(TimeSortList, new TimeComparator());

            int k = 0;
            for (MyTimeSorter item : TimeSortList) {

                int i = item.getIndex();
                items.add(new AlarmItem(Titles.get(i), Time.get(i), Repeat.get(i), RepeatCode.get(i), Actives.get(i)));
                IDmap.put(k, IDList.get(i));
                k++;
            }
            return items;
        }

        public class TimeComparator implements Comparator {
            DateFormat f = new SimpleDateFormat("hh:mm");

            public int compare(Object a, Object b) {
                String o1 = ((MyTimeSorter) a).getTime();
                String o2 = ((MyTimeSorter) b).getTime();

                try {
                    return f.parse(o1).compareTo(f.parse(o2));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

    }

    private void restartAlarm( final String time, final String repeat, final int id) {

        connection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (AlarmService.MyBinder)service;
                switch (repeat){
                    case "只响一次":
                        binder.setSingleAlarm(getApplicationContext(),time,id);
                        break;
                    case "每天":
                        binder.setEverydayAlarm(getApplicationContext(),time,id);
                        break;
                    default:
                        AlarmModel model = db.getAlarm(id);
                        String repeatCode = model.getRepeatCode();
                        binder.setDiyAlarm(getApplicationContext(),repeat,time,id, repeatCode);
                }

                Log.d("MainActivity","重启闹钟");
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("MainActivity","解绑服务");
            }
        };
        Intent intent = new Intent(this,AlarmService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);


        unbindService(connection);

    }

    private void selectAlarm(int id) {
        String CheckedAlarm = Integer.toString(id);
        Intent intent = new Intent(this, EditAlarmActivity.class);
        intent.putExtra(EditAlarmActivity.ALARM_ID, CheckedAlarm);
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.setItemCount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_finish:
                finish();
                break;
            case R.id.action_about:
                Intent i = new Intent(this,AboutActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
