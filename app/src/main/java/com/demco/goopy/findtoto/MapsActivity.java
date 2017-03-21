package com.demco.goopy.findtoto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

// https://github.com/googlemaps/android-samples 참고

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnCircleClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private TextView mTapTextView;
    private TextView mCameraTextView;
    static final LatLng SEOUL = new LatLng(37.56, 126.97);
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private LocationManager manager;

    private static final double DEFAULT_RADIUS_METERS = 200;
    private static final LatLng YJ = new LatLng(37.4799, 127.0124);
    private Marker mBrisbane;

    private List<DraggableCircle> mCircles = new ArrayList<>(1);
    private List<Marker> markerLocations = new ArrayList<Marker>();

    private class DraggableCircle {
        private final Marker mCenterMarker;
        private final Marker mRadiusMarker;
        private final Circle mCircle;
        private double mRadiusMeters;

        public DraggableCircle(LatLng center, double radiusMeters) {
            mRadiusMeters = radiusMeters;
            mCenterMarker = mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .draggable(true));
            mRadiusMarker = mMap.addMarker(new MarkerOptions()
                    .position(YJ)
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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mTapTextView = (TextView) findViewById(R.id.tap_text);
        mCameraTextView = (TextView) findViewById(R.id.camera_text);
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
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCircleClickListener(this);
        enableMyLocation();
        addMarkersToMap();

    }

    private void addMarkersToMap() {
        mBrisbane = mMap.addMarker(new MarkerOptions()
                .position(YJ)
                .title("한가람미술관")
                .snippet("장사 잘되는 곳")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

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
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        addMarker(latLng);
        mTapTextView.setText("long pressed, point=" + latLng);
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


//	private void setInitialCameraPosition() {
//		double lng, lat;
//		float tilt, bearing, zoom;
//
//		SharedPreferences settings = getActivity().getSharedPreferences( EXTRAS_SHARED_PREFERENCES, 0 );
//		lng = Double.longBitsToDouble( settings.getLong( SAVED_STATE_LONG, Double.doubleToLongBits( mLocationClient.getLastLocation().getLongitude() ) ) );
//		lat = Double.longBitsToDouble( settings.getLong( SAVED_STATE_LAT, Double.doubleToLongBits( mLocationClient.getLastLocation().getLatitude() ) ) );
//		zoom = settings.getFloat( SAVED_STATE_ZOOM, 17 );
//		bearing = settings.getFloat( SAVED_STATE_BEARING, 0 );
//		tilt = settings.getFloat( SAVED_STATE_TILT, 30 );
//
//		CameraPosition cameraPosition = new CameraPosition.Builder()
//				.target( new LatLng( lat, lng) )
//				.zoom( zoom )
//				.bearing( bearing )
//				.tilt( tilt )
//				.build();
//		if( cameraPosition == null || mMap == null )
//			return;
//		mMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );
//	}

}
