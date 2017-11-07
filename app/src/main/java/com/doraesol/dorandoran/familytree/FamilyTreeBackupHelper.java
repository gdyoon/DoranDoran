package com.doraesol.dorandoran.familytree;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by YOONGOO on 2017-05-21.
 */

public class FamilyTreeBackupHelper {
    final String LOG_TAG = FamilyTreeBackupHelper.class.getSimpleName();
    Context context;

    public FamilyTreeBackupHelper(Context paramContext){
        this.context = paramContext;
    }

    public void getFileNameList(){
        //String dirPath = context.getFilesDir().getAbsolutePath() + "/FamilyTreeBackUp/";
        File backupDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/FamilyTreeBackUp/");
        File[] fileList = backupDir.listFiles();

        registerToMediaScanner(backupDir);

        if(backupDir.exists()) {
            for(int i=0; i<fileList.length; i++) {
                registerToMediaScanner(fileList[i]);
                Log.d(LOG_TAG, fileList[i].getPath());
            }
        }
        else {
            Log.d(LOG_TAG, "디렉토리를 먼저 생성해야 합니다.");


        }
    }

    public boolean saveBackupFile(String paramBackupData){
        //checkStoragePermission();

        /*if(getExternalStorageState()){
            Log.d(LOG_TAG, "외부 저장소가 없습니다.");
            return false;
        }*/

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateTimeString = sdfNow.format(date);
        //String dirPath = context.getFilesDir().getAbsolutePath() + "/FamilyTreeBackUp5/";
        //File backupDir = new File(dirPath);
        File backupDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/FamilyTreeBackUp/");
        String backupFilePath = backupDir.getPath() + "/" + currentDateTimeString.trim() +".bak";

        if(!backupDir.exists()) {
            if (backupDir.mkdirs()) {
                registerToMediaScanner(backupDir);
                Log.d(LOG_TAG, "디렉토리 생성 성공");
            } else {
                Log.d(LOG_TAG, "디렉토리 생성 실패");
            }
        }
        else
        {
            Log.d(LOG_TAG, "디렉토리가 이미 존재합니다.");
        }


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(backupFilePath , true));
            writer.write(paramBackupData);
            writer.write("\n");
            writer.flush();
            writer.close();

            Log.d(LOG_TAG, "filepath : " + backupFilePath);

            return true;
        } catch (IOException e) {
            Log.d(LOG_TAG, e.toString());

            return false;
        }
    }

    private void registerToMediaScanner( File newFile ){
        MediaScannerConnection.scanFile(context, new String[]{ newFile.getPath() },null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                // TODO
            }
        });
    }

    private boolean getExternalStorageState(){
        String state = Environment.getExternalStorageState();
        boolean isAvailable = state.equals(Environment.MEDIA_MOUNTED);

        if(isAvailable){
            return true;
        }
        else{
            return false;
        }
    }

    public void checkStoragePermission(){
        new TedPermission(context)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.d(LOG_TAG, "외부 저장소 권한 설정 완료");
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Log.d(LOG_TAG, "외부 저장소 권한 설정 실패 : " + deniedPermissions.toString());
        }
    };

    public String encryptedMessageFromAES256(String password, String paramMessage){
        try {
            String encryptedMessage = AESCrypt.encrypt(password, paramMessage);
            Log.d(LOG_TAG, "Succeed to Message Encryption : " + encryptedMessage);
            return encryptedMessage;
        }catch (GeneralSecurityException e) {
            Log.d(LOG_TAG, "Failed to Encryption  : " + e.toString());

            return null;
        }
    }

    public String decryptedMessageFromAES256(String password, String paramMessage){
        try {
            String decryptedMessage = AESCrypt.decrypt(password, paramMessage);

            Log.d(LOG_TAG, "Succeed to Message Decryption : " + decryptedMessage);
            return decryptedMessage;
        }catch (GeneralSecurityException e) {
            Log.d(LOG_TAG, "Failed to Decryption  : " + e.toString());

            return null;
        }
    }
}




