package com.zyascend.MyAlarm.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zyascend.MyAlarm.Model.AlarmModel;
import com.zyascend.MyAlarm.Model.QuestionModel;
import com.zyascend.MyAlarm.R;
import com.zyascend.MyAlarm.data.MyAlarmDataBase;

import java.util.Random;

/**
 *
 * Created by Administrator on 2016/3/26.
 */
public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout questionLayout,setQueAndAnsLayout;

    private int width,alarmID;
    private String diyQuestion;
    private String diyAnswer;
    private String flag = null;
    private Button set_cal,set_que,que_sure,que_cancel;
    private EditText ed_question,ed_answer;
    private MyAlarmDataBase db;
    private QuestionModel questionModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        initViews();

        setQueAndAnsLayout.setVisibility(View.INVISIBLE);

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;

        db = new MyAlarmDataBase(this);

        alarmID = Integer.parseInt(getIntent().getStringExtra(EditAlarmActivity.ALARM_ID));

        try{
            QuestionModel qm = db.getQuestion(alarmID);
            flag = qm.getQuestion();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void initViews() {

        questionLayout = (RelativeLayout) findViewById(R.id.rela_que);
        setQueAndAnsLayout = (RelativeLayout) findViewById(R.id.set_que_ans_lay);

        ed_answer = (EditText) findViewById(R.id.answer_editText);
        ed_question = (EditText) findViewById(R.id.ques_editText);

        set_cal = (Button) findViewById(R.id.set_calculate_que_btn);
        set_que = (Button) findViewById(R.id.set_que_btn);
        que_sure = (Button) findViewById(R.id.question_sure_btn);
        que_cancel = (Button) findViewById(R.id.question_cancel_btn);

        set_cal.setOnClickListener(this);
        set_que.setOnClickListener(this);
        que_sure.setOnClickListener(this);
        que_cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_calculate_que_btn:

                AlarmModel model = db.getAlarm(alarmID);
                if (flag!= null){

                    db.deleteQuestion(model);
                    int ID = db.addQuestion(new QuestionModel("flag_calculate", "000", alarmID));
                    Toast.makeText(QuestionActivity.this, "新的随机问题设置好啦^_^", Toast.LENGTH_SHORT).show();
                }else {
                    int ID = db.addQuestion(new QuestionModel("flag_calculate", "000", alarmID));
                    Toast.makeText(QuestionActivity.this, "随机问题设置好啦^_^", Toast.LENGTH_SHORT).show();
                }
                onBackPressed();
                break;
            case R.id.question_cancel_btn:

                if (setQueAndAnsLayout.getVisibility() == View.VISIBLE){
                    setQueAndAnsLayout.setVisibility(View.INVISIBLE);
                }

                ObjectAnimator animator2 = ObjectAnimator.ofFloat(questionLayout,"translationX",0).setDuration(300);
                animator2.start();
                break;

            case R.id.question_sure_btn:

                diyQuestion = ed_question.getText().toString();
                diyAnswer = ed_answer.getText().toString();
                if (diyAnswer.isEmpty()||diyQuestion.isEmpty()){
                    Toast.makeText(QuestionActivity.this, "(＞﹏＜)，问题和答案都不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    if (flag != null){
                        QuestionModel qm = db.getQuestion(alarmID);
                        qm.setQuestion(diyQuestion);
                        qm.setAnswer(diyAnswer);
                        Log.d("QuestionActivity","更新答案"+diyAnswer);
                        qm.setAlarmID(alarmID);
                        db.updateQuestion(qm);
                    }else {
                        int ID = db.addQuestion(new QuestionModel(diyQuestion,diyAnswer,alarmID));
                        Log.d("QuestionActivity","添加答案"+diyAnswer);
                        Log.d("QuestionActivity","添加问题"+diyQuestion);
                        Toast.makeText(QuestionActivity.this, "自定义问题设置好啦^_^", Toast.LENGTH_SHORT).show();
                    }
                    onBackPressed();
                }
                break;

            case R.id.set_que_btn:

                ObjectAnimator animator1 = ObjectAnimator.ofFloat(questionLayout,"translationX",width).setDuration(300);
                animator1.start();

                if (setQueAndAnsLayout.getVisibility() == View.INVISIBLE){
                    setQueAndAnsLayout.setVisibility(View.VISIBLE);
                }

                if (flag!=null){
                    questionModel = db.getQuestion(alarmID);
                    ed_question.setText(questionModel.getQuestion());
                    ed_answer.setText(questionModel.getAnswer());
                }
                break;

        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
