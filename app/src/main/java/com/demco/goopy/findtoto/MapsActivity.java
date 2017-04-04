package com.demco.goopy.findtoto;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aconcepcion.geofencemarkerbuilder.CircleManagerListener;
import com.aconcepcion.geofencemarkerbuilder.GeofenceCircle;
import com.aconcepcion.geofencemarkerbuilder.MarkerBuilderManagerV2;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.demco.goopy.findtoto.Data.GpsInfo;
import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.Data.ToToPositionRealmObj;
import com.demco.goopy.findtoto.System.GPS_Service;
import com.demco.goopy.findtoto.Utils.AddressConvert;
import com.demco.goopy.findtoto.Utils.FileManager;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmResults;

import static com.demco.goopy.findtoto.Data.ToToPosition.VISIBLE;
import static com.demco.goopy.findtoto.PositionMangerActivity.LATITUDE_POS;
import static com.demco.goopy.findtoto.PositionMangerActivity.LONGITUDE_POS;

// https://github.com/googlemaps/android-samples 참고

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
        CircleManagerListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int DATABASE_VERSION = 1;
    public static String TAG = "MapsActivity";
    private ImageView mSearchImageView;
    private ImageView mCloseImageView;
    private ImageView mCurrentImageView;
    private ImageView mGpsOffImageView;
    private ImageView mScreenSaveImageView;

    private View mResizerRootView;
    private TextView tvResizer;
    private BitmapDescriptor mResizeMeterShow;
    private Marker mResizeShowMarker;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private boolean mGPSRecevie = true;
    private LocationManager manager;
    private BroadcastReceiver broadcastReceiver;
    private GpsInfo gps;

    // 수자가 클수록 줌인 됨
    // 0 ~ 19
    private static final float DEFAULT_ZOOM = 16f;
    private static final int DEFAULT_MIN_RADIUS_METERS = 30;
    private static final double DEFAULT_RADIUS_METERS = 200;
    private MarkerBuilderManagerV2 markerBuilderManager;

    public static final int REQUEST_MARKER_LONGCLICK = 1;
    public static final int RESULT_ITEM_SELECT = 2;
    public static final int REQUEST_SEARCH = 3;
    public static final double defaultLatitude = 37.566660;
    public static final double defaultLongitude = 126.978418;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private double currentLantitute = defaultLatitude;
    private double currentLongitute = defaultLongitude;
    private long mainCircleRadius = (long)DEFAULT_RADIUS_METERS;

    private List<DraggableCircle> mCircles = new ArrayList<>(1);
    private List<Marker> tempMarkerLocations = new ArrayList<>();
    private List<Marker> dbMarkerLocations = new ArrayList<>();
    private Map<String, DraggableCircle> markerCircleMap = new HashMap<>();
    private List<ToToPosition> totoPositions = new ArrayList<>();
    private Map<String, Float> bizCategoryColorMap = new HashMap<>();
    private View capView;
    private ViewGroup mainLayout;
    private ImageView imageView;
    private Bitmap mbitmap;

    private float[] arrayPinColors = new float[] {
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_YELLOW
    };

    private class DraggableCircle {
        private final Circle mCircle;

        public DraggableCircle(LatLng center, double radiusMeters) {
            mCircle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radiusMeters)
                    .strokeColor(Color.BLUE)
                    .strokeWidth(5)
                    .clickable(true));
        }

        public void remove() {
            mCircle.remove();
        }

        public void setClickable(boolean clickable) {
            mCircle.setClickable(clickable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(false == runtime_permissions()) {
            startGPSService();
        }
        mGPSRecevie = true;

        final View rootView = getWindow().getDecorView().getRootView();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        PositionDataSingleton.getInstance().setGPSRecevie(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSearchImageView = (ImageView) findViewById(R.id.search_pos_list);
        mCurrentImageView= (ImageView) findViewById(R.id.gps_current);
        mCloseImageView = (ImageView) findViewById(R.id.app_close);
        mScreenSaveImageView = (ImageView) findViewById(R.id.screen_save);
        mGpsOffImageView = (ImageView) findViewById(R.id.gps_off);
        mainLayout = (ViewGroup) findViewById(R.id.main_layout);

        mResizerRootView = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        tvResizer = (TextView) mResizerRootView.findViewById(R.id.tv_marker);
        mResizeMeterShow = BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, mResizerRootView));

        mSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllDBMarker();
                Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
                startActivityForResult(intent, REQUEST_SEARCH);
            }
        });

        mCurrentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGPSRecevie == false) {
                    Toast.makeText(MapsActivity.this, R.string.gps_receive_on, Toast.LENGTH_SHORT).show();
                }
                mGPSRecevie = true;
                PositionDataSingleton.getInstance().setGPSRecevie(true);
                getCurrentGPSInfo();
            }
        });

        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempMarkerLocations.isEmpty()) {
                    Toast.makeText(MapsActivity.this, R.string.no_remove_marker, Toast.LENGTH_SHORT).show();
                    return;
                }
                int lastIndex = tempMarkerLocations.size() - 1;
                Marker lastMarker = tempMarkerLocations.get(lastIndex);
                if(null != lastMarker) {
                    if(markerCircleMap.containsKey(lastMarker.getId())) {
                        DraggableCircle circle = markerCircleMap.get(lastMarker.getId());
                        circle.remove();
                        markerCircleMap.remove(lastMarker.getId());
                    }
                    tempMarkerLocations.remove(lastMarker);
                    lastMarker.remove();
                }
            }
        });

        // 추후에 자동화 할때 필요
        mGpsOffImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPSRecevie = false;
                PositionDataSingleton.getInstance().setGPSRecevie(false);
                Toast.makeText(MapsActivity.this, R.string.gps_receive_off, Toast.LENGTH_SHORT).show();
            }
        });

        mScreenSaveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    store(getScreenShot(rootView), "asdf.png");
                    Bitmap bitmap = takeScreenshot();
                    saveBitmap(bitmap);
                }
                catch (Exception e) {
                    Toast.makeText(MapsActivity.this, R.string.command_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(loadPositionDataFromDB()) {
            addLoadMarkersToMap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    mGPSRecevie = PositionDataSingleton.getInstance().isGPSRecevie();
                    if(intent.getAction().compareTo("location_update") == 0 && mGPSRecevie) {
                        currentLantitute = (double)intent.getExtras().get(LATITUDE_POS);
                        currentLongitute = (double)intent.getExtras().get(LONGITUDE_POS);
                        LatLng latLng = new LatLng(currentLantitute,currentLongitute);
                        markerBuilderManager.onMapClick(latLng);
                        updateRadiusShow(latLng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
        registerReceiver(broadcastReceiver, new IntentFilter("postion_data_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        if(loadPositionDataFromDB()) {
            addLoadMarkersToMap();
        }
        else {
            new AddLoadMarkerTask().execute();
        }

        enableMyLocation();
        setUpMyPostionMark();
        getCurrentGPSInfo();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);
    }

    private void getCurrentGPSInfo() {
        gps = new GpsInfo(MapsActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            currentLantitute = gps.getLatitude();
            currentLongitute = gps.getLongitude();
            LatLng latLng = new LatLng(currentLantitute, currentLongitute);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            markerBuilderManager.clearCircles();
            markerBuilderManager.onMapClick(latLng);
            updateRadiusShow(latLng);
        }
        else {
            Toast.makeText(this, R.string.gps_off, Toast.LENGTH_LONG).show();
        }
    }

    private void updateRadiusShow(LatLng latLng) {
        if(mResizeShowMarker != null) {
            mResizeShowMarker.remove();
        }

        mResizerRootView = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        tvResizer = (TextView) mResizerRootView.findViewById(R.id.tv_marker);
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toString(mainCircleRadius));
        sb.append("m");

        tvResizer.setText(sb.toString());
        mResizeShowMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, mResizerRootView))));
    }

    private void syncFileDataToDB() {
        totoPositions = PositionDataSingleton.getInstance().getMarkerPositions();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults toToPositionRealmObjRealmResults = realm.where(ToToPositionRealmObj.class).findAll();
        toToPositionRealmObjRealmResults.deleteAllFromRealm();

        for(ToToPosition toToPosition : totoPositions) {
            ToToPositionRealmObj obj = realm.createObject(ToToPositionRealmObj.class);
            obj.setUniqueId(toToPosition.uniqueId);
            obj.setBizState(toToPosition.bizState);
            obj.setTargetName(toToPosition.name);
            obj.setTargetBiz(toToPosition.biz);
            obj.setPhone(toToPosition.phone);
            obj.setAddressData(toToPosition.addressData);
            obj.setLatitude(toToPosition.latLng.latitude);
            obj.setLongtitude(toToPosition.latLng.longitude);
        }
        realm.commitTransaction();
    }

    private boolean loadPositionDataFromDB() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults toToPositionRealmObjRealmResults = realm.where(ToToPositionRealmObj.class).findAll();
        if(toToPositionRealmObjRealmResults.isEmpty()) {
            realm.commitTransaction();
            realm.close();
            return false;
        }
        List<ToToPosition> loadPositions = PositionDataSingleton.getInstance().getMarkerPositions();
        loadPositions.clear();
        for(int i = 0; i< toToPositionRealmObjRealmResults.size(); ++i) {
            ToToPositionRealmObj obj = (ToToPositionRealmObj)toToPositionRealmObjRealmResults.get(i);
            ToToPosition position = new ToToPosition();
            position.uniqueId = obj.getUniqueId();
            position.name = obj.getTargetName();
            position.biz = obj.getTargetBiz();
            position.addressData = obj.getAddressData();
            position.bizState = obj.getBizState();
            position.phone = obj.getPhone();
            LatLng latLng = new LatLng(obj.getLatitude(), obj.getLongtitude());
            position.latLng = latLng;
            loadPositions.add(position);
        }
        //PositionDataSingleton.getInstance().setMarkerPositions(loadPositions);
        realm.commitTransaction();
        realm.close();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("mainCircleRadius", mainCircleRadius);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getLong("mainCircleRadius", (long)DEFAULT_RADIUS_METERS);

//        LatLng latLng;
//        latLng.latitude;
//        latLng.longitude;
//        ArrayList<testparcel> testing = this.getIntent().getParcelableArrayListExtra("extraextra");
    }

    private void startGPSService() {
        Intent i = new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);
    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void setUpMyPostionMark() {
        markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
                .map(mMap)
                .enabled(true)
                .minRadius(DEFAULT_MIN_RADIUS_METERS)
                .radius(mainCircleRadius)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT)
                .listener(this)
                .build();
    }

    private void addLoadMarkersToMap() {
        totoPositions = PositionDataSingleton.getInstance().getMarkerPositions();
        bizCategoryColorMap.clear();
        bizCategoryColorMap.put(getResources().getString(R.string.none), BitmapDescriptorFactory.HUE_ORANGE);
        int i = 0;
        for(ToToPosition toToPosition : totoPositions) {
            String targetName = toToPosition.name;
            String targetBiz = toToPosition.biz;
            if(bizCategoryColorMap.containsKey(targetBiz) == false) {
                bizCategoryColorMap.put(targetBiz, arrayPinColors[i++ % arrayPinColors.length]);
            }
            LatLng targetLatLng = toToPosition.latLng;
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(targetLatLng)
                    .title(targetName)
                    .snippet(targetBiz)
                    .icon(BitmapDescriptorFactory.defaultMarker(bizCategoryColorMap.get(targetBiz))));
                    // .icon(getMarkerIcon("#dadfsf"));

            if( false == dbMarkerLocations.contains( marker ) ) {
                DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);
                mCircles.add(circle);
                dbMarkerLocations.add( marker );
                markerCircleMap.put(marker.getId(), circle);
            }
            toToPosition.state = VISIBLE;
        }
    }

    private void addTempMarkersToMap() {
        for(Marker tempMarker : tempMarkerLocations) {
            MarkerOptions markerOptions = new MarkerOptions().position( tempMarker.getPosition() );
            markerOptions.icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) );
            Marker marker = mMap.addMarker( markerOptions );
            if( false == tempMarkerLocations.contains( marker ) ) {
                DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);
                mCircles.add(circle);
                tempMarkerLocations.add( marker );
                markerCircleMap.put(marker.getId(), circle);
            }
        }
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
//            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onCircleClick(Circle circle) {
//        Toast.makeText(this, "Circle Click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onCameraIdle() {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        clearAllDBMarker();
        for(int i = 0; i < tempMarkerLocations.size(); ++i) {
            Marker temp = tempMarkerLocations.get(i);
            if(temp.getId().compareTo(marker.getId()) == 0) {
                if(markerCircleMap.containsKey(temp.getId())) {
                        DraggableCircle circle = markerCircleMap.get(temp.getId());
                        circle.remove();
                        markerCircleMap.remove(temp.getId());
                }
                temp.remove();
                tempMarkerLocations.remove(temp);
                break;
            }
        }
        Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
        intent.putExtra(LATITUDE_POS, marker.getPosition().latitude);
        intent.putExtra(LONGITUDE_POS, marker.getPosition().longitude);
        startActivityForResult(intent, REQUEST_MARKER_LONGCLICK);
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarker(latLng, false);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        clearAllDBMarker();
        Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
        intent.putExtra(LATITUDE_POS, latLng.latitude);
        intent.putExtra(LONGITUDE_POS, latLng.longitude);
        startActivityForResult(intent, REQUEST_MARKER_LONGCLICK);
    }

    private void clearAllTempMarker() {
        for(Marker lastMarker : tempMarkerLocations) {
            if(markerCircleMap.containsKey(lastMarker.getId())) {
                DraggableCircle circle = markerCircleMap.get(lastMarker.getId());
                circle.remove();
                markerCircleMap.remove(lastMarker.getId());
            }
            tempMarkerLocations.remove(lastMarker);
            lastMarker.remove();
        }
    }

    private void clearAllDBMarker() {
        List<Marker> clearLoccation = new ArrayList<>();
        clearLoccation.addAll(dbMarkerLocations);
        for(Marker marker : clearLoccation) {
            if(null != marker) {
                if(markerCircleMap.containsKey(marker.getId())) {
                    DraggableCircle circle = markerCircleMap.get(marker.getId());
                    circle.remove();
                    markerCircleMap.remove(marker.getId());
                }
                dbMarkerLocations.remove(marker);
                marker.remove();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        addLoadMarkersToMap();
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case REQUEST_SEARCH:
                break;
            case REQUEST_MARKER_LONGCLICK:
                break;
            case RESULT_ITEM_SELECT:
                Bundle bundle = data.getExtras();
                if(null != bundle) {
                    double focusLatitude = bundle.getDouble(LATITUDE_POS, defaultLatitude);
                    double focusLongitude = bundle.getDouble(LONGITUDE_POS, defaultLongitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(focusLatitude, focusLongitude), DEFAULT_ZOOM));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                startGPSService();
            }else {
                runtime_permissions();
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void addMarker( LatLng latLng , boolean insert) {
        if( latLng == null )
            return;
        String address = AddressConvert.getAddress(this, latLng.latitude, latLng.longitude);
        Log.e( "addMarker", address );
        addMarker( 0, latLng, address );
    }

    public void addMarker( float color, LatLng latLng, String title ) {
        if( latLng == null || mMap == null )
            return;

        MarkerOptions markerOptions = new MarkerOptions().position( latLng );
        if( title.isEmpty() == false )
            markerOptions.title( title );

        if( color == 0 )
            color = BitmapDescriptorFactory.HUE_ORANGE;

        markerOptions.icon( BitmapDescriptorFactory.defaultMarker( color ) );
        Marker marker = mMap.addMarker( markerOptions );
        if( false == tempMarkerLocations.contains( marker ) ) {
            DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);
            mCircles.add(circle);
            tempMarkerLocations.add( marker );
            markerCircleMap.put(marker.getId(), circle);
        }
        marker.showInfoWindow();
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(MapsActivity.this)
                .content(R.string.close_content)
                .positiveText(R.string.agree)
                .negativeText(R.string.disagree)
                .backgroundColorRes(R.color.white)
                .positiveColorRes(R.color.dialogBtnColor)
                .negativeColorRes(R.color.dialogBtnColor)
                .contentColorRes(R.color.black)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    }
                })
                .show();
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return true;
        }
        return false;
    }

    private class AddLoadMarkerTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        public AddLoadMarkerTask() {
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.wait_for_map_load_title);
            progressDialog.setMessage(getResources().getString(R.string.wait_for_map_load));
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            FileManager.readExcelFile(MapsActivity.this,"address.xls");
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            syncFileDataToDB();
            addLoadMarkersToMap();
            progressDialog.dismiss();
        }
    };

    @Override
    public void onInitCreateCircle(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onInitCreateCircle " + geofenceCircle.toString());
    }

    @Override
    public void onCircleMarkerClick(GeofenceCircle geofenceCircle) {
        if(geofenceCircle != null) {
            Log.d(TAG, "onCircleMarkerClick " + geofenceCircle.toString());
        }
    }

    @Override
    public void onCreateCircle(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onCreateCircle " + geofenceCircle.toString());
    }

    @Override
    public void onResizeCircleEnd(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onResizeCircleEnd " + geofenceCircle.toString());
        mainCircleRadius = (long)Double.parseDouble(String.format("%.0f", geofenceCircle.getRadius()));
        markerBuilderManager.clearCircles();
        markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
                .map(mMap)
                .enabled(true)
                .minRadius(DEFAULT_MIN_RADIUS_METERS)
                .radius(mainCircleRadius)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT)
                .listener(this)
                .build();

        currentLantitute = gps.getLatitude();
        currentLongitute = gps.getLongitude();
        LatLng latLng = new LatLng(currentLantitute, currentLongitute);
        markerBuilderManager.onMapClick(latLng);
        mMap.setOnMapClickListener(this);
        updateRadiusShow(latLng);
    }


    @Override
    public void onMoveCircleEnd(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onMoveCircleEnd " + geofenceCircle.toString());
    }

    @Override
    public void onMoveCircleStart(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onMoveCircleStart " + geofenceCircle.toString());
    }

    @Override
    public void onResizeCircleStart(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onResizeCircleStart " + geofenceCircle.toString());

    }

    @Override
    public void onMinRadius(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onMinRadius " + geofenceCircle.toString());

    }

    @Override
    public void onMaxRadius(GeofenceCircle geofenceCircle) {
        Log.d(TAG, "onMaxRadius " + geofenceCircle.toString());

    }

    public void screenShot(View view) {
        mbitmap = getBitmapOFRootView(mScreenSaveImageView);
//        imageView.setImageBitmap(mbitmap);
        createImage(mbitmap);
    }

    public Bitmap getBitmapOFRootView(View v) {
        View rootview = v.getRootView();
        rootview.setDrawingCacheEnabled(true);
        Bitmap bitmap1 = rootview.getDrawingCache();
        return bitmap1;
    }

    public void createImage(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        String filename = "screenshot2.png";
        File file = new File(this.getExternalFilesDir(null), filename);
//        File file = new File(Environment.getExternalStorageDirectory() +
//                "/capturedscreenandroid.jpg");
        try {
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes.toByteArray());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void screenshot (View view) throws Exception{

        view.setDrawingCacheEnabled(true);

        Bitmap scrreenshot = view.getDrawingCache();

        String filename = "screenshot.png";

        try{
            File f = new File(this.getExternalFilesDir(null), filename);
            f.createNewFile();
            OutputStream outStream = new FileOutputStream(f);
            scrreenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        }catch( IOException e){
            e.printStackTrace();
        }

        view.setDrawingCacheEnabled(false);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    public static void store(Bitmap bm, String fileName){
        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }


    public Bitmap takeScreenshot()
    {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public void saveBitmap(Bitmap bitmap)
    {
        File imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            Log.e("GREC", e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e("GREC", e.getMessage(), e);
        }
    }

//    private void takeSnapshot() {
//        if (mMap == null) {
//            return;
//        }
//
//        final ImageView snapshotHolder = (ImageView) findViewById(R.id.snapshot_holder);
//
//        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
//            @Override
//            public void onSnapshotReady(Bitmap snapshot) {
//                // Callback is called from the main thread, so we can modify the ImageView safely.
//                snapshotHolder.setImageBitmap(snapshot);
//            }
//        };
//
//        mMap.snapshot(callback);
//    }
//
//    /**
//     * Called when the clear button is clicked.
//     */
//    public void onClearScreenshot(View view) {
//        ImageView snapshotHolder = (ImageView) findViewById(R.id.snapshot_holder);
//        snapshotHolder.setImageDrawable(null);
//    }

}
