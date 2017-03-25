package com.demco.goopy.findtoto;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aconcepcion.geofencemarkerbuilder.MarkerBuilderManagerV2;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.Utils.AddressConvert;
import com.demco.goopy.findtoto.Utils.FileManager;
import com.demco.goopy.findtoto.Views.CircleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS1;
import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS4;
import static com.demco.goopy.findtoto.Data.ToToPosition.BUSINESS;
import static com.demco.goopy.findtoto.Data.ToToPosition.NAME;

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
    private TextView mTapTextView;
    private TextView mCameraTextView;
    static final LatLng SEOUL = new LatLng(37.56, 126.97);
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private LocationManager manager;

    private static final double DEFAULT_RADIUS_METERS = 200;
    private static final LatLng YJ = new LatLng(37.4799, 127.0124);
    private MarkerBuilderManagerV2 markerBuilderManager;
    private Marker mBrisbane;


    private int REQUEST_SEARCH = 0;
    private int REQUEST_MAP_CLICK = 1;
    public final double clickLatitude = 37.566660;
    public final double clickLongitude = 126.978418;

    private List<DraggableCircle> mCircles = new ArrayList<>(1);
    private List<Marker> markerLocations = new ArrayList<Marker>();
    private List<ToToPosition> markerPositions = new ArrayList<ToToPosition>();

    private boolean isReallyStoppedByBackButton = false;

    private class DraggableCircle {
        private final Marker mCenterMarker;
        private final Marker mRadiusMarker;
        private final Circle mCircle;
        private final CircleView mCircleView = new CircleView(getApplicationContext());
        private double mRadiusMeters;

        public DraggableCircle(LatLng center, double radiusMeters) {
            mRadiusMeters = radiusMeters;
            mCenterMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            mRadiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE)));
            mCircle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radiusMeters)
//                    .strokeWidth(mStrokeWidthBar.getProgress())
//                    .strokeColor(mStrokeColorArgb)
//                    .fillColor(mFillColorArgb)
                    .clickable(true));
        }

        public void setClickable(boolean clickable) {
            mCircle.setClickable(clickable);
        }
    }

    private void setUpMap() {
        markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
                .map(mMap)
                .enabled(false)
                .radius(200)
                .strokeColor(Color.BLUE)
                .build();
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);
        mSearchImageView = (ImageView) findViewById(R.id.search_pos_list);
        mTapTextView = (TextView) findViewById(R.id.tap_text);
        mCameraTextView = (TextView) findViewById(R.id.camera_text);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        // 위치관리자 객체를 얻어온다
//        lm.getBestProvider(criteria, enabledOnly)
        List<String> list = lm.getAllProviders(); // 위치제공자 모두 가져오기

        String str = ""; // 출력할 문자열
        for (int i = 0; i < list.size(); i++) {
            str += "위치제공자 : " + list.get(i) + ", 사용가능여부 -"
                    + lm.isProviderEnabled(list.get(i)) +"\n";
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            Log.d(TAG, str);
        }

        mSearchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, PositionMangerActivity.class);
                intent.putExtra(PositionMangerActivity.ROW_POS, clickLatitude);
                intent.putExtra(PositionMangerActivity.CUL_POS, clickLongitude);
                startActivityForResult(intent, REQUEST_SEARCH);

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        loadMarkPositions();
        setUpMap();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);
        enableMyLocation();
        addMarkersToMap();

    }

    private void loadMarkPositions() {
        FileManager.readExcelFile(markerPositions , this,"address.xls");
    }

    private void addMarkersToMap() {
        mBrisbane = mMap.addMarker(new MarkerOptions()
                .position(YJ)
                .title("한가람미술관")
                .snippet("장사 잘되는 곳")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        for(ToToPosition toToPosition : markerPositions) {
            String targetName = toToPosition.rawData[NAME];
            String targetBiz = toToPosition.rawData[BUSINESS];
            StringBuilder sb = new StringBuilder();
            for(int i = ADDRESS1; i <= ADDRESS4; ++i) {
                sb.append(toToPosition.rawData[i]);
            }
            LatLng targetLatLng = AddressConvert.getLatLng(this, sb.toString());
            Log.d(TAG, "주소: " + sb.toString() + ", 좌표: " + targetLatLng.toString());

            mMap.addMarker(new MarkerOptions()
                .position(targetLatLng)
                .title(targetName)
                .snippet(targetBiz)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            DraggableCircle circle = new DraggableCircle(targetLatLng, DEFAULT_RADIUS_METERS);
            mCircles.add(circle);
        }

//        markerBuilderManager = new MarkerBuilderManagerV2.Builder(this)
//                .map(mMap)
//                .enabled(false)
//                .radius(200)
//                .strokeColor(Color.RED)
//                .build();

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
        DraggableCircle circle = new DraggableCircle(YJ, DEFAULT_RADIUS_METERS);
        mCircles.add(circle);
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
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onCircleClick(Circle circle) {
        mCameraTextView.setText("onCircleClick: " + mMap.getCameraPosition().toString());
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onCameraIdle() {
        mCameraTextView.setText(mMap.getCameraPosition().toString());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mTapTextView.setText("tapped, point=" + latLng);
        markerBuilderManager.onMapClick(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarker(latLng);
        mTapTextView.setText("long pressed, point=" + latLng);
        markerBuilderManager.onMapLongClick(latLng);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
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


    public void addMarker( LatLng latLng ) {
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

        DraggableCircle circle = new DraggableCircle(latLng, DEFAULT_RADIUS_METERS);
        mCircles.add(circle);
	}

	public void addMarker( float color, LatLng latLng, String title ) {
		if( latLng == null || mMap == null )
			return;

		MarkerOptions markerOptions = new MarkerOptions().position( latLng );
		if( title.isEmpty() == false )
			markerOptions.title( title );

		if( color == 0 )
			color = BitmapDescriptorFactory.HUE_RED;

		markerOptions.icon( BitmapDescriptorFactory.defaultMarker( color ) );
		Marker marker = mMap.addMarker( markerOptions );
		if( !markerLocations.contains( marker ) )
			markerLocations.add( marker );

		marker.showInfoWindow();
	}

    @Override
    public void onBackPressed() {
        if(isReallyStoppedByBackButton) {
            super.onBackPressed();
        }
        else {
            Toast.makeText(this, "한번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
            isReallyStoppedByBackButton = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isReallyStoppedByBackButton = false;
                }
            }, 2000);
        }
    }
}
