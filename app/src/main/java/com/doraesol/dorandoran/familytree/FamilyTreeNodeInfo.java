package com.doraesol.dorandoran.familytree;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.doraesol.dorandoran.ActivityResultEvent;
import com.doraesol.dorandoran.BusProvider;
import com.doraesol.dorandoran.R;
import com.doraesol.dorandoran.config.ResultCode;
import com.doraesol.dorandoran.map.MapMainActivity;
import com.doraesol.dorandoran.config.Server;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FamilyTreeNodeInfo extends AppCompatActivity {

    final String LOG_TAG = FamilyTreeNodeInfo.class.getSimpleName();

    @BindView(R.id.tv_family_tree_info_id)      TextView tv_family_tree_info_id;
    @BindView(R.id.tv_family_tree_info_name)    TextView tv_family_tree_info_name;
    @BindView(R.id.tv_family_tree_info_age)     TextView tv_family_tree_info_age;
    @BindView(R.id.tv_family_tree_info_gender)  TextView tv_family_tree_info_gender;
    @BindView(R.id.tv_family_tree_info_relation)TextView tv_family_tree_info_relation;
    @BindView(R.id.tv_family_tree_info_phone)   TextView tv_family_tree_info_phone;
    @BindView(R.id.tv__family_tree_info_birth)  TextView tv_family_tree_info_birth;
    @BindView(R.id.fab_family_tree_info_graves)      FloatingActionButton fab_family_tree_info_graves;
    @BindView(R.id.fab_family_tree_info_pic)    FloatingActionButton fab_family_tree_info_pic;
    @BindView(R.id.iv_family_tree_info_pic)     CircleImageView iv_family_tree_info_pic;
    final int REQUEST_PICTURE = 1000;
    byte[] pictureByteArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree_node_info);
        ButterKnife.bind(this);
        setNodeInfoFromIntent();
    }

    private void setNodeInfoFromIntent(){
        Intent intent = getIntent();
        // id, name, age, gender, relation, phone, birth
        String id       = "";
        String name     = intent.getExtras().getString("name");
        String age      = intent.getExtras().getString("age");
        String gender   = intent.getExtras().getString("gender");
        String relation = intent.getExtras().getString("relation");
        String phone    = "";
        String birth    = "";

        tv_family_tree_info_id.setText(id);
        tv_family_tree_info_name.setText(name);
        tv_family_tree_info_age.setText(age);
        tv_family_tree_info_gender.setText(gender);
        tv_family_tree_info_relation.setText(relation);
        tv_family_tree_info_phone.setText(phone);
        tv_family_tree_info_birth.setText(birth);
    }

    @OnClick({R.id.ll_family_tree_info_id, R.id.ll_family_tree_info_name,
    R.id.ll_family_tree_info_age, R.id.ll_family_tree_info_gender,
    R.id.ll_family_tree_info_relation, R.id.ll_family_tree_info_phone,
    R.id.ll_family_tree_info_birth})
    public void onLayoutNodeInfoClicked(View paramView){
        int options = -1;
        switch (paramView.getId()){
            case R.id.ll_family_tree_info_id:       options = 0;    break;
            case R.id.ll_family_tree_info_name:     options = 1;    break;
            case R.id.ll_family_tree_info_age:      options = 2;    break;
            case R.id.ll_family_tree_info_gender:   options = 3;    break;
            case R.id.ll_family_tree_info_relation: options = 4;    break;
            case R.id.ll_family_tree_info_phone:    options = 5;    break;
            case R.id.ll_family_tree_info_birth:    options = 6;    break;
        }
        showEditDialog(options);
    }

    @OnClick({R.id.iv_family_tree_info_confirm, R.id.iv_family_tree_info_back})
    public void onConfirmAndBack(View paramView){
        String id       = tv_family_tree_info_id.getText().toString();
        String name     = tv_family_tree_info_name.getText().toString();
        String age      = tv_family_tree_info_age.getText().toString();
        String gender   = tv_family_tree_info_gender.getText().toString();
        String relation = tv_family_tree_info_relation.getText().toString();
        String phone    = tv_family_tree_info_phone.getText().toString();
        String birth    = tv_family_tree_info_birth.getText().toString();

        switch (paramView.getId()){
            case R.id.iv_family_tree_info_confirm:
                    Intent intent = new Intent();
                    intent.putExtra("id", id);
                    intent.putExtra("name", name);
                    intent.putExtra("age", age);
                    intent.putExtra("gender", gender);
                    intent.putExtra("relation", relation);
                    intent.putExtra("phone", phone);
                    intent.putExtra("birth", birth);

                    if(pictureByteArray != null) {
                        intent.putExtra("picture", pictureByteArray.toString());
                        Log.d(LOG_TAG, "pictureByteArray : " + pictureByteArray);
                    }

                    new saveNodePathInfo().execute(name);
                    BusProvider.getInstance().post(new ActivityResultEvent(0, ResultCode.ACK_RESULT_SUCCESS, intent));
                    finish();

                break;

            case R.id.iv_family_tree_info_back:
                finish();
                break;
        }
    }

    @OnClick(R.id.fab_family_tree_info_pic)
    public void onProfilePictureButtonClicked(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            selectModeDialog();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    2000);
        }
    }

    private void selectModeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.mode_profile_img_select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0)      startGallery(); // 갤러리에서 이미지 선택
                        else if(which == 1) startCameraActivity(); // 사진 촬영
                    }
                });

        AlertDialog alert = builder.create();
        alert.setIcon(R.drawable.ic_list_genogram);
        alert.setTitle("프로필 사진 편집");
        alert.show();
    }

    // 갤러리에서 사진 선택
    public void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_GET_CONTENT);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE);
        }
    }

    // 사진 촬영 -> 퀄리티 개후짐 원인 찾아내야함
    public void startCameraActivity() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICTURE && resultCode == Activity.RESULT_OK) {
            Uri imgUri = data.getData();
            try {
                // 사진 Uri 데이터를 Byte 배열로 캐스팅
                InputStream is = getContentResolver().openInputStream(imgUri);
                pictureByteArray = getBytes(is);

                upLoadPictureAsyncTask upLoadProfilePicture = new upLoadPictureAsyncTask();
                upLoadProfilePicture.execute(pictureByteArray.toString());

                //upLoadProfilePicture(pictureByteArray.toString());
                Log.d(LOG_TAG, "pictureByteArray : " + pictureByteArray.toString());

            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "파일을 찾을 수 없습니다.");
            } catch (IOException e){
                Log.d(LOG_TAG, "입출력 에러");
            }

            Glide.with(this)
                    .load(imgUri)
                    .centerCrop()
                    .crossFade()
                    .into(iv_family_tree_info_pic);
        }
    }

    class saveNodePathInfo extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String user_name = params[0];

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("user_name", user_name)
                    .build();

            //request
            Request request = new Request.Builder()
                    .url(Server.USER_INSERT_PATH_NAME)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                int returnValue = Integer.parseInt(response.body().string());
                return returnValue;
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);

            if(resultCode == ResultCode.ACK_RESULT_SUCCESS){
                printToast("이름 등록");
            }
            else if(resultCode == ResultCode.ACK_RESULT_FAIL){
                printToast("이름 등록 실패");
            }
        }
    }

    class upLoadPictureAsyncTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String paramString = params[0];


            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("upload_image", paramString)
                    .build();

            //request
            Request request = new Request.Builder()
                    .url(Server.UPLOAD_PROFILE_IMAGE)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String returnValue = response.body().string();

                Log.d(LOG_TAG, "returnValue : " + returnValue);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

            Log.d(LOG_TAG, "upLoadPictureAsyncTask is executed..");

            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            super.onPostExecute(resultCode);
        }
        }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // 프로필 수정을 하기 위한 대화상자
    private void showEditDialog(final int options) {

        final EditText et_modify = new EditText(this);
        et_modify.setBackgroundColor(Color.WHITE);
        et_modify.setHint("수정할 내용을 입력해주세요");
        et_modify.setPadding(100, 50, 0, 0);

        AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
        alertdialog.setView(et_modify);
        //alertdialog.setMessage("");

        alertdialog.setPositiveButton("수정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String retStr = et_modify.getText().toString();
                TextView retTextView = getTextViewFromIndex(options);
                retTextView.setText(retStr);

                Toast.makeText(getApplicationContext(), "수정 완료", Toast.LENGTH_SHORT).show();
            }
        });

        alertdialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        String retStr = getStringFromIndex(options);
        AlertDialog alert = alertdialog.create();
        alert.setIcon(R.drawable.ic_list_genogram);
        alert.setTitle("프로필 " + retStr + " 수정");
        alert.show();
    }

    // 인덱스를 기준으로 텍스트 뷰의 리소스 반환 - 아이디 수정은 변경해야함
    private TextView getTextViewFromIndex(int options){

        TextView retTextView = null;

        switch (options){
            case 0: retTextView = tv_family_tree_info_id;       break;
            case 1: retTextView = tv_family_tree_info_name;     break;
            case 2: retTextView = tv_family_tree_info_age;      break;
            case 3: retTextView = tv_family_tree_info_gender;   break;
            case 4: retTextView = tv_family_tree_info_relation; break;
            case 5: retTextView = tv_family_tree_info_phone;    break;
            case 6: retTextView = tv_family_tree_info_birth;    break;
        }

        return retTextView;
    }

    private String getStringFromIndex(int options){
        String retStr = null;

        switch (options){
            case 0: retStr = "아이디";       break;
            case 1: retStr = "이름";      break;
            case 2: retStr = "나이";      break;
            case 3: retStr = "성별";      break;
            case 4: retStr = "관계";      break;
            case 5: retStr = "연락처";    break;
            case 6: retStr = "생년월일";  break;
        }


        return retStr;
    }
    
    @OnClick(R.id.fab_family_tree_info_pic)
    public void onUpdateProfilePicture(){
        printToast("사진 변경 클릭");
    }

    @OnClick(R.id.fab_family_tree_info_graves)
    public void onGoMap()
    {
        String name     = tv_family_tree_info_name.getText().toString();
        Intent intent = new Intent(
                getApplicationContext(),
                MapMainActivity.class);
        intent.putExtra("user_name", name);
        startActivity(intent);
    }

    private void printToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
