package com.doraesol.dorandoran.map;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.doraesol.dorandoran.R;
import com.doraesol.dorandoran.config.ResultCode;
import com.doraesol.dorandoran.config.Server;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapMyRouteActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GeolocationPermissions.Callback,
        GoogleMap.OnMapLongClickListener
{
    private LatLng currentLocation;
    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;
    private Location mCurrentLocation;
    private Marker mCurrLocationMarker;
    private LocationManager locationManager;
    private boolean walkState = false;
    private boolean markerState = false;
    private boolean markerState2 = false;
    private LatLng startLatLng = new LatLng(0, 0);        //polyline 시작점
    private LatLng endLatLng = new LatLng(0, 0);        //polyline 끝점
    private List<Polyline> polylines;
    private Marker marker;

    //갤러리
    //private ImageView iv_map_randmark;
    final int REQUEST_PICTURE = 1000;
    private final static int SECOND_ACTIVITY = 2;
    private Uri mUri;
    private String str;
    private Uri uri;
    private Bitmap bitmap;

    //디폴트 위치, Seoul
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final String TAG = "googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000; // 1초
    private AppCompatActivity mActivity = null;
    boolean askPermissionOnceAgain = false;

    // GeoJsonData
    ArrayList<String> placeList = new ArrayList<>();
    private static String jsonLatlng= "";

    @BindView(R.id.bt_map_recording) Button bt_map_recording;
    @BindView(R.id.bt_map_save) Button bt_map_save;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map_my_route);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ButterKnife.bind(this);
        startLocationService();
        polylines = new ArrayList<>();

    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }


    @OnClick({R.id.bt_map_recording, R.id.bt_map_save})
    public void recordsaveButtonClicked(View view)
    {
        switch(view.getId()){
            case R.id.bt_map_recording:
                changeWalkState();
                String walk = "Walkstate : " + walkState;
                break;
            case R.id.bt_map_save:
                Toast.makeText(MapMyRouteActivity.this, "경로가 저장 되었습니다.", Toast.LENGTH_SHORT).show();
                JSONObject obj = new JSONObject();
                try {
                    JSONArray jArray = new JSONArray();//배열이 필요할때
                    for (int i = 0; i < placeList.size(); i++)//배열
                    {
                        JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                        sObject.put("data", placeList.get(i));
                        jArray.put(sObject);
                    }
                    obj.put("data", jArray);//배열을 넣음
                    System.out.println(obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v("PLZ", obj.toString());
                break;
        }
    }

    private void changeWalkState()
    {
        if (!walkState)
        {
            Toast.makeText(getApplicationContext(), "걸음 시작", Toast.LENGTH_SHORT).show();
            walkState = true;
            markerState = true;
            markerState2 = false;
            startLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }else{
            Toast.makeText(getApplicationContext(), "걸음 종료", Toast.LENGTH_SHORT).show();
            walkState = false;
            markerState = false;
            markerState2 = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;
                checkPermissions();
            }
        }

    }

    @Override
    protected void onStop() {

        mGoogleApiClient.disconnect();

        super.onStop();

    }


    @Override
    public void onPause() {

        //위치 업데이트 중지
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            mGoogleApiClient.disconnect();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
        }

        super.onDestroy();
    }

    private void startLocationService() {

        // get manager instance
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // set listener
        GPSListener gpsListener = new GPSListener();
        long minTime = 2000;
        float minDistance = 0;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

    }

    private void drawPath(){        //polyline을 그려주는 메소드
        PolylineOptions options = new PolylineOptions()
                .add(startLatLng)
                .add(endLatLng)
                .width(4)
                .color(Color.RED)
                .geodesic(true);
        polylines.add(mGoogleMap.addPolyline(options));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 17));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }

                        if (ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            mGoogleMap.setMyLocationEnabled(true);
                        }

                        return;
                    }
                } else {
                    setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 요부 확인하세요");
                }

                break;
        }*/

        if (requestCode == REQUEST_PICTURE && resultCode == Activity.RESULT_OK) {
            Log.d("onActivityResult", "Camera SUCCESS");
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        /*Bundle extraBundle;
        if (requestCode == RESULT_OK)
        {
            extraBundle = data.getExtras();
            str = extraBundle.getString("uri");
            mUri = Uri.parse(str);
        }*/

        }
    }




    @Override
    public void onMapLongClick(LatLng point) {
        String st = null;
        List<Address> addresses = new ArrayList<>();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Marker> markers = new ArrayList<Marker>();
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("랜드마크")
                .snippet("사진을 등록해주세요")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_map_pmarker)));

        markers.add(marker);
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).showInfoWindow();

        }

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(point));
        /*mGoogleMap.addMarker(new MarkerOptions()
                  .position(point)
                  .title("랜드마크")
                  .snippet("사진을 등록해주세요.")
                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_map_pmarker)));*/



        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                int permissionCheck = ContextCompat.checkSelfPermission(MapMyRouteActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    startGallery();
                } else {
                    ActivityCompat.requestPermissions(MapMyRouteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2000);
                }
            }
        });

        //역지오코딩
        try {
            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);

            if (addresses.size() > 0) {
                android.location.Address address = addresses.get(0);
                st = address.getAddressLine(0) + address.getLocality();
            }
        } catch (IOException e) {
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
        }

        String s = " \" " + st + "\" 입니다. 저장 하시겠습니까?";
        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), s, 10000)
                .setActionTextColor(Color.parseColor("#FFFFFF"))
                .setAction("네", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //주소저장
                        Toast toast = Toast.makeText(MapMyRouteActivity.this, "저장 저장~", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
        View view = snack.getView();
        view.setBackgroundColor(Color.parseColor("#B354C242"));
        final TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
        {

            @Override
            public View getInfoWindow(Marker marker)
            {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker)
            {
                View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

                TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
                tv_title.setTextColor(Color.BLACK);
                tv_title.setGravity(Gravity.CENTER);
                tv_title.setTypeface(null, Typeface.BOLD);
                tv_title.setText(marker.getTitle());
                tv_title.setSingleLine(false);

                TextView tv_explain = (TextView)v.findViewById(R.id.tv_explain);
                tv_explain.setTextColor(Color.GRAY);
                tv_explain.setGravity(Gravity.CENTER);
                tv_explain.setTypeface(null, Typeface.ITALIC);
                tv_explain.setText(marker.getSnippet());
                tv_explain.setSingleLine(false);

                ImageView iv_map_randmark = (ImageView)v.findViewById(R.id.iv_map_randmark);
                if (uri != null)
                {
                    Glide.with(getApplicationContext())
                            .load(uri)
                            .centerCrop()
                            .override(300, 300)
                            .placeholder(R.drawable.img_cmt_insert_gallery)
                            .into(iv_map_randmark);
                    tv_explain.setVisibility(v.GONE);
                }else   tv_explain.setVisibility(v.VISIBLE);




                return v;
            }



        });
    }



    public void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_GET_CONTENT);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE);
        }
    }

    /**
     * 카메라에서 사진 촬영
     */
    /*public void doTakePhotoAction() // 카메라 촬영 후 이미지 가져오기
    {
        // 카메라 호출
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());

        // 이미지 잘라내기 위한 크기
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 150);

        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            // Do nothing for now
        }
    }*/





    private class GPSListener implements LocationListener
    {
        public void onLocationChanged(Location location)
        {

            //capture location data sent by current provider
            Double latitude = location.getLatitude();
            Double longtitude = location.getLongitude();

            mCurrentLocation = location;
            if(markerState != false)
            {
                String markerTitle = getCurrentAddress(location);
                String markerSnippet = "시작 위치";
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latitude, longtitude));
                markerOptions.title(markerTitle);
                markerOptions.snippet(markerSnippet);
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                markerState = false;
            }
            if(markerState2 != false)
            {
                String markerTitle2 = getCurrentAddress(location);
                String markerSnippet2 = "종료 위치";
                MarkerOptions markerOptions2 = new MarkerOptions();
                markerOptions2.position(new LatLng(latitude, longtitude));
                markerOptions2.title(markerTitle2);
                markerOptions2.snippet(markerSnippet2);
                markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions2);
                markerState2 = false;
            }
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 17));


            if(walkState){                                           //걸음 시작 버튼이 눌렸을 때
                endLatLng = new LatLng(latitude, longtitude);        //현재 위치를 끝점으로 설정
                drawPath();                                          //polyline 그리기
                startLatLng = new LatLng(latitude, longtitude);      //시작점을 끝점으로 다시 설정
                jsonLatlng = "" + startLatLng;
                placeList.add(jsonLatlng);

            }
        }

        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }



    @Override
    public void onMapReady(GoogleMap map)
    {

        mGoogleMap = map;
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 요부 확인하세요");
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면 런타임 퍼미션 처리 필요

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            } else {

                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }

                mGoogleMap.setMyLocationEnabled(true);

            }
        } else {

            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }

            mGoogleMap.setMyLocationEnabled(true);

        }


    }



    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }


    @Override
    public void onConnected(Bundle connectionHint) {

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Location location = null;
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude(DEFAULT_LOCATION.longitude);

        setCurrentLocation(location, "위치정보 가져올 수 없음",
                "위치 퍼미션과 GPS 활성 요부 확인하세요");
    }


    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }


    public String getCurrentAddress(Location location) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();


        if (location != null) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            //마커를 원하는 이미지로 변경해줘야함
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentMarker = mGoogleMap.addMarker(markerOptions);

            //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) 체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }

            mGoogleMap.setMyLocationEnabled(true);


        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {

                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }

                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mGoogleMap.setMyLocationEnabled(true);
                }


            } else {

                checkPermissions();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapMyRouteActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapMyRouteActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapMyRouteActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void invoke(String origin, boolean allow, boolean retain) {}

    class route_save extends AsyncTask<String,Void,Integer>
    {

        @Override
        protected Integer doInBackground(String... params) {
            String data = params[0];
            String name = params[0];

            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("data", data)
                    .add("name", name)
                    .build();

            Request request = new Request.Builder()
                    .url(Server.ROUTE_SAVE)
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
                Toast.makeText(getApplicationContext(), "저장 완료", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == ResultCode.ACK_RESULT_FAIL){
                Toast.makeText(getApplicationContext(), "저장 실패", Toast.LENGTH_LONG).show();
            }
        }
    }


}
