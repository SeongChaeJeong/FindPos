package com.demco.goopy.findtoto;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static com.demco.goopy.findtoto.Data.ToToPosition.MODIFY;
import static com.demco.goopy.findtoto.PositionMangerActivity.LATITUDE_POS;
import static com.demco.goopy.findtoto.PositionMangerActivity.LONGITUDE_POS;
import static com.demco.goopy.findtoto.PositionMangerActivity.MARKER_ID;
import static com.demco.goopy.findtoto.PositionMangerActivity.MARKER_LOAD;
import static com.demco.goopy.findtoto.PositionMangerActivity.MARKER_TEMP;
import static com.demco.goopy.findtoto.PositionMangerActivity.MARKER_TYPE;
import static com.demco.goopy.findtoto.Utils.FileManager.ADDRESS1;
import static com.demco.goopy.findtoto.Utils.FileManager.ADDRESS5;
import static com.demco.goopy.findtoto.Utils.FileManager.BIZSTATE;
import static com.demco.goopy.findtoto.Utils.FileManager.BUSINESS;
import static com.demco.goopy.findtoto.Utils.FileManager.CHANNEL;
import static com.demco.goopy.findtoto.Utils.FileManager.LAST_INDEX;
import static com.demco.goopy.findtoto.Utils.FileManager.NAME;
import static com.demco.goopy.findtoto.Utils.FileManager.PHONE;

// https://github.com/googlemaps/android-samples 참고

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowLongClickListener,
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

    int markerColorIndex = 0;
    private double currentLantitute = defaultLatitude;
    private double currentLongitute = defaultLongitude;
    private long mainCircleRadius = (long)DEFAULT_RADIUS_METERS;

    private Map<String, MapMarker> visibleMarkers = new HashMap<>();
    private List<MapMarker> tempMarkers = new ArrayList<>();
    private List<ToToPosition> totoPositions = new ArrayList<>();
    private Map<String, Float> bizCategoryColorMap = new HashMap<>();
    private View capView;
    private ViewGroup mainLayout;
    private ImageView imageView;
    private Bitmap mbitmap;
    private boolean dataLoadComplete = false;
    private Handler handler = new Handler();

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

    private class MapMarker {
        public ToToPosition toToPosition;
        public Marker marker;
        public DraggableCircle circle;
        public LatLng tempLatLng;

        public void remove() {
            marker.remove();
            circle.remove();
        }

        @Override
        public String toString() {
            String data;
            if(null != toToPosition) {
                data = toToPosition.name + " " + toToPosition.addressData;
            }
            else {
                data = marker.getPosition().toString();
            }
            return data;
        }
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
//        if(false == runtime_permissions()) {
//            startGPSService();
//        }
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
                if(tempMarkers.isEmpty()) {
                    Toast.makeText(MapsActivity.this, R.string.no_remove_marker, Toast.LENGTH_SHORT).show();
                    return;
                }
                int lastIndex = tempMarkers.size() - 1;
                MapMarker lastMapMarker = tempMarkers.get(lastIndex);
                if(null != lastMapMarker) {
                    lastMapMarker.marker.remove();
                    lastMapMarker.circle.remove();
                    tempMarkers.remove(lastMapMarker);
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
                    store(getScreenShot(rootView), "asdf.png");
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
    protected void onStart() {
        super.onStart();
        if(false == runtime_permissions()) {
            startGPSService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopGPSService();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

        enableMyLocation();
        setUpMyPostionMark();
        getCurrentGPSInfo();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);

        if(hasPositionDataFromDB()) {
            new AddLoadMarkerTask(MapsActivity.this, R.string.wait_for_map_load_title_db, R.string.wait_for_map_load_db).execute(true);
        }
        else {
            new AddLoadMarkerTask(MapsActivity.this, R.string.wait_for_map_load_title, R.string.wait_for_map_load).execute(false);
        }
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setCompassEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class AddLoadMarkerTask extends AsyncTask<Boolean, String, String> {
        ProgressDialog progressDialog;
        Context mContext;
        int mTitle;
        int mMessage;

        public AddLoadMarkerTask(Context context, int title, int message) {
            mContext = context;
            mTitle = title;
            mMessage = message;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(mTitle);
            progressDialog.setMessage(getResources().getString(mMessage));
            progressDialog.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Boolean... params) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            bizCategoryColorMap.clear();
            bizCategoryColorMap.put(getResources().getString(R.string.none), BitmapDescriptorFactory.HUE_ORANGE);
            List<ToToPosition> positionList = PositionDataSingleton.getInstance().getMarkerPositions();
            positionList.clear();
            markerColorIndex = 0;
            if(params[0] == false) {
                HSSFSheet mySheet = FileManager.getReadExcelSheet(MapsActivity.this,"address.xls");
                if(null == mySheet) {
                    realm.commitTransaction();
                    realm.close();
                    return null;
                }
                int totalCount = mySheet.getLastRowNum();
                publishProgress("max", Integer.toString(totalCount));
                Iterator<Row> rowIter = mySheet.rowIterator();
                // 헤더 부분
                if(rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                }

                RealmResults toToPositionRealmObjRealmResults = realm.where(ToToPositionRealmObj.class).findAll();
                toToPositionRealmObjRealmResults.deleteAllFromRealm();
                int timeoutError = 0;
                int rowIndex = 0;
                while(rowIter.hasNext()) {
                    ++rowIndex;
                    publishProgress("progress", Integer.toString(rowIndex), "지도 마킹 " + Integer.toString(rowIndex) + "번 작업중");
                    String[] rawData = new String[LAST_INDEX];
                    Arrays.fill(rawData, ""); //특정 값으로 초기

                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int i = 0;
                    final ToToPosition toToPosition = new ToToPosition();
                    toToPosition.uniqueId = UUID.randomUUID().toString();
                    while (cellIter.hasNext()) {
                        if(i == LAST_INDEX) {
                            break;
                        }
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (ADDRESS1 <= i && i <= ADDRESS5) {
                            toToPosition.addressList.add(myCell.toString());
                            i++;
                        } else {
                            try {
                                rawData[i++] = myCell.toString();
                            }
                            catch (ArrayIndexOutOfBoundsException e) {
                                Log.e(TAG, e.getMessage().toString());
                            }

                        }
                    }
                    toToPosition.name = rawData[NAME];
                    toToPosition.biz = rawData[BUSINESS];
                    toToPosition.channel = rawData[CHANNEL];
                    toToPosition.bizState = rawData[BIZSTATE];
                    toToPosition.phone = rawData[PHONE];
                    toToPosition.addressData = TextUtils.join(" ", toToPosition.addressList);
                    LatLng targetLatLng = null;
                    try {
                        targetLatLng = AddressConvert.getLatLng(MapsActivity.this, toToPosition.addressData);
                        if (targetLatLng == null) {
                            targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                        }
                    } catch (TimeoutException e) {
                        targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                        if(timeoutError++ < 10) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MapsActivity.this, R.string.map_address_timeout, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    toToPosition.latLng = targetLatLng;
                    positionList.add(toToPosition);

                    // 파일에서 읽은 엑셀 정보를 디비 컬럼에 넣음
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
            }
            else {
                RealmResults toToPositionRealmObjRealmResults = realm.where(ToToPositionRealmObj.class).findAll();
                if(toToPositionRealmObjRealmResults.isEmpty()) {
                    realm.commitTransaction();
                    realm.close();
                    return null;
                }
                int totalCount = toToPositionRealmObjRealmResults.size();
                publishProgress("max", Integer.toString(totalCount));
                for(int i = 0; i< toToPositionRealmObjRealmResults.size(); ++i) {
                    ToToPositionRealmObj obj = (ToToPositionRealmObj)toToPositionRealmObjRealmResults.get(i);
                    final ToToPosition position = new ToToPosition();
                    position.uniqueId = obj.getUniqueId();
                    position.name = obj.getTargetName() != null ? obj.getTargetName() : "";
                    position.biz = obj.getTargetBiz();
                    position.addressData = obj.getAddressData();
                    position.bizState = obj.getBizState();
                    position.phone = obj.getPhone();
                    position.latLng = new LatLng(obj.getLatitude(), obj.getLongtitude());
                    positionList.add(position);
                    publishProgress("progress", Integer.toString(i), "지도 마킹 " + Integer.toString(i) + "번 작업중");
                }
            }

            realm.commitTransaction();
            realm.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
                progressDialog.setProgress(Integer.parseInt(progress[1]));
                progressDialog.setMessage(progress[2]);
            }
            else if (progress[0].equals("max")) {
                progressDialog.setMax(Integer.parseInt(progress[1]));
            }
        }

        @Override
        protected void onPostExecute(String result) {
            placeMarkers(handler);
            progressDialog.dismiss();
        }
    };

    public void placeMarkers(final Handler handler) {
        final List<ToToPosition> positionList = PositionDataSingleton.getInstance().getMarkerPositions();
        final List<ToToPosition> modifyPositionList = PositionDataSingleton.getInstance().getMarkerModifyPositions();
        new Thread() {
            @Override
            public void run(){
                final LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

                for(final ToToPosition delPos : modifyPositionList) {
                    if (visibleMarkers.containsKey(delPos.uniqueId)) {
                        // 지도에서 지우기
                        visibleMarkers.get(delPos.uniqueId).remove();
                        // 등록 목록에서 지우기
                        visibleMarkers.remove(delPos.uniqueId);
                    }

                    if(delPos.state == MODIFY) {
                        if (bizCategoryColorMap.containsKey(delPos.biz) == false) {
                            bizCategoryColorMap.put(delPos.biz, arrayPinColors[markerColorIndex++ % arrayPinColors.length]);
                        }
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(delPos.latLng)
                                .title(delPos.name)
                                .snippet(delPos.biz)
                                .icon(BitmapDescriptorFactory.defaultMarker(bizCategoryColorMap.get(delPos.biz))));
                        marker.setDraggable(true);
                        DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);

                        MapMarker mapMarker = new MapMarker();
                        mapMarker.toToPosition = delPos;
                        mapMarker.marker = marker;
                        mapMarker.circle = circle;

                        // 지도에 등록
                        visibleMarkers.put(delPos.uniqueId, mapMarker);

                    }
                }
                modifyPositionList.clear();

                for(final ToToPosition position: positionList) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            LatLng markerPoint = position.latLng;
                            if (mMap == null || false == bounds.contains(markerPoint)) {
                                return;
                            }
                            if (bizCategoryColorMap.containsKey(position.biz) == false) {
                                bizCategoryColorMap.put(position.biz, arrayPinColors[markerColorIndex++ % arrayPinColors.length]);
                            }

                            if (false == visibleMarkers.containsKey(position.uniqueId)) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(position.latLng)
                                        .title(position.name)
                                        .snippet(position.biz)
                                        .icon(BitmapDescriptorFactory.defaultMarker(bizCategoryColorMap.get(position.biz))));
                                marker.setDraggable(true);
                                DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);

                                MapMarker mapMarker = new MapMarker();
                                mapMarker.toToPosition = position;
                                mapMarker.marker = marker;
                                mapMarker.circle = circle;

                                // 지도에 등록
                                visibleMarkers.put(position.uniqueId, mapMarker);
                                Log.d(TAG, mapMarker.toString());
                            }
                        }
                    });
                }
                dataLoadComplete = true;
            }
        }.run();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
//                        GeomagneticField field = new GeomagneticField(
//                                (float)currentLantitute,
//                                (float)currentLongitute,
//                                (float)latLng.getAltitude(),
//                                System.currentTimeMillis()
//                        );
//
//                        // getDeclination returns degrees
//                        mDeclination = field.getDeclination();
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

    private void getCurrentGPSInfo() {
        gps = new GpsInfo(MapsActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            currentLantitute = gps.getLatitude();
            currentLongitute = gps.getLongitude();
            LatLng latLng = new LatLng(currentLantitute, currentLongitute);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            markerBuilderManager.onMapClick(latLng);
            updateRadiusShow(latLng);
        }
        else {
            Toast.makeText(this, R.string.gps_off, Toast.LENGTH_LONG).show();
        }
    }

    // 메인서클의 범위미터를 표시
    private void updateRadiusShow(LatLng latLng) {
        if(mResizeShowMarker != null) {
            mResizeShowMarker.remove();
        }

        mResizerRootView = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
        tvResizer = (TextView) mResizerRootView.findViewById(R.id.tv_marker);
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toString(mainCircleRadius));
        sb.append("m");

//        Location.bearingTo();
        mResizeShowMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, mResizerRootView))));
    }

    private boolean hasPositionDataFromDB() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if(realm.where(ToToPositionRealmObj.class).count() == 0) {
            realm.commitTransaction();
            realm.close();
            return false;
        }
        realm.commitTransaction();
        realm.close();
        return true;
    }

    private void tempMarkerData(LatLng latLng, String title) {
        if (mMap != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            markerOptions.title( title );
            markerOptions.icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) );
            Marker marker = mMap.addMarker( markerOptions );
            marker.setDraggable(true);
            DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);
            MapMarker mapMarker = new MapMarker();
            mapMarker.marker = marker;
            mapMarker.circle = circle;
            mapMarker.tempLatLng = latLng;
            tempMarkers.add(mapMarker);
            marker.showInfoWindow();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void startGPSService() {
        Intent i = new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);
    }

    private void stopGPSService() {
        Intent i = new Intent(getApplicationContext(),GPS_Service.class);
        stopService(i);
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
    public void onMarkerDragStart(Marker marker) {
        boolean findTempTarget = false;
        double latitude = defaultLatitude;
        double longitude = defaultLongitude;
        for(int i = 0; i < tempMarkers.size(); ++i) {
            MapMarker temp = tempMarkers.get(i);
            if(temp.marker.getId().compareTo(marker.getId()) == 0) {
                findTempTarget = true;
                latitude = temp.tempLatLng.latitude;
                longitude = temp.tempLatLng.longitude;
                temp.remove();
                tempMarkers.remove(temp);
                String logdata = String.format("temp lat: %f, long: %f === mark lat: %f, long %f",
                        latitude, longitude, marker.getPosition().latitude, marker.getPosition().longitude);
                StringBuilder sb = new StringBuilder();
                sb.append("onMarkerClick ");
                sb.append(logdata);
                Log.d(TAG, sb.toString());
                break;
            }
        }
        MapMarker findTargetMarker = null;
        if(false == findTempTarget) {
            for(Map.Entry<String,MapMarker> entry: visibleMarkers.entrySet()) {
                MapMarker mapMarker = entry.getValue();
                if(0 == mapMarker.marker.getId().compareTo(marker.getId())) {
                    findTargetMarker = mapMarker;
                    break;
                }
            }
        }

        Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
        if(findTempTarget) {
            intent.putExtra(MARKER_TYPE, MARKER_TEMP);
            intent.putExtra(LATITUDE_POS, latitude);
            intent.putExtra(LONGITUDE_POS, longitude);
        }
        else if(null != findTargetMarker) {
            intent.putExtra(MARKER_TYPE, MARKER_LOAD);
            intent.putExtra(MARKER_ID, findTargetMarker.toToPosition.uniqueId);
            intent.putExtra(LATITUDE_POS, marker.getPosition().latitude);
            intent.putExtra(LONGITUDE_POS, marker.getPosition().longitude);
        }
        else {
            Toast.makeText(this, "잘못된 마커를 터치하셨습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivityForResult(intent, REQUEST_MARKER_LONGCLICK);
    }

    @Override
    public void onMarkerDrag(Marker marker) { }

    @Override
    public void onMarkerDragEnd(Marker marker) { }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        PositionDataSingleton.getInstance().setGPSRecevie(false);
        addMarker(latLng, false);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
    }

    private void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        setClipboard(this, marker.getTitle());
    }

    //    @Override
//    public void onMapLongClick(LatLng latLng) {
//        Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
//        intent.putExtra(LATITUDE_POS, latLng.latitude);
//        intent.putExtra(LONGITUDE_POS, latLng.longitude);
//        startActivityForResult(intent, REQUEST_MARKER_LONGCLICK);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(dataLoadComplete) {
            placeMarkers(handler);
        }
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
        String countryLabel = getResources().getString(R.string.contry_label);
        String targetAddress = null;
        int index = address.indexOf(countryLabel);
        if(-1 != index) {
            targetAddress = address.substring(index + countryLabel.length());
        }
        else {
            targetAddress = address;
        }
        Log.e( "addMarker", targetAddress);
        addMarker( 0, latLng, targetAddress);
    }

    public void addMarker( float color, LatLng latLng, String title ) {
        if( latLng == null || mMap == null )
            return;
        tempMarkerData(latLng, title);
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


    @Override
    public void onCameraIdle() {
        Log.d(TAG, "onCameraIdle");
        if(dataLoadComplete) {
            placeMarkers(handler);
        }
    }

    @Override
    public void onCameraMove() {
        Log.d(TAG, "onCameraMove");
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if(i == REASON_GESTURE) {
            PositionDataSingleton.getInstance().setGPSRecevie(false);
        }
        Log.d(TAG, "onCameraMoveStarted " + String.valueOf(i));

    }

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


    private void loadMakerData(ToToPosition position, boolean visible) {
        if (visible) {
            if (bizCategoryColorMap.containsKey(position.biz) == false) {
                bizCategoryColorMap.put(position.biz, arrayPinColors[markerColorIndex++ % arrayPinColors.length]);
            }

            if (mMap != null) {
                if (false == visibleMarkers.containsKey(position.uniqueId)) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position.latLng)
                            .title(position.name)
                            .snippet(position.biz)
                            .icon(BitmapDescriptorFactory.defaultMarker(bizCategoryColorMap.get(position.biz))));
                    DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);

                    MapMarker mapMarker = new MapMarker();
                    mapMarker.toToPosition = position;
                    mapMarker.marker = marker;
                    mapMarker.circle = circle;

                    // 지도에 등록
                    visibleMarkers.put(position.uniqueId, mapMarker);
                }
            }
        }
        else {
            if (visibleMarkers.containsKey(position.uniqueId)) {
                // 지도에서 지우기
                MapMarker mapMarker = visibleMarkers.get(position.uniqueId);
                mapMarker.marker.remove();
                mapMarker.circle.remove();
                // 등록 목록에서 지우기
                visibleMarkers.remove(position.uniqueId);
            }
        }
    }

    private void clearAllTempMarker() {
    }

    private void clearAllDBMarker() {
    }

}
