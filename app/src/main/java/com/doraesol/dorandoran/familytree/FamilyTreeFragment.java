




package com.doraesol.dorandoran.familytree;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doraesol.dorandoran.ActivityResultEvent;
import com.doraesol.dorandoran.BusProvider;
import com.doraesol.dorandoran.FamilyTreeSearchActivity;
import com.doraesol.dorandoran.MainActivity;
import com.doraesol.dorandoran.R;
import com.doraesol.dorandoran.config.DataConfig;
import com.doraesol.dorandoran.config.ResultCode;
import com.doraesol.dorandoran.config.Server;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 */
public class FamilyTreeFragment extends Fragment {

    final String LOG_TAG = FamilyTreeFragment.class.getSimpleName();

    @BindView(R.id.fam_familytree)
    FloatingActionMenu fam_familytree;
    @BindView(R.id.wv_familytree)
    WebView wv_familytree;
    @BindView(R.id.ll_shared_user_list)
    LinearLayout ll_shared_user_list;
    @BindView(R.id.hsv_shared_user_list)
    HorizontalScrollView hsv_shared_user_list;

    Toolbar tb_main_bar;
    int currentMode = 0;


    public FamilyTreeFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_family_tree, container, false);
        ButterKnife.bind(this, view);
        fam_familytree.setClosedOnTouchOutside(true);
        tb_main_bar = (Toolbar) getActivity().findViewById(R.id.tb_main_bar);
        initializeFamilyTreeSetting();
        BusProvider.getInstance().register(this);

        // 웹뷰가 초기화된 후 호출이 되어야 하기 때문에 1.5초간 딜레이를 줌 향후 개선해야 할 코드임
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String data = loadCurrentFamilyTreeInfo();
                executeJsFunction("toJS_PrintFamilyTree", data);
                //Toast.makeText(getActivity().getApplicationContext(), "불러오기..", Toast.LENGTH_LONG).show();
            }
        }, 1500);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        BusProvider.getInstance().unregister(this);
    }

    private void initializeFamilyTreeSetting(){
        wv_familytree.setWebViewClient(new WebViewClient());
        wv_familytree.setWebChromeClient(new WebChromeClient());
        wv_familytree.addJavascriptInterface(new FamilyTreeJSCallBack(), "to_Android");

        WebSettings webSettings = wv_familytree.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);

        wv_familytree.loadUrl(Server.CONNETION_FAMILYTREE_UI);
        hsv_shared_user_list.setVisibility(View.GONE);
        Log.d("chromium", "load url : " + Server.CONNETION_FAMILYTREE_UI);



        tb_main_bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.menu_family_tree_save){
                    showSaveDlg();
                }
                return false;
            }
        });
    }

    private void showSaveDlg(){
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(getActivity());

        // 다이얼로그 메세지
        alertdialog.setMessage("저장 하시겠습니까?");

        // 확인버튼
        alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentMode = 1;
                wv_familytree.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("CALLBACK", "toJS_setFamilyTreeMode is triggered .. ");
                        wv_familytree.loadUrl("javascript:toJS_setFamilyTreeMode(" + currentMode + ")");
                    }});

                executeJsFunction("toJS_GetCurrentFamilyTreeInfo");

                tb_main_bar.getMenu().clear();
                //loadFamilyTreeFromJson();
                Toast.makeText(getContext(), "저장되었습니다", Toast.LENGTH_LONG).show();
            }
        });

        // 취소버튼
        alertdialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alert = alertdialog.create();
        alert.setIcon(R.drawable.ic_list_genogram);
        alert.setTitle("가계도");
        alert.show();
    }

    private void saveCurrentFamilyTreeInfo(String paramJsonData){
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(DataConfig.FAMILY_TREE_INFO, paramJsonData);
        editor.commit();
    }

    private String loadCurrentFamilyTreeInfo(){
        String retJsonData;
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        retJsonData = mPref.getString(DataConfig.FAMILY_TREE_INFO, DataConfig.FAMILY_TREE_DEFAULT_DATA);

        if(retJsonData.equals(DataConfig.EMPTY_DATA)){
            Log.d("JSONDATA", "invalid Json Data .. ");
        }
        else {
            Log.d("JSONDATA", "valid json data .. ");
        }

        return retJsonData;
    }


    // Activity 에서 Fragment로 통신
    @Subscribe
    public void onActivityResultFromFamilyTreeNodeInfo(ActivityResultEvent event){
        String id       = "";
        String name     = "";
        String age      = "";
        String gender   = "";
        String relation = "";
        String phone    = "";
        String birth    = "";
        String pictureString = null;

        // 노드 수정 완료 시
        if(event.getResultCode() == ResultCode.ACK_RESULT_SUCCESS){
            id = event.getData().getExtras().getString("id");
            name = event.getData().getExtras().getString("name");
            age = event.getData().getExtras().getString("age");
            gender = event.getData().getExtras().getString("gender");
            relation = event.getData().getExtras().getString("relation");
            phone = event.getData().getExtras().getString("phone");
            birth = event.getData().getExtras().getString("birth");

            // 사진 데이터 받아오기
            if(event.getData().getExtras().getString("picture") != null){
                pictureString = event.getData().getExtras().getString("picture");
                // 서버로 프로필 사진 업로드
                upLoadProfilePicture(pictureString);
            }

            executeJsFunction("toJS_SetSelecetedMemberInfo", name, age, gender, relation);
        }
        // 복구 파일이 선택 되었을 때
        else if(event.getResultCode() == ResultCode.ACK_READ_RESTORE_DATA){
            FamilyTreeBackupHelper familyTreeBackupHelper =
                    new FamilyTreeBackupHelper(getContext());
            String encryptedData = event.getData().getExtras().getString("RESTORE_DATA");
            String decryptedData = familyTreeBackupHelper.decryptedMessageFromAES256("1111", encryptedData);

            //Log.d(LOG_TAG, "ACK_READ_RESTORE_DATA : " + encryptedData);
            //Log.d(LOG_TAG, "ACK_READ_RESTORE_DATA : " + decryptedData);

            executeJsFunction("toJS_PrintFamilyTree", decryptedData);
            Toast.makeText(getContext(), "복구 파일을 불러왔습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.fab_familytree_insert_mode,
          //  R.id.fab_familytree_edit_mode,
            R.id.fab_familytree_search,
            R.id.fab_familytree_backup,
            R.id.fab_familytree_restore})
    public void onFabButtonClicked(View view){
        tb_main_bar.getMenu().clear();

        switch (view.getId()){
            // 모드 선택
            case R.id.fab_familytree_insert_mode:
                selectModeDialog();

                break;
            // 노드 수정 모드
            /*
            case R.id.fab_familytree_edit_mode:

                break;
            */
            // 가계도 불러오기
            case R.id.fab_familytree_search:
                //startActivity(new Intent(getActivity(), FamilyTreeSearchActivity.class));
                hsv_shared_user_list.setVisibility(View.VISIBLE);
                testFuncHSV();
                break;

            // 가계도 백업
            case R.id.fab_familytree_backup:
                FamilyTreeBackupHelper familyTreeBackupHelper =
                        new FamilyTreeBackupHelper(getContext());
                boolean isSucceed = false;
                String backupData = loadCurrentFamilyTreeInfo();

                // AES256 메세지 암, 복호화 - 패스워드 지정 필요
                String encryptedData = familyTreeBackupHelper.encryptedMessageFromAES256("1111", backupData);

                Log.d(LOG_TAG, "가계도 백업 원본 데이터 : " + backupData);
                Log.d(LOG_TAG, "가계도 백업 암호화 데이터 : " + encryptedData);


                isSucceed = familyTreeBackupHelper.saveBackupFile(encryptedData);

                if(isSucceed){
                    Toast.makeText(getActivity().getApplicationContext(), "데이터 백업 성공", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "데이터 백업 실패", Toast.LENGTH_SHORT).show();
                }

                familyTreeBackupHelper.getFileNameList();
                break;

            case R.id.fab_familytree_restore:
                Intent intent = new Intent(getContext(), FamilyTreeRestoreActivity.class);
                startActivity(intent);
                break;
        }

        fam_familytree.close(true);

        wv_familytree.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("CALLBACK", "toJS_setFamilyTreeMode is triggered .. ");
                Log.d("CALLBACK", "currentMode : " + currentMode);
                wv_familytree.loadUrl("javascript:toJS_setFamilyTreeMode(" + currentMode + ")");
            }
        },1000);
    }

    private void upLoadProfilePicture(String paramByteArray){

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("upload_image", paramByteArray.toString())
                .build();

        //request
        Request request = new Request.Builder()
                .url(Server.UPLOAD_PROFILE_IMAGE)
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectModeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.mode_select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        // JavaScript 메서드는 반드시 메인 스레드에서 호출
                        wv_familytree.post(new Runnable() {
                            @Override
                            public void run() {
                                // 추가 모드
                                if(which == 0){
                                    currentMode = 0;
                                    tb_main_bar.inflateMenu(R.menu.menu_family_tree);
                                }
                                // 수정 모드
                                else if(which == 1){
                                    currentMode = 1;
                                }

                                Log.d("CALLBACK", "selectModeDialog() currentMode : " + currentMode);
                            }
                        });
                    }
                });

        AlertDialog alert = builder.create();
        alert.setIcon(R.drawable.ic_list_genogram);
        alert.setTitle("모드 선택");
        alert.show();
    }

    // 자바스크립트 함수 실행
    public void executeJsFunction(String paramFuncName, Object... params){

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:")
                .append(paramFuncName)
                .append("(");

        for(int i=0; i<params.length; i++){
            if(i == params.length-1) {
                stringBuilder.append("'"+params[i].toString()+"'");
                break;
            }

            stringBuilder.append("'"+params[i]+"'").append(',');
        }
        stringBuilder.append(')');


        wv_familytree.post(new Runnable() {
            @Override
            public void run() {
                wv_familytree.loadUrl(stringBuilder.toString());
            }

        });


        Log.d("jsfunc", stringBuilder.toString());
    }


    // 공유된 가계도 목록 출력 함수
    private void createSharedUserData(int paramImgResource, String paramText) throws Exception{
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(280, 280);
        param.gravity = Gravity.CENTER;
        final FamilyTreeSharedUser familyTreeSharedUser = new FamilyTreeSharedUser(getContext());
        familyTreeSharedUser.setLayoutParams(param);
        familyTreeSharedUser.setImage(paramImgResource);
        familyTreeSharedUser.setText(paramText);

        familyTreeSharedUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = familyTreeSharedUser.getText();
                String ret_json_data = loadSharedUserJsonData(userId);
                executeJsFunction("toJS_PrintFamilyTree", ret_json_data);
                hsv_shared_user_list.setVisibility(View.GONE);
            }
        });

        ll_shared_user_list.addView(familyTreeSharedUser);
    }

    // 공유된 가계도 목록 불러오기
    private String loadSharedUserList(){
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // data1#data2#data3
        String shared_user_list = mPref.getString(DataConfig.SHARED_USER_LIST, DataConfig.EMPTY_DATA);

        return shared_user_list;
    }

    // 아이디를 기반으로 가계도 불러오기
    private String loadSharedUserJsonData(String paramText){
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // data1#data2#data3
        String ret_json_data = mPref.getString(paramText, DataConfig.EMPTY_DATA);

        return ret_json_data;
    }


    // 공유 목록 기능 테스트 함수
    private void testFuncHSV(){
        try {
            // 기존의 모든 뷰 제거
            ll_shared_user_list.removeAllViews();

            String[] shared_user_list = loadSharedUserList().split("#");

            for (String shared_user : shared_user_list) {
                createSharedUserData(R.drawable.img_shared_user_default, shared_user);
            }
        }
        catch(Exception ex){
            Log.d(LOG_TAG, ex.toString());
        }
    }


    private void loadFamilyTreeFromJson(String data){
        executeJsFunction("loadFamilyTreeView", data);
    }





    // 안드로이드 자바스크립트 통신 규약을 정의한 클래스
    class FamilyTreeJSCallBack {
        // 추가 모드 - 노드 클릭시
        @JavascriptInterface
        public void onFamilyTreeNodeClicked(String selectedNodeName){

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("선택된 노드 : " + selectedNodeName)
                    .setItems(R.array.nodelist, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, final int which) {
                            // JavaScript 메서드는 반드시 메인 스레드에서 호출
                            wv_familytree.post(new Runnable() {
                                @Override
                                public void run() {
                                    wv_familytree.loadUrl("javascript:toJS_AddMember(" + which + ")");
                                }
                            });
                        }
                    });
            // inject TableLayout
                /*
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dlg_family_tree
                        = inflater.inflate(R.layout.dlg_family_tree_insert_node, null);
                builder.setView(dlg_family_tree);
                */
            builder.show();
        }

        @JavascriptInterface
        public void getFamilyTreeNodeInfo(String paramName, String paramAge, String paramGender, String paramRelation, String paramImage){

            // id, name, age, gender, relation, phone, birth
            Intent intent = new Intent(getContext(), FamilyTreeNodeInfo.class);
            intent.putExtra("name", paramName)
                    .putExtra("age", paramAge)
                    .putExtra("gender", paramGender)
                    .putExtra("relation", paramRelation)
                    .putExtra("image", paramImage);

            startActivity(intent);
        }

        // 가계도 데이터를 JSON 데이터 형식으로 반환
        @JavascriptInterface
        public void getCurrentFamilyTreeInfo(String retJsonData){
            saveCurrentFamilyTreeInfo(retJsonData);

            Log.d("JSONDATA", loadCurrentFamilyTreeInfo());
        }
    }



}
