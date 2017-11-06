package com.doraesol.dorandoran;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.doraesol.dorandoran.config.ResultCode;
import com.doraesol.dorandoran.config.Server;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JoinActivity extends AppCompatActivity {

    private final String LOG_TAG = JoinActivity.class.getSimpleName();

    @BindView(R.id.et_join_id)      EditText et_join_id;
    @BindView(R.id.et_join_pw)      EditText et_join_pw;
    @BindView(R.id.et_join_name)    EditText et_join_name;
    @BindView(R.id.et_join_birth)   EditText et_join_birth;
    @BindView(R.id.et_join_email)   EditText et_join_email;
    @BindView(R.id.rb_join_men)     RadioButton rb_join_men;
    @BindView(R.id.rb_join_women)   RadioButton rb_join_women;
    String mGender;
    boolean isJoinSucceed;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);
        mGender = "";
        isJoinSucceed = false;
    }

    @OnClick({R.id.btn_join_ok, R.id.btn_join_cancel})
    public void onButtonClicked(View view){
        String user_id = et_join_id.getText().toString();
        String user_pw = et_join_pw.getText().toString();
        String user_name = et_join_name.getText().toString();
        String user_birth = et_join_birth.getText().toString();
        String user_email = et_join_email.getText().toString();
        String user_gender = mGender;

        switch(view.getId()){
            case R.id.btn_join_ok:
                fcmJoinTask joinTask = new fcmJoinTask();
                joinTask.execute(user_id, user_pw);
                new userJoinTask().execute(
                        user_id, user_pw, user_name,
                        user_birth, user_email, user_gender);
                break;
            case R.id.btn_join_cancel:
                finish();
                break;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isJoinSucceed){
                    finish();
                }
            }
        }, 1000);

    }

    @OnClick({R.id.rb_join_men, R.id.rb_join_women})
    public void onGenderSelected(View view){
        switch(view.getId()){
            case R.id.rb_join_men:
                mGender = "MAN";
                break;
            case R.id.rb_join_women:
                mGender = "WOMAN";
                break;
        }
    }

    class userJoinTask extends AsyncTask<String,Void,Integer>{
        @Override
        protected Integer doInBackground(String... params) {
            String id = params[0];
            String pw = params[1];
            String name = params[2];
            String birth = params[3];
            String email = params[4];
            String gender = params[5];

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("user_id", id)
                    .add("user_pw", pw)
                    .add("user_name", name)
                    .add("user_birth", birth)
                    .add("user_email", email)
                    .add("user_gender", gender)
                    .build();

            Request request = new Request.Builder()
                    .url(Server.USER_JOIN)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String returnValue = response.body().string();
                int resultCode = Integer.parseInt(returnValue);
                return resultCode;
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

            return ResultCode.ACK_RESULT_FAIL;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);
            if(resultCode == ResultCode.ACK_RESULT_SUCCESS){
                Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_LONG).show();
                isJoinSucceed = true;
            }
            else if(resultCode == ResultCode.ACK_RESULT_FAIL){
                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_LONG).show();
            }
        }
    }

    class fcmJoinTask extends AsyncTask<String,Void,Integer>{
        @Override
        protected Integer doInBackground(String... params) {
            String id = params[0];
            String pw = params[1];

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id", id)
                    .add("pw", pw)

                    .build();

            Request request = new Request.Builder()
                    .url(Server.JOIN)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String returnValue = response.body().string();
                int resultCode = Integer.parseInt(returnValue);
                return resultCode;
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

            return ResultCode.ACK_RESULT_FAIL;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);

            if(resultCode == ResultCode.ACK_RESULT_SUCCESS){
                //Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == ResultCode.ACK_RESULT_FAIL){
                //Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_LONG).show();
            }
        }
    }

}
