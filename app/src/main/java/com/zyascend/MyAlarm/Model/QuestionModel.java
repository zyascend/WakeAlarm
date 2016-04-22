package com.zyascend.MyAlarm.Model;

/**
 *
 * Created by Administrator on 2016/3/26.
 */
public class QuestionModel {

    private int questionID;
    private int alarmID;
    private String question;
    private String answer;

    public QuestionModel(int mQues_id,String mQuestion,String mAnswer,int mAla_id){
        questionID = mQues_id;
        alarmID = mAla_id;
        question = mQuestion;
        answer = mAnswer;

    }
    public QuestionModel(String mQuestion,String mAnswer,int mAla_id){
        alarmID = mAla_id;
        question = mQuestion;
        answer = mAnswer;

    }

    public QuestionModel(){}

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getQuestionID() {
        return questionID;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    public int getAlarmID() {
        return alarmID;
    }

    public void setAlarmID(int alarmID) {
        this.alarmID = alarmID;
    }
}
