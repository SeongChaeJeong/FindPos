package com.demco.goopy.findtoto;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.aconcepcion.geofencemarkerbuilder.MarkerBuilderManagerV2;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.demco.goopy.findtoto.Data.GpsInfo;
import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.System.GPS_Service;
import com.demco.goopy.findtoto.Utils.AddressConvert;
import com.demco.goopy.findtoto.Utils.FileManager;
import com.demco.goopy.findtoto.Views.CircleView;
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

import org.apache.poi.hssf.util.HSSFColor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS1;
import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS4;
import static com.demco.goopy.findtoto.Data.ToToPosition.BUSINESS;
import static com.demco.goopy.findtoto.Data.ToToPosition.NAME;
import static com.demco.goopy.findtoto.PositionMangerActivity.LATITUDE_POS;
import static com.demco.goopy.findtoto.PositionMangerActivity.LONGITUDE_POS;

// https://github.com/googlemaps/android-samples 참고
// https://github.com/ac-opensource/MarkerBuilder 움직이는 서클

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static String TAG = "MapsActivity";
    private ImageView mSearchImageView;
    private ImageView mCloseImageView;
    private ImageView mCurrentImageView;
    private ImageView mGpsOffImageView;
    private ImageView mScreenSaveImageView;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private boolean mGPSRecevie = true;
    private LocationManager manager;
    private BroadcastReceiver broadcastReceiver;

    private static final float DEFAULT_ZOOM = 14.0f;
    private static final double DEFAULT_RADIUS_METERS = 200;
    private MarkerBuilderManagerV2 markerBuilderManager;

    public static final int REQUEST_MARKER_LONGCLICK = 1;
    public static final int RESULT_ITEM_SELECT = 2;
    public static final int REQUEST_SEARCH = 3;
    public static final double defaultLatitude = 37.566660;
    public static final double defaultLongitude = 126.978418;

    private List<DraggableCircle> mCircles = new ArrayList<>(1);
    private List<Marker> markerLocations = new ArrayList<>();
    private List<ToToPosition> markerPositions = null;
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
//        private final Marker mCenterMarker;
//        private final Marker mRadiusMarker;
        private final Circle mCircle;
        private final CircleView mCircleView = new CircleView(getApplicationContext());
        private double mRadiusMeters;

        public DraggableCircle(LatLng center, double radiusMeters) {
//            mRadiusMeters = radiusMeters;
//            mCenterMarker = mMap.addMarker(new MarkerOptions()
//                    .position(center)
//                    .draggable(true));
//            mRadiusMarker = mMap.addMarker(new MarkerOptions()
//                    .position(center)
//                    .draggable(true)
//                    .icon(BitmapDescriptorFactory.defaultMarker(
//                            BitmapDescriptorFactory.HUE_AZURE)));
            mCircle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radiusMeters)
                    .strokeColor(Color.BLUE)
//                    .strokeWidth(mStrokeWidthBar.getProgress())
//                    .strokeColor(mStrokeColorArgb)
//                    .fillColor(mFillColorArgb)
                    .clickable(true));
        }

        public void setClickable(boolean clickable) {
            Toast.makeText(MapsActivity.this, "setClickable", Toast.LENGTH_SHORT).show();
            mCircle.setClickable(clickable);
        }
    }

    private void setUpMyPostionMark() {
        markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
                .map(mMap)
                .enabled(true)
                .radius(200)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT)
//                .resizerIcon(R.drawable.ic_person_pin_circle)
//                .centerIcon(R.drawable.ic_person_pin_circle)
                .build();

//        markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
//                .map(googleMap)
//                .enabled(isEnabled)
//                .radius(initRadiusMetersFinal)
//                .circleId(circleId)
//                .strokeWidth(strokeWidth)
//                .strokeColor(strokeColor)
//                .fillColor(fillColor)
//                .minRadius(minRadius)
//                .maxRadius(maxRadius)
//                .centerIcon(centerIcon)
//                .centerBitmap(centerBitmap)
//                .resizerIcon(resizerIcon)
//                .centerOffsetHorizontal(centerOffsetHorizontal)
//                .centerOffsetVertical(centerOffsetVertical)
//                .build();
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(false == runtime_permissions()) {
            startGPSService();
        }
        mSearchImageView = (ImageView) findViewById(R.id.search_pos_list);
        mCurrentImageView= (ImageView) findViewById(R.id.gps_current);
        mCloseImageView = (ImageView) findViewById(R.id.app_close);
        mScreenSaveImageView = (ImageView) findViewById(R.id.screen_save);
        mGpsOffImageView = (ImageView) findViewById(R.id.gps_off);
        mainLayout = (ViewGroup) findViewById(R.id.main_layout);

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
                getCurrentGPSInfo();
            }
        });

        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MapsActivity.this)
                        .content(R.string.close_content)
                        .positiveText(R.string.agree)
                        .negativeText(R.string.disagree)
                        .backgroundColorRes(R.color.white)
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
        });

        mScreenSaveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capView = getWindow().getDecorView();
                try {
//                    screenShot(capView);
//                    mainLayout.setDrawingCacheEnabled(false);
//                    mainLayout.setDrawingCacheEnabled(true);
//                    Bitmap bmScreen = mainLayout.getDrawingCache();
//                    createImage(bmScreen);
                    screenShot(mainLayout);

                }
                catch (Exception e) {
                    Toast.makeText(MapsActivity.this, R.string.command_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 추후에 자동화 할때 필요
        mGpsOffImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPSRecevie = false;
                Toast.makeText(MapsActivity.this, R.string.gps_receive_off, Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(mGPSRecevie) {
                        double lantitute = (double)intent.getExtras().get(LATITUDE_POS);
                        double longitute = (double)intent.getExtras().get(LONGITUDE_POS);
                        markerBuilderManager.onMapClick(new LatLng(lantitute,longitute));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lantitute,longitute)));
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();
        setUpMyPostionMark();
        getCurrentGPSInfo();
        loadMarkPositions();
        addLoadMarkersToMap();

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);
    }

    private void getCurrentGPSInfo() {
        GpsInfo gps = new GpsInfo(MapsActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);
            // Showing the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            // Map 을 zoom 합니다.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            // 마커 설정.
//            MarkerOptions optFirst = new MarkerOptions();
//            optFirst.position(latLng);// 위도 • 경도
//            optFirst.title("Current Position");// 제목 미리보기
//            optFirst.snippet("Snippet");
//            optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_audiotrack));
//            mMap.addMarker(optFirst).showInfoWindow();
            markerBuilderManager.onMapClick(latLng);
        }
        else {
            Toast.makeText(this, R.string.gps_off, Toast.LENGTH_LONG).show();
        }
    }

    private void startGPSService() {
        Intent i = new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);
    }

    private void loadMarkPositions() {
        FileManager.readExcelFile(this,"address.xls");
    }

    private void addLoadMarkersToMap() {
        markerPositions = PositionDataSingleton.getInstance().getMarkerPositions();
        bizCategoryColorMap.clear();
        bizCategoryColorMap.put(getResources().getString(R.string.none), BitmapDescriptorFactory.HUE_ORANGE);
        int i = 0;
        for(ToToPosition toToPosition : markerPositions) {
            String targetName = toToPosition.rawData[NAME];
            String targetBiz = toToPosition.rawData[BUSINESS];
            if(bizCategoryColorMap.containsKey(targetBiz) == false) {
                bizCategoryColorMap.put(targetBiz, arrayPinColors[i++ % arrayPinColors.length]);
            }
            toToPosition.addressData = TextUtils.join(" ", toToPosition.addressList);
            if(TextUtils.isEmpty(toToPosition.addressData)) {
                return;
            }
            LatLng targetLatLng = AddressConvert.getLatLng(this, toToPosition.addressData);
            if(targetLatLng == null) {
                targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
            }

            mMap.addMarker(new MarkerOptions()
                    .position(targetLatLng)
                    .title(targetName)
                    .snippet(targetBiz)
                    .icon(BitmapDescriptorFactory.defaultMarker(bizCategoryColorMap.get(targetBiz))));
            // .icon(getMarkerIcon("#dadfsf"));
        }
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
//            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onCircleClick(Circle circle) {
//        mCameraTextView.setText("onCircleClick: " + mMap.getCameraPosition().toString());
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onCameraIdle() {
//        mCameraTextView.setText(mMap.getCameraPosition().toString());
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        DraggableCircle circle = new DraggableCircle(marker.getPosition(), DEFAULT_RADIUS_METERS);
        mCircles.add(circle);
//        Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
//        intent.putExtra(LATITUDE_POS, marker.getPosition().latitude);
//        intent.putExtra(LONGITUDE_POS, marker.getPosition().longitude);
//        startActivityForResult(intent, REQUEST_SEARCH);
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarker(latLng, false);
//        markerBuilderManager.onMapClick(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        for(Marker marker : markerLocations) {
            if (Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05) {
                Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
                intent.putExtra(LATITUDE_POS, latLng.latitude);
                intent.putExtra(LONGITUDE_POS, latLng.longitude);
                startActivityForResult(intent, REQUEST_MARKER_LONGCLICK);
                break;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mMap.clear();
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(focusLatitude, focusLongitude)));
                }
                break;
            default:
                for(Marker marker : markerLocations) {
                    marker.remove();
                }
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
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    public void addMarker( LatLng latLng , boolean insert) {
        if( latLng == null )
            return;

        Geocoder geocoder = new Geocoder( this );
        String address;
        try {
            address = geocoder.getFromLocation( latLng.latitude, latLng.longitude, 1 ).get( 0 ).getAddressLine( 0 );
        } catch( IOException e ) {
            address = "";
        }
        Log.e( "addMarker", address );
        addMarker( 0, latLng, address );
//        DraggableCircle circle = new DraggableCircle(latLng, DEFAULT_RADIUS_METERS);
//        mCircles.add(circle);
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
        if( false == markerLocations.contains( marker ) )
            markerLocations.add( marker );
        marker.showInfoWindow();
    }


    @Override
    public void onBackPressed() {
        return;
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

//            File f = new File(Environment.getEx(),filename);
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

}
