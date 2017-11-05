package com.doraesol.dorandoran.familytree;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.doraesol.dorandoran.ActivityResultEvent;
import com.doraesol.dorandoran.BusProvider;
import com.doraesol.dorandoran.R;
import com.doraesol.dorandoran.config.ResultCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FamilyTreeRestoreActivity extends AppCompatActivity {

    final String LOG_TAG = FamilyTreeRestoreActivity.class.getSimpleName();
    File[] restoreFileList;

    @BindView(R.id.lv_family_tree_restore)
    ListView lv_family_tree_restore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree_restore);
        ButterKnife.bind(this);

        ArrayAdapter<String> fileDataAdapter = new ArrayAdapter<>(this, R.layout.single_list_item);

        // 복구 파일 이름 리스트 가져오기
        restoreFileList = getRestoreFileList();
        String[] restoreFileNameList = getRestoreFileNameList();
        for(String fileName : restoreFileNameList){
            fileDataAdapter.add(fileName);
        }

        lv_family_tree_restore.setAdapter(fileDataAdapter);

        lv_family_tree_restore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), ""+position, Toast.LENGTH_SHORT).show();
                showConfirmDialog(position);
            }
        });
    }

    // 가계도 복구 파일 리스트 가져오기
    private File[] getRestoreFileList(){
        File backupDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/FamilyTreeBackUp/");
        File[] restoreFileList = backupDir.listFiles();

        if(backupDir.exists()) {
           return restoreFileList;
        }
        else {
            Log.d(LOG_TAG, "디렉토리를 먼저 생성해야 합니다.");

            return null;
        }
    }

    // 가계도 복구 파일 이름 리스트 가져오기
    private String[] getRestoreFileNameList() {
        File backupDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/FamilyTreeBackUp/");
        File[] restoreFileList = backupDir.listFiles();
        String[] restoreFileNameList = new String[restoreFileList.length];

        if(backupDir.exists()) {
            for(int i=0; i<restoreFileList.length; i++){
                restoreFileNameList[i] = restoreFileList[i].getName();
            }

            return restoreFileNameList;
        }
        else {
            Log.d(LOG_TAG, "디렉토리를 먼저 생성해야 합니다.");

            return null;
        }
    }

    private void showConfirmDialog(final int filePosition){
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);

        // 다이얼로그 메세지
        alertdialog.setMessage("가계도를 불러오시겠습니까?");

        // 확인버튼
        alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File selectedFile = restoreFileList[filePosition];
                StringBuilder stringBuilder = new StringBuilder();
                FileReader fr;
                String fileStr;
                int data;

                try {
                    fr = new FileReader(selectedFile);

                    while((data = fr.read()) != -1) {
                        stringBuilder.append((char)data);
                    }

                    fr.close();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "파일을 찾을 수 없습니다.");
                    return;
                }

                Intent retIntent = new Intent();
                retIntent.putExtra("RESTORE_DATA", stringBuilder.toString());

                BusProvider.getInstance().post(new ActivityResultEvent(0, ResultCode.ACK_READ_RESTORE_DATA, retIntent));
                finish();
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
        alert.setTitle("가계도 복구");
        alert.show();
    }

}
